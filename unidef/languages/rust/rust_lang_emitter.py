import copy

from pydantic import BaseModel

from unidef.emitters import Emitter
from unidef.languages.common.ir_model import *
from unidef.languages.common.type_inference import TypeInference
from unidef.languages.common.type_model import DyType, Traits
from unidef.languages.common.walk_nodes import walk_nodes
from unidef.languages.rust.rust_data_emitter import *
from unidef.languages.rust.rust_json_emitter import *
from unidef.models import config_model
from unidef.models.config_model import ModelDefinition
from unidef.utils.formatter import *
from unidef.utils.typing_ext import *
from unidef.utils.vtable import VTable


class MutabilityHandler(NodeTransformer[IrNode, IrNode]):
    to_modify: Dict[str, IrNode] = {}

    @beartype
    def accept(self, node: IrNode) -> bool:
        return node.get_field(Attributes.FunctionDecl)

    @beartype
    def transform(self, node: IrNode) -> IrNode:
        walk_nodes(node, self.collect_mutable)
        walk_nodes(node, self.mark_mutable_decl)
        for arg in node.get_field(Attributes.Arguments):
            name = arg.get_field(Attributes.ArgumentName)
            if name in self.to_modify:
                arg.append_field(Attributes.Mutable)
        return node

    def mark_mutable_decl(self, node: IrNode):
        if node.get_field(Attributes.VariableDeclaration):
            id = node.get_field(Attributes.VariableDeclarationId)
            if id in self.to_modify:
                node.append_field(Attributes.Mutable)

    def collect_mutable(self, node: IrNode):
        if node.get_field(Attributes.AssignExpression):
            left = node.get_field(Attributes.AssignExpressionLeft)
            key = left.get_field(Attributes.Identifier)
            self.to_modify[key] = node


class RustEmitterBase(VTable):
    functions: Optional[List[NodeTransformer]] = None

    def transform(self, node: IrNode) -> RustAstNode:
        ret = self(node)
        if not isinstance(ret, RustAstNode):
            pass
        return ret

    def transform_children(self, node: Children) -> RustAstNode:
        sources = []
        for child in node.children:
            sources.append(self.transform(child))
        return RustBulkNode(sources)

    def transform_raw_code(self, node: RawCodeNode) -> RustAstNode:
        return RustRawNode(node.code)

    def transform_program(self, node: ProgramNode) -> RustAstNode:
        # node = TypeInference().transform(node)
        return self.transform_children(node.body)

    # def transform_expression_statement(self, node) -> RustAstNode:
    #     expr = node.get_field(Attributes.Expression)
    #     # if expr.get_field(Attributes.Kind) == "literal":
    #     #     raw_code = expr.get_field(Attributes.RawCode)
    #     #     return RustCommentNode(content=str(raw_code).splitlines())
    #     # else:
    #     return self.transform(expr)

    def transform_directive(self, node: DirectiveNode) -> RustCommentNode:
        return RustCommentNode([node.directive])

    def transform_return(self, node: ReturnNode) -> RustReturnNode:
        return RustReturnNode(returnee=node.returnee and self.transform(node.returnee))

    def transform_argument(self, node: ParameterNode) -> RustArgumentPairNode:
        return RustArgumentPairNode(
            mutable=node.get_field_opt(Attributes.Mutable),
            name=node.argument_name,
            type=self.format_type(
                node.argument_type or Types.AllValue
            ),
        )

    def format_type(self, ty: DyType) -> str:
        return map_type_to_rust(ty)

    def get_return_type(self, node: IrNode) -> str:
        return self.format_type(
            node.get_field(Attributes.FunctionReturn) or Types.AllValue
        )

    def transform_function_decl(self, node: FunctionDecl) -> RustFuncDeclNode:
        node = MutabilityHandler().transform(node)
        assert isinstance(node, FunctionDecl)
        return RustFuncDeclNode(
            name=map_func_name(node.name),
            is_async=node.is_async,
            access=node.accessibility or AccessModifier.PUBLIC,
            args=[RustArgumentPairNode(name="&self", type="Self")]
                 + [
                     self.transform_argument(arg)
                     for arg in node.arguments
                 ],
            ret=node.function_return or Types.AllValue,
            content=[
                self.transform(n)
                for n in node.function_body.children
            ],
        )

    # def transform_inferred_type(self, node) -> RustAstNode:
    #     ty = node.get_field(Attributes.InferredType)
    #     return RustRawNode(self.format_type(ty))

    def transform_static_member_expression(self, node: MemberExpressionNode) -> RustBulkNode:
        if node.static:
            sources = []
            obj = node.obj
            prop = node.property
            if obj == "this":
                obj = "self"
            elif obj == "super":
                obj = "self.base"
            sources.append(self.transform(obj))

            sources.append(RustRawNode("."))
            sources.append(self.transform(prop))

            return RustBulkNode(sources)

        else:
            sources = []
            obj = node.obj
            prop = node.property
            if obj == "this":
                obj = "self"
            elif obj == "super":
                obj = "self.base"
            sources.append(self.transform(obj))
            sources.append(RustRawNode("["))
            sources.append(self.transform(prop))
            sources.append(RustRawNode("]"))
            return RustBulkNode(sources)

    def transform_function_call(self, node: FunctionCallNode) -> RustAstNode:
        try:
            return RustFuncCallNode(
                callee=self.transform(node.callee),
                arguments=[self.transform(n) for n in node.arguments],
            )
        except Exception as e:
            pass

    def transform_identifier(self, node: IdentifierNode) -> RustAstNode:
        return RustRawNode(node.get_field(Attributes.Identifier))

    def transform_this_expression(self, node: ThisExpressionNode):
        return RustRawNode("self")

    def transform_super_expression(self, node: SuperExpressionNode):
        return RustRawNode("self.base")

    def transform_new_expression(self, node: NewExpressionNode):
        return RustBulkNode(
            nodes=[
                self.transform(node.type),
                RustRawNode("::new("),
                RustBulkNode(
                    nodes=[
                        self.transform(n) for n in node.arguments
                    ]
                ),
                RustRawNode(")"),
            ]
        )

    def transform_literal(self, node: LiteralNode):
        return RustRawNode(
            raw=repr(node.raw_value).replace("'", '"')
        )

    def transform_requires(self, node) -> RustAstNode:
        # FIXME
        required = node.get_field(Attributes.Requires)
        sources = []
        for req in required:
            path = (
                req.get_field(Attributes.RequirePath)
                    .replace(".", "self")
                    .replace("/", "::")
            )
            key = req.get_field(Attributes.RequireKey)
            path = "::".join([path, key])
            value = req.get_field(Attributes.RequireValue)
            sources.append(RustUseNode(path=path, rename=value))

        return RustBulkNode(sources)

    def transform_throw_statement(self, node: ThrowStatementNode) -> RustAstNode:
        arg = node.content
        return RustStatementNode(
            nodes=[
                RustRawNode("return Error("),
                self.transform(arg),
                RustRawNode(")"),
            ]
        )

    def transform_statement(self, node: StatementNode) -> RustAstNode:
        transferred_node = self.transform(node.value)
        if isinstance(transferred_node, RustBlockNode):
            return transferred_node
        elif isinstance(transferred_node, RustStatementNode):
            return transferred_node
        else:
            return RustStatementNode(nodes=[transferred_node])

    def transform_variable_declarations(self, node: VariableDeclarationsNode) -> RustAstNode:
        sources = []
        for decl in node.decls:
            if isinstance(decl.id, DecomposePatternNode):
                id = "{" + ', '.join(decl.id.names) + ' }'
            else:
                id = decl.id
            sources.append(
                RustVariableDeclaration(
                    name=id,
                    init=decl.init and self.transform(decl.init),
                    ty=decl.ty,
                    mutability=decl.get_field(Attributes.Mutable)
                )
            )

        return RustBulkNode(sources)

    def transform_class_declaration(self, node: ClassDeclNode) -> RustAstNode:
        sources = []
        fields = copy.copy(node.fields)
        for base in node.get_field(Attributes.SuperClasses):
            fields.append(
                DyType.from_str(base)
                    .append_field(Traits.TypeRef(base))
                    .append_field(Traits.FieldName("base"))
            )
        name = node.get_field(Attributes.Name)
        rust_struct = RustStructNode(
            raw=StructType(name=name, fields=fields, is_data_type=False)
        )
        sources.append(rust_struct)
        functions = []
        for i, child in enumerate(node.functions):
            functions.append(self.transform_function_decl(child))

        sources.append(RustImplNode(name=rust_struct.name, functions=functions))
        return RustBulkNode(sources)

    def transform_object_properties(self, node: JsonObject) -> RustAstNode:
        return RustRawNode("todo!(\"json objects\")")
        # FIXME
        class MyJsonCrate(SerdeJsonNoMacroCrate):
            this: Any

            def transform_others(self, nd):
                return self.this.transform(nd)

        json_crate = MyJsonCrate(this=self)
        return json_crate.transform(node)

    def transform_array_elements(self, node: ArrayExpressionNode) -> RustAstNode:
        sources = [
            RustRawNode("vec!["),
            *[self.transform(n) for n in node.elements],
            RustRawNode("[")
        ]
        return RustBulkNode(sources)

    def transform_block_statement(self, node: BlockStatementNode) -> RustBlockNode:
        return RustBlockNode(nodes=[self.transform(n) for n in node.children])

    def transform_if_clauses(self, node: IfExpressionNode) -> RustAstNode:
        if node.conditional_op:
            return RustBulkNode(
                nodes=[
                    RustRawNode("if "),
                    self.transform(node.test),
                    RustRawNode(" { "),
                    self.transform(node.consequent),
                    RustRawNode(" } else { "),
                    self.transform(node.alternative),
                    RustRawNode(" }"),
                ]
            )
        else:
            sources = []
            sources.append(RustRawNode("if "))
            sources.append(
                self.transform(node.test)
            )
            sources.append(RustRawNode(" "))
            sources.append(RustBlockNode(nodes=[self.transform(node.consequent)], new_line=True))

            if node.alternative:
                sources.append(RustRawNode("else "))
                sources.append(RustBlockNode(nodes=[self.transform(node.alternative)], new_line=True))

            return RustBulkNode(sources)
    def transform_operator(self, node: OperatorNode):
        op = node.operator
        if op == "===":
            op = "=="
        elif op == "!==":
            op = "!="

        left = node.left
        middle = node.value
        right = node.right
        prefix = node.value
        postfix = node.value

        if node.kind == 'middle':
            sources = []
            sources.append(RustRawNode("("))
            sources.append(self.transform(left))
            sources.append(RustRawNode(f" {op} "))
            sources.append(self.transform(right))
            sources.append(RustRawNode(")"))
            return RustBulkNode(sources)

        elif node.kind == 'prefix':
            if op in ["++", "--"]:
                inline = [self.transform(prefix), RustRawNode(f" {op[0]}= 1")]
                return RustStatementNode(nodes=inline)
            else:
                inline = [RustRawNode(op), self.transform(prefix)]
                return RustBulkNode(inline)

        elif node.kind == 'postfix':
            if op in ["++", "--"]:
                in_block = [
                    RustStatementNode(
                        nodes=[
                            RustRawNode(f"let _t = "),
                            self.transform(postfix),
                        ]
                    ),
                    RustStatementNode(
                        nodes=[
                            self.transform(postfix),
                            RustRawNode(f" {op[0]}= 1"),
                        ]
                    ),
                    RustStatementNode(nodes=[RustRawNode("_t")]),
                ]

                return RustBlockNode(nodes=in_block)
            else:
                inline = [self.transform(postfix), RustRawNode(op)]
                return RustBulkNode(inline)

        else:
            raise NotImplementedError()

    def transform_c_for_loop(self, node: ClassicalLoopNode) -> RustAstNode:
        init = node.get_field(Attributes.CForLoopInit)
        for d in node.init.decls:
            d.append_field(Attributes.Mutable)
        init = self.transform(init)
        test = self.transform(node.test)
        update = self.transform(node.update)
        sources = [
            init,
            RustRawNode("while "),
            test,
            RustRawNode(" "),
            RustBlockNode(nodes=[self.transform(node.body), update]),
        ]
        return RustBulkNode(sources)

    def transform_await_expression(self, node: AwaitExpressionNode) -> RustAstNode:
        return RustBulkNode(
            nodes=[
                self.transform(node.value),
                RustRawNode(".await"),
            ]
        )

    def transform_try_statement(self, node: TryStatementNode) -> RustAstNode:
        """
        let _try = || {
            // try block
            Ok::<_, anyhow::Error>(())
        };
        if let Err(_catch) = _try() {
            // catch block
        }
        """
        catches = node.catch_clauses
        assert len(catches) == 1, "only supports one catch clause here"
        catch = catches[0]
        catch_name = catch.argument_name
        sources = [
            RustStatementNode(
                nodes=[
                    RustRawNode("let _try = || "),
                    RustBlockNode(
                        nodes=[
                            RustBulkNode([self.transform(n) for n in (node.try_body)]),
                            RustRawNode("Ok::<_, anyhow::Error>(())"),
                        ],
                        new_line=False,
                    ),
                ]
            ),
            RustRawNode("if let Err("),
            self.transform(catch_name),
            RustRawNode(") = _try()"),
            self.transform_children(catch.body)
        ]
        return RustBulkNode(sources)

    def transform_assign_expression(self, node: AssignmentExpressionNode) -> RustAstNode:
        return RustBulkNode(
            nodes=[
                self.transform(node.assignee),
                RustRawNode(" = "),
                self.transform(node.value),
            ]
        )


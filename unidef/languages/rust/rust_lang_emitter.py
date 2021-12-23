import copy

from pydantic import BaseModel

from unidef.emitters import Emitter
from unidef.languages.common.ir_model import (Attribute, Attributes,
                                              ClassDeclaration, IrNode)
from unidef.languages.common.type_inference import TypeInference
from unidef.languages.common.type_model import DyType, Traits
from unidef.languages.common.walk_nodes import walk_nodes
from unidef.languages.rust.rust_data_emitter import *
from unidef.languages.rust.rust_json_emitter import *
from unidef.models import config_model
from unidef.models.config_model import ModelDefinition
from unidef.utils.formatter import *
from unidef.utils.typing import *
from unidef.utils.typing import List


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


class RustEmitterBase(NodeTransformer[IrNode, RustAstNode], VisitorPattern):
    functions: Optional[List[NodeTransformer]] = None

    @beartype
    def transform(self, node: IrNode) -> RustAstNode:
        if self.functions is None:

            def acceptor(this, name):
                return this.target_name == name

            self.functions = self.get_functions("transform_", acceptor=acceptor)

        node_name = node.get_field(Attributes.Kind)
        assert node_name, f"Node name cannot be empty to emit: {node}"
        node_name = to_snake_case(node_name)

        for func in self.functions:
            if func.accept(node_name):
                result = func.transform(node)
                break
        else:
            result = NotImplemented

        if result is NotImplemented:
            return self.transform_others(node)
        else:
            return result

    @beartype
    def transform_children(self, node) -> RustAstNode:
        sources = []
        for child in node.get_field(Attributes.Children):
            sources.append(self.transform(child))
        return RustBulkNode(nodes=sources)

    @beartype
    def transform_raw_code(self, node) -> RustAstNode:
        return RustRawNode(raw=node.get_field(Attributes.RawCode))

    @beartype
    def transform_others(self, node) -> RustAstNode:
        return RustRawNode(raw=str(node))

    @beartype
    def transform_program(self, node) -> RustAstNode:
        node = TypeInference().transform(node)

        sources = []
        for child in node.get_field(Attributes.Children):
            sources.append(self.transform(child))
        return RustBulkNode(nodes=sources)

    @beartype
    def transform_expression_statement(self, node) -> RustAstNode:
        expr = node.get_field(Attributes.Expression)
        # if expr.get_field(Attributes.Kind) == "literal":
        #     raw_code = expr.get_field(Attributes.RawCode)
        #     return RustCommentNode(content=str(raw_code).splitlines())
        # else:
        return self.transform(expr)

    @beartype
    def transform_directive(self, node: IrNode) -> RustAstNode:
        return RustCommentNode([node.get_field(Attributes.Directive)])

    @beartype
    def transform_return(self, node) -> RustAstNode:
        returnee = node.get_field(Attributes.Return)
        if returnee:
            returnee = self.transform(returnee)
        return RustReturnNode(returnee=returnee)

    @beartype
    def transform_argument(self, node: IrNode) -> RustArgumentPairNode:
        return RustArgumentPairNode(
            mutable=node.get_field_opt(Attributes.Mutable),
            name=node.get_field(Attributes.ArgumentName),
            type=self.format_type(
                node.get_field(Attributes.ArgumentType) or Types.AllValue
            ),
        )

    @beartype
    def format_type(self, ty: DyType) -> str:
        return map_type_to_rust(ty)

    @beartype
    def get_return_type(self, node: IrNode) -> str:
        return self.format_type(
            node.get_field(Attributes.FunctionReturn) or Types.AllValue
        )

    @beartype
    def transform_function_decl(self, node: IrNode) -> RustFuncDeclNode:
        node = MutabilityHandler().transform(node)
        return RustFuncDeclNode(
            name=map_func_name(node.get_field(Attributes.Name)),
            is_async=node.get_field(Attributes.Async),
            access=AccessModifier.PUBLIC,
            args=[RustArgumentPairNode(name="&self", type="Self")]
            + [
                self.transform_argument(arg)
                for arg in node.get_field(Attributes.Arguments)
            ],
            ret=node.get_field(Attributes.FunctionReturn) or Types.AllValue,
            content=[
                self.transform(n)
                for n in node.get_field(Attributes.FunctionBody).get_field(
                    Attributes.Children
                )
            ],
        )

    @beartype
    def transform_inferred_type(self, node) -> RustAstNode:
        ty = node.get_field(Attributes.InferredType)
        return RustRawNode(raw=self.format_type(ty))

    @beartype
    def transform_static_member_expression(self, node) -> RustAstNode:
        sources = []
        obj = node.get_field(Attributes.MemberExpressionObject)
        prop = node.get_field(Attributes.MemberExpressionProperty)
        if obj == "this":
            obj = "self"
        elif obj == "super":
            obj = "self.base"
        sources.append(self.transform(obj))

        sources.append(RustRawNode(raw="."))
        sources.append(self.transform(prop))

        return RustBulkNode(nodes=sources)

    @beartype
    def transform_computed_member_expression(self, node) -> RustAstNode:
        sources = []
        obj = node.get_field(Attributes.MemberExpressionObject)
        prop = node.get_field(Attributes.MemberExpressionProperty)
        if obj == "this":
            obj = "self"
        elif obj == "super":
            obj = "self.base"
        sources.append(self.transform(obj))
        sources.append(RustRawNode(raw="["))
        sources.append(self.transform(prop))
        sources.append(RustRawNode(raw="]"))
        return RustBulkNode(nodes=sources)

    @beartype
    def transform_function_call(self, node) -> RustAstNode:
        return RustFuncCallNode(
            callee=self.transform(node.get_field(Attributes.Callee)),
            arguments=[self.transform(n) for n in node.get_field(Attributes.Arguments)],
        )

    @beartype
    def transform_identifier(self, node) -> RustAstNode:
        return RustRawNode(raw=node.get_field(Attributes.Identifier))

    @beartype
    def transform_this_expression(self, node):
        return RustRawNode(raw="self")

    @beartype
    def transform_super_expression(self, node):
        return RustRawNode(raw="self.base")

    @beartype
    def transform_new_expression(self, node: IrNode):
        return RustBulkNode(
            nodes=[
                self.transform(node.get_field(Attributes.Callee)),
                RustRawNode(raw="::new("),
                RustBulkNode(
                    nodes=[
                        self.transform(n) for n in node.get_field(Attributes.Arguments)
                    ]
                ),
                RustRawNode(raw=")"),
            ]
        )

    @beartype
    def transform_literal(self, node):
        return RustRawNode(
            raw=repr(node.get_field(Attributes.RawValue)).replace("'", '"')
        )

    @beartype
    def transform_requires(self, node) -> RustAstNode:
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

        return RustBulkNode(nodes=sources)

    @beartype
    def transform_throw_statement(self, node: IrNode) -> RustAstNode:
        arg = node.get_field(Attributes.ThrowStatement)
        return RustStatementNode(
            nodes=[
                RustRawNode(raw="return Error("),
                self.transform(arg),
                RustRawNode(raw=")"),
            ]
        )

    @beartype
    def transform_statement(self, node) -> RustAstNode:
        transferred_node = self.transform(node.get_field(Attributes.Statement))
        if isinstance(transferred_node, RustBlockNode):
            return transferred_node
        elif isinstance(transferred_node, RustStatementNode):
            return transferred_node
        else:
            return RustStatementNode(nodes=[transferred_node])

    @beartype
    def transform_variable_declarations(self, node) -> RustAstNode:
        sources = []
        for decl in node.get_field(Attributes.VariableDeclarations):
            assert isinstance(decl, IrNode), f"decl should be node, got {type(decl)}"
            name = decl.get_field(Attributes.VariableDeclarationId)
            init = decl.get_field(Attributes.DefaultValue)
            if init:
                init = self.transform(init)
            ty = decl.get_field(Attributes.InferredType)
            mut = decl.get_field(Attributes.Mutable)
            sources.append(
                RustVariableDeclaration(name=name, init=init, ty=ty, mutability=mut)
            )

        return RustBulkNode(nodes=sources)

    @beartype
    def transform_class_declaration(self, node: ClassDeclaration) -> RustAstNode:
        sources = []
        fields = copy.copy(node.get_field(Attributes.Fields))
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
        for i, child in enumerate(node.get_field(Attributes.Functions)):
            functions.append(self.transform_function_decl(child))

        sources.append(RustImplNode(name=rust_struct.name, functions=functions))
        return RustBulkNode(nodes=sources)

    @beartype
    def transform_object_properties(self, node) -> RustAstNode:
        class MyJsonCrate(SerdeJsonNoMacroCrate):
            this: Any

            @beartype
            def transform_others(self, nd):
                return self.this.transform(nd)

        json_crate = MyJsonCrate(this=self)
        return json_crate.transform(node)

    @beartype
    def transform_array_elements(self, node) -> RustAstNode:
        return self.transform_object_properties(node)

    @beartype
    def transform_block_statement(self, node) -> RustBlockNode:
        children = node.get_field(Attributes.Children)
        return RustBlockNode(nodes=[self.transform(n) for n in children])

    @beartype
    def transform_if_clauses(self, node) -> RustAstNode:
        sources = []
        clauses = node.get_field(Attributes.Children)
        for i, clause in enumerate(clauses):
            if clause.get_field(Attributes.IfClause):
                sources.append(RustRawNode(raw="if "))
                sources.append(
                    self.transform(clause.get_field(Attributes.TestExpression))
                )
                sources.append(RustRawNode(raw=" "))
            elif clause.get_field(Attributes.ElseIfClause):
                sources.append(RustRawNode(raw="else if "))
                sources.append(
                    self.transform(clause.get_field(Attributes.TestExpression))
                )
                sources.append(RustRawNode(raw=" "))
            elif clause.get_field(Attributes.ElseClause):
                sources.append(RustRawNode(raw="else "))
            else:
                raise NotImplementedError("unreachable")
            consequence = clause.get_field(Attributes.Consequence).get_field(
                Attributes.Children
            )
            consequence = [self.transform(n) for n in consequence]
            if i == len(clauses) - 1:
                sources.append(RustBlockNode(nodes=consequence, new_line=True))
            else:
                sources.append(RustBlockNode(nodes=consequence, new_line=False))
                sources.append(RustRawNode(raw=" "))

        return RustBulkNode(nodes=sources)

    @beartype
    def transform_operator(self, node):
        op = node.get_field(Attributes.Operator)
        if op == "===":
            op = "=="
        elif op == "!==":
            op = "!="

        left = node.get_field(Attributes.OperatorLeft)
        middle = node.get_field(Attributes.OperatorMiddle)
        right = node.get_field(Attributes.OperatorRight)
        prefix = node.get_field(Attributes.OperatorSinglePrefix)
        postfix = node.get_field(Attributes.OperatorSinglePostfix)

        if left and right:
            sources = []
            sources.append(RustRawNode(raw="("))
            sources.append(self.transform(left))
            sources.append(RustRawNode(raw=f" {op} "))
            sources.append(self.transform(right))
            sources.append(RustRawNode(raw=")"))
            return RustBulkNode(nodes=sources)

        elif prefix:
            if op in ["++", "--"]:
                inline = [self.transform(prefix), RustRawNode(raw=f" {op[0]}= 1")]
                return RustStatementNode(nodes=inline)
            else:
                inline = [RustRawNode(raw=op), self.transform(prefix)]
                return RustBulkNode(nodes=inline)

        elif postfix:
            if op in ["++", "--"]:
                in_block = [
                    RustStatementNode(
                        nodes=[
                            RustRawNode(raw=f"let _t = "),
                            self.transform(postfix),
                        ]
                    ),
                    RustStatementNode(
                        nodes=[
                            self.transform(postfix),
                            RustRawNode(raw=f" {op[0]}= 1"),
                        ]
                    ),
                    RustStatementNode(nodes=[RustRawNode(raw="_t")]),
                ]

                return RustBlockNode(nodes=in_block)
            else:
                inline = [self.transform(postfix), RustRawNode(raw=op)]
                return RustBulkNode(nodes=inline)

        else:
            raise NotImplementedError()

    @beartype
    def transform_all_value(self, node) -> RustAstNode:
        return RustRawNode(raw="serde_json::Value")

    @beartype
    def transform_c_for_loop(self, node) -> RustAstNode:
        init = node.get_field(Attributes.CForLoopInit)
        for d in init.get_field(Attributes.VariableDeclarations):
            d.append_field(Attributes.Mutable)
        init = self.transform(init)
        test = self.transform(node.get_field(Attributes.CForLoopTest))
        update = self.transform(node.get_field(Attributes.CForLoopUpdate))
        children = node.get_field(Attributes.Children)
        sources = [
            init,
            RustRawNode(raw="while "),
            test,
            RustRawNode(raw=" "),
            RustBlockNode(nodes=[self.transform(c) for c in children] + [update]),
        ]
        return RustBulkNode(nodes=sources)

    @beartype
    def transform_await_expression(self, node: IrNode) -> RustAstNode:
        return RustBulkNode(
            nodes=[
                self.transform(node.get_field(Attributes.AwaitExpression)),
                RustRawNode(raw=".await"),
            ]
        )

    @beartype
    def transform_try_statement(self, node: IrNode) -> RustAstNode:
        """
        let _try = || {
            // try block
            Ok::<_, anyhow::Error>(())
        };
        if let Err(_catch) = _try() {
            // catch block
        }
        """
        try_ = node.get_field(Attributes.TryStatement)
        catches = node.get_field(Attributes.CatchClauses)
        assert len(catches) == 1, "only supports one catch clause here"
        catch = catches[0]
        catch_name = catch.get_field(Attributes.ArgumentName)
        sources = [
            RustStatementNode(
                nodes=[
                    RustRawNode(raw="let _try = || "),
                    RustBlockNode(
                        nodes=[
                            RustBulkNode(nodes=[self.transform(n) for n in try_]),
                            RustRawNode(raw="Ok::<_, anyhow::Error>(())"),
                        ],
                        new_line=False,
                    ),
                ]
            ),
            RustRawNode(raw="if let Err("),
            self.transform(catch_name),
            RustRawNode(raw=") = _try()"),
            RustBlockNode(
                nodes=[self.transform(n) for n in catch.get_field(Attributes.Children)]
            ),
        ]
        return RustBulkNode(nodes=sources)

    @beartype
    def transform_assign_expression(self, node: IrNode) -> RustAstNode:
        left = node.get_field(Attributes.AssignExpressionLeft)
        right = node.get_field(Attributes.AssignExpressionRight)
        return RustBulkNode(
            nodes=[
                self.transform(left),
                RustRawNode(raw=" = "),
                self.transform(right),
            ]
        )

    @beartype
    def transform_conditional_expression(self, node: IrNode) -> RustAstNode:
        test = node.get_field(Attributes.TestExpression)
        consequence = node.get_field(Attributes.Consequence)
        alternative = node.get_field(Attributes.Alternative)
        return RustBulkNode(
            nodes=[
                RustRawNode(raw="if "),
                self.transform(test),
                RustRawNode(raw=" { "),
                self.transform(consequence),
                RustRawNode(raw=" } else { "),
                self.transform(alternative),
                RustRawNode(raw=" }"),
            ]
        )

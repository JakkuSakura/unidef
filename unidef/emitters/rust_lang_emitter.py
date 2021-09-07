from pydantic import BaseModel

from unidef.emitters import Emitter
from unidef.emitters.rust_data_emitter import *
from unidef.models import config_model, type_model
from unidef.models.config_model import ModelDefinition
from unidef.models.ir_model import Attribute, Attributes, Node
from unidef.models.type_model import Traits, Type
from unidef.utils.formatter import *
from unidef.utils.typing import *
from unidef.utils.typing import List
from unidef.emitters.rust_json_emitter import *


class RustEmitterBase(NodeTransformer[Any, RustAstNode], VisitorPattern):
    functions: Optional[List[(str, Callable)]] = None

    @beartype
    def transform_node(self, node: Any) -> RustAstNode:
        if self.functions is None:
            self.functions = self.get_functions("transform_")

        node_name = node.get_field(Attributes.Kind)
        assert node_name, f"Name cannot be empty to emit: {node}"
        node_name = to_snake_case(node_name)

        for name, func in self.functions:
            if name in node_name:
                result = func(node)
                break
        else:
            result = NotImplemented

        if result is NotImplemented:
            return self.transform_others(node)
        else:
            return result

    @beartype
    def transform_others(self, node) -> RustAstNode:
        return RustRawNode(raw=str(node))

    @beartype
    def transform_program(self, node) -> RustAstNode:
        sources = []
        for child in node.get_field(Attributes.Children):
            sources.append(self.transform_node(child))
        return RustBulkNode(nodes=sources)

    @beartype
    def transform_expression_statement(self, node) -> RustAstNode:
        expr = node.get_field(Attributes.Expression)
        if expr.get_field(Attributes.Kind) == "literal":
            raw_code = expr.get_field(Attributes.RawCode)
            return RustCommentNode(content=str(raw_code).splitlines())
        else:
            return self.transform_node(expr)

    @beartype
    def transform_return(self, node) -> RustAstNode:
        returnee = node.get_field(Attributes.Return)
        return RustReturnNode(returnee=self.transform_node(returnee))

    @beartype
    def transform_argument_name(self, node) -> RustArgumentNameNode:
        return RustArgumentNameNode(name=node.get_field(Attributes.ArgumentName),
                                    type=self.transform_node(node.get_field(Attributes.ArgumentType)))

    @beartype
    def format_type(self, ty: Type) -> str:
        return map_type_to_rust(ty)

    @beartype
    def get_return_type(self, node) -> str:
        return "(/* to be guessed */)"

    @beartype
    def transform_function_decl(self, node) -> RustFuncDeclNode:
        return RustFuncDeclNode(
            name=map_func_name(node.get_field(Attributes.Name)),
            is_async=node.get_field(Attributes.Async),
            access=AccessModifier.PUBLIC,
            args=[RustArgumentNameNode(name='&self', type=None)] + [
                self.transform_argument_name(arg) for arg in node.get_field(Attributes.Arguments)],
            ret=RustRawNode(raw=self.get_return_type(node)),
            content=[self.transform_node(n) for n in node.get_field(Attributes.Children)]
        )

    @beartype
    def transform_member_expression(self, node) -> RustAstNode:
        sources = []
        obj = node.get_field(Attributes.MemberExpressionObject)
        prop = node.get_field(Attributes.MemberExpressionProperty)
        if obj == "this":
            obj = "self"
        elif obj == "super":
            obj = "self.base"
        sources.append(
            self.transform_node(obj)
        )
        # FIXME: could not infer properly: o.i vs o[i]
        if prop.get_field(Attributes.Identifier):
            sources.append(RustRawNode(raw='.'))
            sources.append(self.transform_node(prop))
        else:
            sources.append(RustRawNode(raw='['))
            sources.append(self.transform_node(prop))
            sources.append(RustRawNode(raw=']'))
        return RustBulkNode(nodes=sources)

    @beartype
    def transform_function_call(self, node) -> RustAstNode:
        return RustFuncCallNode(callee=self.transform_node(node.get_field(Attributes.Callee)),
                                arguments=[self.transform_node(n) for n in node.get_field(Attributes.Arguments)])

    @beartype
    def transform_identifier(self, node) -> RustAstNode:
        return RustRawNode(raw=node.get_field(Attributes.Name))

    @beartype
    def transform_this_expression(self, node):
        return RustRawNode(raw='this')

    @beartype
    def transform_super_expression(self, node):
        return RustRawNode(raw='this.base')

    @beartype
    def transform_literal(self, node):
        return RustRawNode(raw=repr(node.get_field(Attributes.RawValue)).replace("'", "\""))

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
            path = '::'.join([path, key])
            value = req.get_field(Attributes.RequireValue)
            sources.append(RustUseNode(path=path, rename=value))

        return RustBulkNode(nodes=sources)

    @beartype
    def transform_statement(self, node) -> RustAstNode:
        transferred_node = self.transform_node(node.get_field(Attributes.Statement))
        if isinstance(transferred_node, RustBlockNode):
            return transferred_node
        else:
            return RustStatementNode(nodes=[transferred_node])

    @beartype
    def transform_variable_declarations(self, node) -> RustAstNode:
        sources = []
        for decl in node.get_field(Attributes.VariableDeclarations):
            assert isinstance(decl, Node), f"decl should be node, got {type(decl)}"
            name = decl.get_field(Attributes.Id)
            init = self.transform_node(decl.get_field(Attributes.Value))
            sources.append(RustVariableDeclaration(name=name, init=init))

        return RustBulkNode(nodes=sources)

    @beartype
    def transform_class_declaration(self, node) -> RustAstNode:
        sources = []
        fields = []
        for base in node.get_field(Attributes.SuperClasses):
            fields.append(
                Type.from_str(base)
                    .append_field(Traits.TypeRef(base))
                    .append_field(Traits.FieldName("base"))
            )
        name = node.get_field(Attributes.Name)
        rust_struct = RustStructNode(raw=Types.struct(name, fields))
        sources.append(rust_struct)
        functions = []
        for i, child in enumerate(node.get_field(Attributes.Children)):
            functions.append(self.transform_node(child))

        sources.append(RustImplNode(name=rust_struct.name, functions=functions))
        return RustBulkNode(nodes=sources)

    @beartype
    def transform_object_properties(self, node) -> RustAstNode:
        class MyJsonCrate(SerdeJsonNoMacroCrate):
            this: Any

            @beartype
            def transform_others(self, nd):
                return self.this.transform_node(nd)

        json_crate = MyJsonCrate(this=self)
        return json_crate.transform_node(node)

    @beartype
    def transform_block_statement(self, node) -> RustBlockNode:
        children = node.get_field(Attributes.Children)
        return RustBlockNode(nodes=[self.transform_node(n) for n in children])

    @beartype
    def transform_if_clauses(self, node) -> RustAstNode:
        sources = []
        clauses = node.get_field(Attributes.Children)
        for clause in clauses:
            if clause.get_field(Attributes.IfClause):
                sources.append(RustRawNode(raw='if '))
                sources.append(self.transform_node(clause.get_field(Attributes.TestExpression)))
                sources.append(RustRawNode(raw=' '))
            elif clause.get_field(Attributes.ElseIfClause):
                sources.append(RustRawNode(raw='else if '))
                sources.append(self.transform_node(clause.get_field(Attributes.TestExpression)))
                sources.append(RustRawNode(raw=' '))
            elif clause.get_field(Attributes.ElseClause):
                sources.append(RustRawNode(raw='else '))
            else:
                raise NotImplementedError("unreachable")
            sources.append(self.transform_block_statement(clause.get_field(Attributes.Consequence)))
        return RustBulkNode(nodes=sources)

    @beartype
    def transform_operator(self, node):
        op = node.get_field(Attributes.Operator)
        left = node.get_field(Attributes.OperatorLeft)
        middle = node.get_field(Attributes.OperatorMiddle)
        right = node.get_field(Attributes.OperatorRight)
        prefix = node.get_field(Attributes.OperatorSinglePrefix)
        postfix = node.get_field(Attributes.OperatorSinglePostfix)
        if left and right:
            sources = []
            sources.append(RustRawNode(raw="("))
            sources.append(self.transform_node(left))
            sources.append(RustRawNode(raw=f" {op} "))
            sources.append(self.transform_node(right))
            sources.append(RustRawNode(raw=")"))
            return RustBulkNode(nodes=sources)

        elif prefix:
            if op in ['++', '--']:
                inline = [
                    self.transform_node(prefix),
                    RustRawNode(raw=f' {op[0]}= 1')
                ]
                return RustStatementNode(nodes=inline)
            else:
                inline = [
                    RustRawNode(raw=op),
                    self.transform_node(prefix)
                ]
                return RustBulkNode(nodes=inline)

        elif postfix:
            if op in ['++', '--']:
                in_block = [
                    RustStatementNode(nodes=[RustRawNode(raw=f'let _t = '), self.transform_node(postfix)]),
                    RustStatementNode(nodes=[self.transform_node(postfix), RustRawNode(raw=f' {op[0]}= 1')]),
                    RustStatementNode(nodes=[RustRawNode(raw='_t')])
                ]

                return RustBlockNode(nodes=in_block)
            else:
                inline = [
                    self.transform_node(postfix),
                    RustRawNode(raw=op)
                ]
                return RustBulkNode(nodes=inline)

        else:
            raise NotImplementedError()

    @beartype
    def transform_all_value(self, node) -> RustAstNode:
        return RustRawNode(raw='serde_json::Value')

    @beartype
    def transform_c_for_loop(self, node) -> RustAstNode:
        init = self.transform_node(node.get_field(Attributes.CForLoopInit))
        test = self.transform_node(node.get_field(Attributes.CForLoopTest))
        update = self.transform_node(node.get_field(Attributes.CForLoopUpdate))
        children = node.get_field(Attributes.Children)
        sources = [
            init,
            RustRawNode(raw="while "),
            test,
            RustRawNode(raw=' '),
            RustBlockNode(
                nodes=[self.transform_node(c) for c in children] + [
                    update
                ]
            )
        ]
        return RustBulkNode(nodes=sources)


class RustLangEmitter(Emitter):
    def accept(self, s: str) -> bool:
        return s == "rust_lang"

    def emit_model(self, target: str, model: ModelDefinition) -> str:
        parsed = model.get_parsed()

        return self.emit_type(target, parsed)

    def emit_type(self, target: str, ty: Type) -> str:
        builder = RustEmitterBase()
        node = builder.transform_node(ty)
        formatter = RustFormatter()
        node = formatter.transform_node(node)
        formatter = StructuredFormatter(nodes=[node])
        return formatter.to_string()

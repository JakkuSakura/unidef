from pydantic import BaseModel

from unidef.emitters import Emitter
from unidef.emitters.rust_model import *
from unidef.models import config_model, type_model
from unidef.models.config_model import ModelDefinition
from unidef.models.transpile_model import Attribute, Attributes, Node
from unidef.models.type_model import Traits, Type
from unidef.utils.formatter import Formatee, Function, IndentBlock, IndentedWriter
from unidef.utils.typing_compat import *
from unidef.utils.typing_compat import List
from unidef.emitters.emitter_base import EmitterBase
from unidef.emitters.rust_json_emitter import *


class RustEmitterBase(EmitterBase):
    def emit_program(self, node):
        for child in node.get_field(Attributes.Children):
            self.emit_node(child)

    def emit_expression_statement(self, node):
        expr = node.get_field(Attributes.Expression)
        if expr.get_field(Attributes.Kind) == "literal":
            self.write(RustComment(expr.get_field(Attributes.RawCode)))
        else:
            self.emit_node(expr)

    def emit_return(self, node):
        returnee = node.get_field(Attributes.Return)
        self.formatter.append("return ")
        self.emit_node(returnee)
        self.formatter.append_line(";")

    def emit_argument_name(self, node):
        self.formatter.append(node.get_field(Attributes.ArgumentName))
        self.formatter.append(": ")
        self.formatter.append(self.format_type(node.get_field(Attributes.ArgumentType)))

    def format_type(self, ty: Type) -> str:
        return map_type_to_rust(ty)

    def get_return_type(self, node) -> str:
        return "(/* to be guessed */)"

    def emit_function_decl(self, node):
        is_pub = True
        is_async = node.get_field(Attributes.Async)
        if is_pub:
            self.formatter.append("pub ")
        if is_async:
            self.formatter.append("async ")
        name = node.get_field(Attributes.Name)
        self.formatter.append(f"fn {map_func_name(name)}(&self")
        for arg in node.get_field(Attributes.Arguments):
            self.formatter.append(", ")
            self.emit_node(arg)
        self.formatter.append(")")
        return_type = self.get_return_type(node)
        if return_type:
            self.formatter.append(f"-> {return_type}")
        self.formatter.append_line(" {")
        self.formatter.incr_indent()

        self.emit_node(node.get_field(Attributes.Children))
        self.formatter.decr_indent()
        self.formatter.append_line("}")

    def emit_member_expression(self, node):
        obj = node.get_field(Attributes.MemberExpressionObject)
        prop = node.get_field(Attributes.MemberExpressionProperty)
        if obj == "this":
            obj = "self"
        elif obj == "super":
            obj = "self.base"
        self.emit_node(obj)
        if prop.get_field(Attributes.Identifier):
            self.formatter.append(".")
            self.emit_node(prop)
        else:
            self.formatter.append("[")
            self.emit_node(prop)
            self.formatter.append("]")

    def emit_function_call(self, node):
        self.emit_node(node.get_field(Attributes.Callee))
        self.formatter.append("(")
        for i, a in enumerate(node.get_field(Attributes.Arguments)):
            if i > 0:
                self.formatter.append(", ")

            self.emit_node(a)
        self.formatter.append(f")")

    def emit_identifier(self, node):
        self.formatter.append(node.get_field(Attributes.Name))

    def emit_this_expression(self, node):
        self.formatter.append("this")

    def emit_super_expression(self, node):
        self.formatter.append("super")

    def emit_literal(self, node):
        self.formatter.append(repr(node.get_field(Attributes.RawValue)).replace("'", "\""))

    def emit_requires(self, node):
        required = node.get_field(Attributes.Requires)
        for req in required:
            path = (
                req.get_field(Attributes.RequirePath)
                    .replace(".", "self")
                    .replace("/", "::")
            )
            key = req.get_field(Attributes.RequireKey)
            value = req.get_field(Attributes.RequireValue)
            if key and value:
                self.formatter.append_line(f"use {path}::{key} as {value};")
            elif key:
                self.formatter.append_line(f"use {path}::{key};")
            else:
                self.formatter.append_line(f"use {path};")

    def emit_statement(self, node):
        self.emit_node(node.get_field(Attributes.Statement))
        self.formatter.append_line(";")

    def emit_variable_declarations(self, node):
        for decl in node.get_field(Attributes.VariableDeclarations):
            assert isinstance(decl, Node), f"decl should be node, got {type(decl)}"
            name = decl.get_field(Attributes.Id)
            self.formatter.append(f"let {name}")
            init = decl.get_field(Attributes.Value)
            if init:
                self.formatter.append(" = ")
                self.emit_node(init)
            self.formatter.append_line(";")

    def emit_class_declaration(self, node):
        fields = []
        for base in node.get_field(Attributes.SuperClasses):
            fields.append(
                Type.from_str(base)
                    .append_field(Traits.TypeRef(base))
                    .append_field(Traits.FieldName("base"))
            )
        name = node.get_field(Attributes.Name)
        rust_struct = RustStruct(raw=Types.struct(name, fields))
        self.write(rust_struct)

        self.formatter.append_line(f"""impl {rust_struct.name} {{""")
        self.formatter.incr_indent()

        for i, child in enumerate(node.get_field(Attributes.Children)):
            self.emit_node(child)

        self.formatter.decr_indent()
        self.formatter.append_line("}")

    def emit_object_properties(self, node):
        class MyJsonCrate(SerdeJsonNoMacroCrate):
            this: Any
            formatter: Any

            def emit_others(self, nd):
                self.this.emit_node(nd)

        json_crate = MyJsonCrate(this=self, formatter=self.formatter)
        json_crate.emit_node(node)

    def emit_block_statement(self, node):
        children = node.get_field(Attributes.Children)
        self.formatter.append_line(" {")
        self.formatter.incr_indent()
        self.emit_node(children)
        self.formatter.decr_indent()
        self.formatter.append("} ")

    def emit_if_clauses(self, node):
        clauses = node.get_field(Attributes.Children)
        for clause in clauses:
            if clause.get_field(Attributes.IfClause):
                self.formatter.append("if ")
                self.emit_node(clause.get_field(Attributes.TestExpression))
            elif clause.get_field(Attributes.ElseIfClause):
                self.formatter.append("else if ")
                self.emit_node(clause.get_field(Attributes.TestExpression))
            elif clause.get_field(Attributes.ElseClause):
                self.formatter.append("else")
            else:
                raise NotImplementedError("unreachable")
            self.emit_block_statement(clause.get_field(Attributes.Consequence))

        self.formatter.append_line()

    def emit_operator(self, node):
        op = node.get_field(Attributes.Operator)
        left = node.get_field(Attributes.OperatorLeft)
        middle = node.get_field(Attributes.OperatorMiddle)
        right = node.get_field(Attributes.OperatorRight)
        prefix = node.get_field(Attributes.OperatorSinglePrefix)
        postfix = node.get_field(Attributes.OperatorSinglePostfix)
        if left and right:
            self.formatter.append("(")
            self.emit_node(left)
            self.formatter.append(f" {op} ")
            self.emit_node(right)
            self.formatter.append(")")
        elif prefix:
            if op in ['++', '--']:
                self.emit_node(prefix)
                self.formatter.append_line(f' {op[0]}= 1')
            else:
                self.formatter.append(f"{op}")
                self.emit_node(prefix)
        elif postfix:
            if op in ['++', '--']:
                self.formatter.append_line('{')
                self.formatter.incr_indent()
                self.formatter.append(f'let _t = ')
                self.emit_node(postfix)
                self.formatter.append_line(';')
                self.emit_node(postfix)
                self.formatter.append_line(f' {op[0]}= 1;')
                self.formatter.append_line('_t')
                self.formatter.decr_indent()
                self.formatter.append('}')
            else:
                self.emit_node(postfix)
                self.formatter.append(f"{op}")

        else:
            raise NotImplementedError()

    def emit_c_for_loop(self, node):
        init = node.get_field(Attributes.CForLoopInit)
        test = node.get_field(Attributes.CForLoopTest)
        update = node.get_field(Attributes.CForLoopUpdate)
        children = node.get_field(Attributes.Children)
        self.emit_node(init)
        self.formatter.append("while ")
        self.emit_node(test)
        self.formatter.append_line(" {")
        self.formatter.incr_indent()
        self.emit_node(children)
        self.emit_node(update)
        self.formatter.decr_indent()
        self.formatter.append_line("}")


class RustLangEmitter(Emitter):
    def accept(self, s: str) -> bool:
        return s == "rust_lang"

    def emit_model(self, target: str, model: ModelDefinition) -> str:
        parsed = model.get_parsed()
        assert isinstance(parsed, Node)
        builder = RustEmitterBase()
        builder.emit_node(parsed)
        return builder.formatter.to_string()

    def emit_type(self, target: str, ty: Type) -> str:
        builder = RustEmitterBase()
        builder.emit_node(parsed)
        return builder.formatter.to_string()

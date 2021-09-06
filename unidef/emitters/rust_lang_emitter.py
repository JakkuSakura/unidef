from pydantic import BaseModel

from unidef.emitters import Emitter
from unidef.emitters.rust_model import *
from unidef.models import config_model, type_model
from unidef.models.config_model import ModelDefinition
from unidef.models.transpile_model import (Attribute, Attributes, Node,
                                           RequireNode)
from unidef.models.type_model import Traits, Type
from unidef.utils.formatter import (Formatee, Function, IndentBlock,
                                    IndentedWriter)
from unidef.utils.typing_compat import *
from unidef.utils.typing_compat import List
from unidef.utils.visitor import VisitorPattern


class EmitterBase(VisitorPattern):
    def __init__(self):
        super().__init__()
        self.functions = None
        self.formatter = IndentedWriter()

    def emit_node(self, node):
        if isinstance(node, Node):
            if self.functions is None:
                self.functions = self.get_functions("emit_")
            node_name = node.get_trait(Attributes.Kind)
            node_name = to_snake_case(node_name)
            for name, func in self.functions:
                if name in node_name:
                    result = func(node)
                    break
            else:
                result = NotImplemented
            if result is NotImplemented:
                self.formatter.append_line(str(node))
        elif isinstance(node, str):
            self.emit_raw(node)
        else:
            raise Exception("Could not emit " + str(node))

    def write(self, elem: Formatee):
        elem.format_with(self.formatter)

    def emit_program(self, node):
        for child in node.get_traits(Attributes.Children):
            self.emit_node(child)

    def emit_expression_statement(self, node):
        expr = node.get_trait(Attributes.Expression)
        if expr.get_trait(Attributes.Kind) == "literal":
            self.write(RustComment(expr.get_trait(Attributes.RawCode)))
        else:
            self.emit_node(expr)

    def emit_return(self, node):
        returnee = node.get_trait(Attributes.Return)
        self.formatter.append("return ")
        self.emit_node(returnee)
        self.formatter.append_line(";")

    def emit_argument_name(self, node):
        self.formatter.append(node.get_trait(Attributes.ArgumentName))
        self.formatter.append(": ")
        self.formatter.append(self.format_type(node.get_trait(Attributes.ArgumentType)))

    def format_type(self, ty: Type) -> str:
        return map_type_to_rust(ty)

    def get_return_type(self, node) -> str:
        return "(/* to be guessed */)"

    def emit_function_decl(self, node):
        is_pub = True
        is_async = node.get_trait(Attributes.Async)
        if is_pub:
            self.formatter.append("pub ")
        if is_async:
            self.formatter.append("async ")
        name = node.get_trait(Attributes.Name)
        self.formatter.append(f"fn {name}(&self")
        for arg in node.get_traits(Attributes.Arguments):
            self.formatter.append(", ")
            self.emit_node(arg)
        self.formatter.append(")")
        return_type = self.get_return_type(node)
        if return_type:
            self.formatter.append(f"-> {return_type}")
        self.formatter.append_line(" {")
        self.formatter.incr_indent()
        for child in node.get_traits(Attributes.Children):
            self.emit_node(child)
        self.formatter.decr_indent()
        self.formatter.append_line("}")

    def emit_function_call(self, node):
        name = node.get_trait(Attributes.Callee)
        arguments = node.get_traits(Attributes.Arguments)
        self.formatter.append(f"{name}(")
        for i, a in enumerate(arguments):
            if i > 0:
                self.formatter.append(", ")

            self.emit_node(a)
        self.formatter.append(f")")

    def emit_raw(self, node):
        self.formatter.append(f"{node}")

    def emit_literal(self, node):
        self.formatter.append(node.get_trait(Attributes.RawCode))

    def emit_require(self, node):
        required = node.get_traits(Attributes.Require)
        for req in required:
            req: RequireNode = req
            path = req.path.replace(".", "self").replace("/", "::")
            if req.key and req.value:
                self.formatter.append_line(f"use {path}::{req.key} as {req.value};")
            elif req.key:
                self.formatter.append_line(f"use {path}::{req.key};")
            else:
                self.formatter.append_line(f"use {path};")

    def emit_variable_declaration(self, node):
        for decl in node.get_trait(Attributes.VarDecl):
            assert isinstance(decl, Node), f"decl should be node, got {type(decl)}"
            name = decl.get_trait(Attributes.Id).get_trait(Attributes.Name)
            self.formatter.append(f"let {name}")
            init = decl.get_trait_by_name("init")
            if init:
                self.formatter.append(" = ")
                self.emit_node(init)
            self.formatter.append_line(";")

    def emit_method_definition(self, node):
        return NotImplemented

    def emit_class_declaration(self, node):
        fields = []
        for base in node.get_traits(Attributes.SuperClasses):
            fields.append(
                Type.from_str(base)
                .append_trait(Traits.TypeRef(base))
                .append_trait(Traits.FieldName("base"))
            )
        name = node.get_trait(Attributes.Name)
        rust_struct = RustStruct(raw=Types.struct(name, fields))
        self.write(rust_struct)

        self.formatter.append_line(f"""impl {rust_struct.name} {{""")
        self.formatter.incr_indent()

        for i, child in enumerate(node.get_traits(Attributes.Children)):
            self.emit_node(child)

        self.formatter.decr_indent()
        self.formatter.append_line("}")


class RustEmitter(EmitterBase):
    def __init__(self):
        super().__init__()


class RustLangEmitter(Emitter):
    def accept(self, s: str) -> bool:
        return s == "rust_lang"

    def emit_model(self, target: str, model: ModelDefinition) -> str:
        parsed = model.get_parsed()
        assert isinstance(parsed, Node)
        builder = RustEmitter()
        builder.emit_node(parsed)
        return builder.formatter.to_string()

    def emit_type(self, target: str, ty: Type) -> str:
        builder = RustEmitter()
        builder.emit_node(parsed)
        return builder.formatter.to_string()

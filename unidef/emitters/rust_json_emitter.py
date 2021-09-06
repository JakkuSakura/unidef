from pydantic import BaseModel

from unidef.emitters.registry import Emitter
from unidef.models.config_model import ModelDefinition
from unidef.models.type_model import Traits, Type
from unidef.utils.formatter import IndentedWriter
from unidef.utils.typing_compat import *
from unidef.emitters.emitter_base import EmitterBase


class JsonCrate(EmitterBase):
    object_type: str
    array_type: str
    none_type: str
    value_type: str
    no_macro: bool = False

    def __init__(self, **data: Any):
        super().__init__(**data)

    def emit_vector(self, node):
        traits = node.get_traits(Traits.ValueType)
        if self.no_macro:
            if traits:
                self.formatter.append_line("{")
                self.formatter.incr_indent()
                self.formatter.append_line("let mut node = Vec::new();")
                for field in traits:
                    for line in field.get_traits(Traits.BeforeLineComment):
                        self.formatter.append_line("// {}".format(line))
                    self.formatter.append("node.push(")
                    self.emit_node(field)
                    self.formatter.append_line(");")
                self.formatter.append_line("node")
                self.formatter.decr_indent()
                self.formatter.append_line("}")
            else:
                self.formatter.append("Vec::new()")
        else:
            self.formatter.append("vec![")
            for field in node.get_traits(Traits.ValueType):
                self.emit_node(field)
                self.formatter.append(",")
            self.formatter.append("]")

    def emit_string(self, node):
        self.formatter.append('"{}"'.format(node.get_trait(Traits.RawValue)))

    def emit_node(self, node):
        if node.get_trait(Traits.RawValue) == "undefined":
            self.formatter.append(self.none_type)
            return
        return super().emit_node(node)

    def emit_bool(self, node):
        self.formatter.append(str(node.get_trait(Traits.RawValue)).lower())

    def emit_field_key(self, node):
        field_name = node.get_trait(Traits.FieldName)
        if field_name:
            self.formatter.append(f'"{field_name}"')
        else:
            field_name = node.get_field(Traits.TypeName)
            self.formatter.append(f'"{field_name}"')

    def emit_field_value(self, node):
        self.emit_node(node)

    def emit_integer(self, node):
        self.formatter.append("{}".format(node.get_trait(Traits.RawValue)))

    def emit_float(self, node):
        self.formatter.append("{}".format(node.get_trait(Traits.RawValue)))

    def emit_struct(self, node):
        fields = node.get_traits(Traits.StructField)
        if fields:
            self.formatter.append_line("{")
            self.formatter.incr_indent()
            self.formatter.append_line(f"let mut node = <{self.object_type}>::new();")
            for field in fields:
                for line in field.get_traits(Traits.BeforeLineComment):
                    self.formatter.append_line("//{}".format(line))
                self.formatter.append("node.insert(")
                self.emit_field_key(field)
                self.formatter.append(".into(), ")
                self.emit_field_value(field)
                self.formatter.append_line(".into());")

            self.formatter.append_line("node")
            self.formatter.decr_indent()
            self.formatter.append("}")
        else:
            self.formatter.append(f"<{self.object_type}>::new()")


class IjsonCrate(JsonCrate):
    object_type = "ijson::IObject"
    array_type = "ijson::IArray"
    none_type = "Option::<ijson::IValue>::None"
    value_type = "ijson::IValue"


class SerdeJsonNoMacroCrate(JsonCrate):
    object_type = "serde_json::Map<String, serde_json::Value>"
    array_type = "Vec<serde_json::Value>"
    none_type = "serde_json::json!(null)"
    value_type = "serde_json::Value"
    only_outlier = False
    no_macro = True
    depth = 0


class SerdeJsonCrate(SerdeJsonNoMacroCrate):
    only_outlier = False
    no_macro = False

    def emit_node(self, node):
        if self.only_outlier and self.depth == 0:
            self.formatter.append("serde_json::json!(")
            self.depth += 1
            super().emit_node(node)
            self.depth -= 1
            self.formatter.append(")")
        else:
            super().emit_node(node)

    def emit_struct(self, node):
        if not self.only_outlier:
            self.formatter.append("serde_json::json!(")
        fields = node.get_traits(Traits.StructField)
        if fields:
            self.formatter.append_line("{")
            self.formatter.incr_indent()
            for field in node.get_traits(Traits.StructField):
                for line in field.get_traits(Traits.BeforeLineComment):
                    self.formatter.append_line("// {}".format(line))
                self.emit_field_key(field)
                self.formatter.append(": ")
                self.emit_field_value(field)
                self.formatter.append_line(", ")

            self.formatter.decr_indent()
            self.formatter.append("}")
        else:
            self.formatter.append("{}")
        if not self.only_outlier:
            self.formatter.append(")")

    def emit_vector(self, node):
        self.formatter.append("[")
        for field in node.get_traits(Traits.ValueType):
            self.emit_node(field)
            self.formatter.append(",")
        self.formatter.append("]")


def get_json_crate(target: str) -> JsonCrate:
    if "ijson" in target:
        result = IjsonCrate()
    elif "serde_json_no_macro" in target:
        result = SerdeJsonNoMacroCrate()
    elif "serde_json" in target:
        result = SerdeJsonCrate()
    else:
        raise Exception(f"Could not find json crate for {target}")
    if "no_macro" in target:
        result.no_macro = True
    return result


class RustJsonEmitter(Emitter):
    def accept(self, target: str) -> bool:
        return "rust" in target and "json" in target

    def emit_model(self, target: str, model: ModelDefinition) -> str:
        return self.emit_type(target, model.get_parsed())

    def emit_type(self, target: str, ty: Type) -> str:
        json_crate = get_json_crate(target)
        json_crate.emit_node(ty)
        return json_crate.formatter.to_string(strip_left=True)

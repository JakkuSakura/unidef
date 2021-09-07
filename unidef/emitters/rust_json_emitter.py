from pydantic import BaseModel

from unidef.emitters.registry import Emitter
from unidef.models.config_model import ModelDefinition
from unidef.models.type_model import Traits, Type
from unidef.utils.typing import *
from unidef.languages.rust.rust_ast import *
from unidef.utils.transformer import *
from unidef.utils.visitor import *
from unidef.utils.formatter import StructuredFormatter
from unidef.models.ir_model import Node, Attributes


class JsonCrate(NodeTransformer[Any, RustAstNode], VisitorPattern):
    def accept(self, node: Input) -> bool:
        return True

    functions: List[(str, Callable)] = None

    def transform_others(self, node):
        raise NotImplementedError()

    def transform_node(self, node) -> RustAstNode:

        if isinstance(node, Node) or isinstance(node, Type):
            if self.functions is None:
                self.functions = self.get_functions("transform_")

            if node.get_field(Traits.RawValue) == "undefined":
                return RustRawNode(raw=self.none_type)

            node_name = node.get_field(Attributes.Kind)
            assert node_name, f"Name cannot be empty to emit: {node}"
            node_name = to_snake_case(node_name)

            for name, func in self.functions:
                if name in node_name:
                    result = func(node)
                    break
            else:
                result = NotImplemented
            # TODO print comments
            if result is NotImplemented:
                return self.transform_others(node)
            return result
        elif isinstance(node, list):
            for n in node:
                self.emit_node(n)
        else:
            raise Exception("Could not emit " + str(node))

    object_type: str
    array_type: str
    none_type: str
    value_type: str
    no_macro: bool = False

    def __init__(self, **data: Any):
        super().__init__(**data)

    @beartype
    def transform_vector(self, node) -> RustAstNode:
        traits = node.get_field(Traits.ValueType)
        sources = []
        if self.no_macro:
            if traits:
                lines = []
                lines.append(RustStatementNode(raw="let mut node = Vec::new();"))
                for field in traits:
                    comments = field.get_field(Traits.BeforeLineComment)
                    if comments:
                        lines.append(RustCommentNode(comments))

                    line = [RustRawNode(raw="node.push("), self.transform_node(field), RustRawNode(raw=");")]
                    lines.append(RustBulkNode(nodes=line))
                lines.append(RustStatementNode(raw="node"))
                sources.append(RustBlockNode(nodes=lines))
            else:
                sources.append(RustStatementNode(raw="Vec::new()"))
        else:
            inner = []
            inner.append(RustRawNode(raw="vec!["))
            for i, field in enumerate(node.get_field(Traits.ValueType)):
                if i > 0:
                    inner.append(RustRawNode(raw=","))
                inner.append(self.transform_node(field))
            sources.append(RustIndentedNode(nodes=inner))
            sources.append(RustRawNode(raw="]"))
        return RustBulkNode(nodes=sources)

    @beartype
    def transform_string(self, node) -> RustAstNode:
        return RustRawNode(raw='"{}"'.format(node.get_field(Traits.RawValue)))

    @beartype
    def transform_bool(self, node) -> RustAstNode:
        return RustRawNode(raw=str(node.get_field(Traits.RawValue)).lower())

    @beartype
    def transform_field_key(self, node) -> RustAstNode:
        if node.get_field(Attributes.ObjectProperty):
            result = self.transform_node(node.get_field(Attributes.KeyName))
        else:
            field_name = node.get_field(Traits.FieldName)
            if field_name:
                result = RustRawNode(raw=f'"{field_name}"')
            else:
                result = self.transform_node(node.get_field(Attributes.KeyName))
        return result

    @beartype
    def transform_field_value(self, node) -> RustAstNode:
        if node.get_field(Attributes.ObjectProperty):
            return self.transform_node(node.get_field(Attributes.Value))
        else:
            return self.transform_node(node)

    @beartype
    def transform_integer(self, node):
        return RustRawNode(raw="{}".format(node.get_field(Traits.RawValue)))

    @beartype
    def transform_float(self, node):
        return RustRawNode(raw="{}".format(node.get_field(Traits.RawValue)))

    @beartype
    def transform_object_properties(self, node):
        return self.transform_struct(node)

    @beartype
    def transform_struct(self, node):
        fields = node.get_field(Traits.StructFields) or node.get_field(
            Attributes.ObjectProperties
        )
        if fields:
            lines = []
            lines.append(RustStatementNode(raw=f"let mut node = <{self.object_type}>::new();"))
            for field in fields:
                comments = field.get_field(Traits.BeforeLineComment)
                if comments:
                    lines.append(RustCommentNode(content=comments))
                inline = \
                    [
                        RustRawNode(raw="node.insert("),
                        self.transform_field_key(field),
                        RustRawNode(raw=".into(), "),
                        self.transform_field_value(field),
                        RustRawNode(raw=".into());", new_line=True)
                    ]
                lines.append(RustBulkNode(nodes=inline))

            lines.append(RustStatementNode(raw="node"))
            return RustBlockNode(nodes=lines)
        else:
            return RustStatementNode(raw=f"<{self.object_type}>::new()")


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
    no_macro = True


class SerdeJsonCrate(SerdeJsonNoMacroCrate):
    only_outlier = False
    depth = 0
    no_macro = False

    def transform_node(self, node):
        if self.only_outlier and self.depth == 0:
            inline = []
            inline.append(RustRawNode(raw="serde_json::json!("))
            self.depth += 1
            inline.append(super().emit_node(node))
            self.depth -= 1
            inline.append(RustRawNode(raw=")"))
            return RustBulkNode(nodes=inline)
        else:
            return super().transform_node(node)

    def transform_struct(self, node) -> RustAstNode:
        sources = []
        # FIXME: missing comments due to limitation of esprima
        emit_wrapper = not self.only_outlier
        if emit_wrapper:
            sources.append(RustRawNode(raw="serde_json::json!("))
        fields = node.get_field(Traits.StructFields)

        if fields:
            lines = []
            fields = node.get_field(Traits.StructFields)
            for i, field in enumerate(fields):
                comments = field.get_field(Traits.BeforeLineComment)
                if comments:
                    lines.append(RustCommentNode(comments))
                inline = \
                    [
                        self.transform_field_key(field),
                        RustRawNode(raw=": "),
                        self.transform_field_value(field)
                    ]
                if i < len(fields) - 1:
                    inline.append(RustRawNode(raw=", "))
                lines.append(RustStatementNode(nodes=inline))

            sources.append(RustBlockNode(nodes=lines, new_line=not emit_wrapper))

        else:
            sources.append(RustRawNode(raw="{}"))
        if emit_wrapper:
            sources.append(RustRawNode(raw=")"))
        return RustBulkNode(nodes=sources)

    def transform_vector(self, node) -> RustAstNode:
        sources = []
        sources.append(RustRawNode(raw="["))
        for i, field in enumerate(node.get_field(Traits.ValueType)):
            if i > 0:
                sources.append(RustRawNode(raw=","))
            sources.append(self.emit_node(field))
        sources.append(RustRawNode(raw="]"))
        return RustBulkNode(nodes=sources)


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
        node = json_crate.transform_node(ty)
        formatter = RustFormatter()
        node = formatter.transform_node(node)
        formatter = StructuredFormatter(nodes=[node])

        return formatter.to_string(strip_left=True)

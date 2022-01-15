from pydantic import BaseModel

from unidef.emitters.registry import Emitter
from unidef.languages.common.ir_model import Attributes, IrNode
from unidef.languages.common.type_model import DyType, Traits
from unidef.languages.rust.rust_ast import *
from unidef.models.config_model import ModelDefinition
from unidef.utils.formatter import StructuredFormatter
from unidef.utils.transformer import *
from unidef.utils.typing_ext import *
from unidef.utils.visitor import *
from ..javascript.jsonify import *


class JsonCrate(VTable):

    def __init__(self,
                 object_type: str,
                 array_type: str,
                 none_type: str,
                 value_type: str,
                 no_macro: bool = False):

        self.object_type = object_type
        self.array_type = array_type
        self.none_type = none_type
        self.value_type = value_type
        self.no_macro = no_macro

    def transform(self, val):
        return self(val)

    def transform_vector(self, node: Union[DyType, ArrayExpressionNode]) -> RustAstNode:
        fields = node.get_field(Traits.ValueTypes) or node.elements
        sources = []
        if self.no_macro:
            if fields:
                lines = []
                lines.append(RustStatementNode(raw="let mut node = Vec::new();"))
                for field in fields:
                    comments = field.get_field(Traits.BeforeLineComment)
                    if comments:
                        lines.append(RustCommentNode(comments))

                    line = [
                        RustRawNode("node.push("),
                        self.transform(field),
                        RustRawNode(")"),
                    ]
                    lines.append(RustStatementNode(nodes=line))
                lines.append(RustStatementNode(raw="node"))
                sources.append(RustBlockNode(nodes=lines, new_line=False))
            else:
                sources.append(RustStatementNode(raw="Vec::new()"))
        else:
            inner = []
            inner.append(RustRawNode("vec!["))
            for i, field in enumerate(fields):
                if i > 0:
                    inner.append(RustRawNode(","))
                inner.append(self.transform(field))
            sources.append(RustIndentedNode(inner))
            sources.append(RustRawNode("]"))
        return RustBulkNode(sources)

    def transform_string(self, node: str) -> RustAstNode:
        return RustRawNode('"{}"'.format(node))

    def transform_bool(self, node: bool) -> RustAstNode:
        return RustRawNode(str(node).lower())

    def _transform_field_key(self, node: Union[JsonProperty, DyType]) -> RustAstNode:
        if isinstance(node, JsonProperty):
            return self.transform(node.key)
        elif isinstance(node, FieldType):
            field_name = node.field_name
            result = RustRawNode(f'"{field_name}"')
            # result = self.transform(node.get_field(Attributes.KeyName))
            return result

    def _transform_field_value(self, node: Union[JsonProperty, DyType]) -> RustAstNode:
        if isinstance(node, JsonProperty):
            return self.transform(node.value)
        elif isinstance(node, FieldType):
            return self.transform(node.field_type.get_field(Traits.Default))

    def transform_integer(self, node: int):
        return RustRawNode("{}".format(node))

    def transform_float(self, node: float):
        return RustRawNode("{}".format(node))

    def transform_struct(self, node: Union[JsonObject, StructType]):
        fields = node.get_field(Traits.StructFields) or node.properties
        if fields:
            lines = []
            lines.append(
                RustStatementNode(raw=f"let mut node = <{self.object_type}>::new();")
            )
            for field in fields:
                comments = field.get_field(Traits.BeforeLineComment)
                if comments:
                    lines.append(RustCommentNode(content=comments))
                inline = [
                    RustRawNode("node.insert("),
                    self._transform_field_key(field),
                    RustRawNode(".into(), "),
                    self._transform_field_value(field),
                    RustRawNode(".into())"),
                ]
                lines.append(RustStatementNode(nodes=inline))

            lines.append(RustStatementNode(raw="node"))
            return RustBlockNode(nodes=lines, new_line=False)
        else:
            return RustRawNode(f"<{self.object_type}>::new()")


class IjsonCrate(JsonCrate):
    def __init__(self):
        super().__init__(
            object_type="ijson::IObject",
            array_type="ijson::IArray",
            none_type="Option::<ijson::IValue>::None",
            value_type="ijson::IValue"
        )


class SerdeJsonNoMacroCrate(JsonCrate):
    def __init__(self):
        super().__init__(
            object_type="serde_json::Map<String, serde_json::Value>",
            array_type="Vec<serde_json::Value>",
            none_type="serde_json::json!(null)",
            value_type="serde_json::Value",
            no_macro=True
        )


class SerdeJsonCrate(SerdeJsonNoMacroCrate):
    def __init__(self):
        super().__init__()
        self.only_outlier = False
        self.depth = 0
        self.no_macro = False

    def transform(self, node):
        if self.only_outlier and self.depth == 0:
            inline = []
            inline.append(RustRawNode("serde_json::json!("))
            self.depth += 1
            inline.append(super().emit_node(node))
            self.depth -= 1
            inline.append(RustRawNode(")"))
            return RustBulkNode(inline)
        else:
            return super().transform(node)

    def transform_struct(self, node: Union[StructType, JsonProperty]) -> RustAstNode:
        sources = []
        # FIXME: missing comments due to limitation of esprima
        emit_wrapper = not self.only_outlier
        if emit_wrapper:
            sources.append(RustRawNode("serde_json::json!("))
        fields = node.get_field(Traits.StructFields)

        if fields:
            lines = []

            for i, field in enumerate(fields):
                comments = field.get_field(Traits.BeforeLineComment)
                if comments:
                    lines.append(RustCommentNode(comments))
                inline = [
                    self._transform_field_key(field),
                    RustRawNode(": "),
                    self._transform_field_value(field),
                ]
                if i < len(fields) - 1:
                    inline.append(RustRawNode(", "))
                lines.append(RustStatementNode(nodes=inline))

            sources.append(RustBlockNode(nodes=lines, new_line=not emit_wrapper))

        else:
            sources.append(RustRawNode("{}"))
        if emit_wrapper:
            sources.append(RustRawNode(")"))
        return RustBulkNode(sources)

    def transform_vector(self, node) -> RustAstNode:
        sources = []
        sources.append(RustRawNode("["))
        for i, field in enumerate(node.get_field(Traits.ValueTypes)):
            if i > 0:
                sources.append(RustRawNode(","))
            sources.append(self.emit_node(field))
        sources.append(RustRawNode("]"))
        return RustBulkNode(sources)


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

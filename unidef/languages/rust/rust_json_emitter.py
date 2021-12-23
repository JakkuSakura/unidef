from pydantic import BaseModel

from unidef.emitters.registry import Emitter
from unidef.languages.common.ir_model import Attributes, IrNode
from unidef.languages.common.type_model import DyType, Traits
from unidef.languages.rust.rust_ast import *
from unidef.models.config_model import ModelDefinition
from unidef.utils.formatter import StructuredFormatter
from unidef.utils.transformer import *
from unidef.utils.typing import *
from unidef.utils.visitor import *


class JsonCrate(NodeTransformer[Any, RustAstNode], VisitorPattern):
    def accept(self, node: Input) -> bool:
        return True

    functions: List[NodeTransformer] = None

    def transform_others(self, node):
        raise NotImplementedError()

    def transform(self, node) -> RustAstNode:

        if isinstance(node, IrNode) or isinstance(node, DyType):
            if self.functions is None:

                def accept(this, name):
                    return name == this.target_name

                self.functions = self.get_functions("transform_", acceptor=accept)

            if node.get_field(Traits.RawValue) == "undefined":
                return RustRawNode(self.none_type)

            node_name = node.get_field(Attributes.Kind)
            assert node_name, f"Name cannot be empty to emit: {node}"
            node_name = to_snake_case(node_name)

            for func in self.functions:
                if func.accept(node_name):
                    result = func.transform(node)
                    if result is not NotImplemented:
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
    def transform_array_elements(self, node) -> RustAstNode:
        return self.transform_vector(node)

    @beartype
    def transform_vector(self, node) -> RustAstNode:
        fields = node.get_field(Traits.ValueTypes) or node.get_field(
            Attributes.ArrayElements
        )
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
            for i, field in enumerate(node.get_field(Traits.ValueTypes)):
                if i > 0:
                    inner.append(RustRawNode(","))
                inner.append(self.transform(field))
            sources.append(RustIndentedNode(inner))
            sources.append(RustRawNode("]"))
        return RustBulkNode(sources)

    @beartype
    def transform_string(self, node) -> RustAstNode:
        return RustRawNode('"{}"'.format(node.get_field(Traits.RawValue)))

    @beartype
    def transform_bool(self, node) -> RustAstNode:
        return RustRawNode(str(node.get_field(Traits.RawValue)).lower())

    @beartype
    def transform_field_key(self, node) -> RustAstNode:
        if node.get_field(Attributes.ObjectProperty):
            result = self.transform(node.get_field(Attributes.KeyName))
        else:
            field_name = node.get_field(Traits.FieldName)
            if field_name:
                result = RustRawNode(f'"{field_name}"')
            else:
                result = self.transform(node.get_field(Attributes.KeyName))
        return result

    @beartype
    def transform_field_value(self, node) -> RustAstNode:
        # TODO: this part is messy
        if node.get_field(Attributes.ObjectProperty):
            return self.transform(node.get_field(Attributes.Value))
        else:
            return self.transform(node)

    @beartype
    def transform_integer(self, node):
        return RustRawNode("{}".format(node.get_field(Traits.RawValue)))

    @beartype
    def transform_float(self, node):
        return RustRawNode("{}".format(node.get_field(Traits.RawValue)))

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
            lines.append(
                RustStatementNode(raw=f"let mut node = <{self.object_type}>::new()")
            )
            for field in fields:
                comments = field.get_field(Traits.BeforeLineComment)
                if comments:
                    lines.append(RustCommentNode(content=comments))
                inline = [
                    RustRawNode("node.insert("),
                    self.transform_field_key(field),
                    RustRawNode(".into(), "),
                    self.transform_field_value(field),
                    RustRawNode(".into())"),
                ]
                lines.append(RustStatementNode(nodes=inline))

            lines.append(RustStatementNode(raw="node"))
            return RustBlockNode(nodes=lines, new_line=False)
        else:
            return RustRawNode(f"<{self.object_type}>::new()")


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

    def transform_struct(self, node) -> RustAstNode:
        sources = []
        # FIXME: missing comments due to limitation of esprima
        emit_wrapper = not self.only_outlier
        if emit_wrapper:
            sources.append(RustRawNode("serde_json::json!("))
        fields = node.get_field(Traits.StructFields)

        if fields:
            lines = []
            fields = node.get_field(Traits.StructFields)
            for i, field in enumerate(fields):
                comments = field.get_field(Traits.BeforeLineComment)
                if comments:
                    lines.append(RustCommentNode(comments))
                inline = [
                    self.transform_field_key(field),
                    RustRawNode(": "),
                    self.transform_field_value(field),
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

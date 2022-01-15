from unidef.models.typed_field import TypedField

from unidef.languages.common.ir_model import IrNode
from unidef.languages.common.type_model import Traits, DyType
from unidef.utils.name_convert import to_snake_case
from unidef.utils.typing_ext import *
from unidef.utils.vtable import VTable


@abstract
class JsonValue(IrNode):
    pass


class JsonProperty(IrNode):
    key: IrNode
    value: IrNode

    def __init__(self, key, value):
        super().__init__(key=key, value=value)


class JsonObject(JsonValue):
    properties: List[JsonProperty]

    def __init__(self, properties):
        super().__init__(properties=properties)


class JsonRawValue(IrNode):
    val: Union[str, int, float]

    def __init__(self, val):
        super().__init__(val=val)

class ArrayExpressionNode(IrNode):
    elements: List[IrNode]
class Jsonify(VTable):
    def default(self, value, *args, **kwargs):
        raise NotImplementedError()

    def transform_key_str(self, node: str) -> IrNode:
        return JsonRawValue(val=node)

    def transform_int(self, node: int) -> IrNode:
        return JsonRawValue(val=node)

    def transform_float(self, node: float) -> IrNode:
        return JsonRawValue(val=node)

    def transform_object(self, node: dict) -> JsonObject:
        properties = []
        for key, val in node.items():
            properties.append(JsonProperty(self(key), self(val)))
        return JsonObject(properties=properties)

from unidef.languages.common.type_model import *
from unidef.models.input_model import FieldsInput, InputDefinition
from unidef.parsers import Parser
from unidef.utils.typing import *


class FieldsParser(Parser):
    def accept(self, fmt: InputDefinition) -> bool:
        return isinstance(fmt, FieldsInput)

    def parse_field(self, field: Dict[str, Any]) -> FieldType:
        field = field.copy()
        name = field.pop("name")
        type_ref = field.pop("type")
        ty = GLOBAL_TYPE_REGISTRY.get_type(type_ref)
        if ty:
            ty = ty.copy()
        else:
            ty = DyType.from_trait(name, Traits.TypeRef(type_ref))

        for key, val in field.items():
            trait = GLOBAL_TYPE_REGISTRY.get_trait(key)
            if trait is not None:
                ty.append_field(trait(val))
            else:
                raise Exception("InvalidArgumentException: " + key)
        return FieldType(field_name=name, field_type=ty)

    def parse(self, name: str, fmt: InputDefinition) -> DyType:
        assert isinstance(fmt, FieldsInput)
        fields = []
        for field in fmt.__root__:
            fields.append(self.parse_field(field))

        return StructType(name=name, fields=fields)

from enum import Enum
from io import IOBase
from typing import Optional, List, Any, Dict, Union
from models.type_model import Type, parse_data_example, Traits, GLOBAL_TYPE_REGISTRY, Types
import pyhocon
import yaml
from pydantic import BaseModel
import unicodedata


class ExampleFormat(Enum):
    JSON = 'JSON'


class ModelExample(BaseModel):
    format: ExampleFormat
    text: str

    def __init__(self, format: ExampleFormat, text: str):
        super().__init__(format=format, text=unicodedata.normalize('NFKC', text))

    def get_parsed(self, name='') -> Type:
        parsed = None
        if self.format == ExampleFormat.JSON:
            parsed = parse_data_example(dict(pyhocon.ConfigParser.parse(self.text)), name)

        if parsed.get_trait(Traits.Struct) and name:
            parsed.get_trait(Traits.Name).value = name
        return parsed


class InvalidArgumentException(Exception):
    pass


class ModelDefinition(BaseModel):
    type: str = 'untyped'
    name: str
    url: str = ''
    ref: str = ''
    note: str = ''
    raw: str = ''
    example: Optional[ModelExample] = None
    fields: Optional[List[Dict[str, Any]]] = None
    variants: Optional[List[Dict[str, Any]]] = None

    def get_parsed(self) -> Type:
        if self.example:
            parsed = self.example.get_parsed(self.name)
            return parsed
        if self.fields:
            fields = []
            for field in self.fields:
                name = field['name']
                type_ref = field['type']
                ty = GLOBAL_TYPE_REGISTRY.get_type(type_ref)
                if not ty:
                    ty = Type.from_str(name).append_trait(Traits.TypeRef.init_with(type_ref))
                else:
                    ty = ty.copy()
                    ty.replace_trait(Traits.Name.init_with(name))

                for key, val in field.items():
                    if key not in ['name', 'type']:
                        trait = GLOBAL_TYPE_REGISTRY.get_trait(key)
                        if trait is not None:
                            ty.append_trait(trait.init_with(val))
                        else:
                            raise InvalidArgumentException(key)

                fields.append(ty)

            return Types.struct(self.name, fields)
        if self.variants:
            variants = []
            for var in self.variants:
                variants.append(Types.variant(var['name'].split()))
            return Types.enum(self.name, variants)


def read_model_definition(content: Union[str, IOBase]) -> List[ModelDefinition]:
    if isinstance(content, IOBase):
        content = content.read()

    defs = []
    segments = content.split('---')
    for seg in segments:
        seg = seg.strip()
        if not seg:
            continue

        data = yaml.safe_load(seg)
        loaded_model = ModelDefinition(**dict(data.items()))
        loaded_model.raw = seg
        defs.append(loaded_model)

    return defs

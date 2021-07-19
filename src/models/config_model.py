from enum import Enum
from io import IOBase
from typing import Optional, List, Any, Dict, Union
from models.type_model import Type, parse_data_example, Traits
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

        if parsed.get_first_trait(Traits.Struct) and name:
            parsed.get_first_trait(Traits.Name).value = name
        return parsed


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
                primary = bool(field.get('primary')) or False
                nullable = bool(field.get('nullable')) or False
                comment = field.get('comment') or ''
                ty = parse_type_definition(field['type'])

                fields.append(StructField(name=name, primary=primary, value=OptionalType(ty) if nullable else ty,
                                          comment=comment))
            return StructType(name=self.name, fields=fields)
        if self.variants:
            variants = []
            for enum in self.variants:
                e = EnumVariant(name=enum['name'].split())
                variants.append(e)
            return EnumType(name=self.name, variants=variants)


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

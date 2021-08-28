from enum import Enum
from io import IOBase
from utils.typing_compat import Optional, List, Any, Union, Dict
from models.type_model import Type, parse_data_example, Traits, GLOBAL_TYPE_REGISTRY, Types, Trait
import pyhocon
import yaml
from pydantic import BaseModel
import unicodedata
from api_parsers.api_format import API_FORMAT_REGISTRY


class ModelExample(BaseModel):
    format: str
    text: str

    def get_parsed(self, name='') -> Type:
        parser = API_FORMAT_REGISTRY.find_parser(self.format)
        if parser is None:
            raise Exception(f'Could not recognize format {self.format} for {name}')
        return parser.parse(self.format, name, self.text)


class InvalidArgumentException(Exception):
    pass


class ModelDefinition(BaseModel):
    type: str = 'untyped'
    name: str
    url: str = ''
    ref: str = ''
    note: str = ''
    raw: str = ''
    traits: List[Dict[str, Any]] = []
    example: Optional[ModelExample] = None
    fields: Optional[List[Dict[str, Any]]] = None
    variants: Optional[List[Dict[str, Any]]] = None

    def parse_field(self, field: Dict[str, Any]) -> Type:
        field = field.copy()
        name = field.pop('name')
        type_ref = field.pop('type')
        ty = GLOBAL_TYPE_REGISTRY.get_type(type_ref)
        if ty:
            ty = ty.copy()
            ty.replace_trait(Traits.Name.init_with(name))
        else:
            ty = Type.from_str(name).append_trait(Traits.TypeRef.init_with(type_ref))

        for key, val in field.items():
            trait = GLOBAL_TYPE_REGISTRY.get_trait(key)
            if trait is not None:
                ty.append_trait(trait.init_with(val))
            else:
                raise InvalidArgumentException(key)
        return ty

    def get_traits(self) -> List[Trait]:
        traits = []
        for t in self.traits:
            name = t['name']
            value = t['value']
            trait = GLOBAL_TYPE_REGISTRY.get_trait(name)
            if trait is None:
                trait = Trait.from_str(name).init_with(value)
            else:
                trait = trait.init_with(value)
            traits.append(trait)
        return traits

    def get_parsed(self) -> Type:
        def inner():
            if self.example:
                parsed = self.example.get_parsed(self.name)
                return parsed
            if self.fields:
                fields = []
                for field in self.fields:
                    fields.append(self.parse_field(field))

                return Types.struct(self.name, fields)
            if self.variants:
                variants = []
                for var in self.variants:
                    variants.append(Types.variant(var['name'].split()))
                return Types.enum(self.name, variants)

        parsed = inner()
        for t in self.get_traits():
            parsed.append_trait(t)
        return parsed


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

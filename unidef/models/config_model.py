import unicodedata
from enum import Enum
from io import IOBase

import pyhocon
import yaml
from pydantic import BaseModel, root_validator

from unidef.models.input_model import *
from unidef.models.type_model import (
    GLOBAL_TYPE_REGISTRY,
    Trait,
    Traits,
    DyType,
    Types,
    infer_type_from_example,
)
from unidef.parsers.registry import PARSER_REGISTRY
from unidef.utils.typing import *
from unidef.models.ir_model import IrNode


class ModelDefinition(BaseModel):
    type: str = "untyped"
    name: str
    url: str = ""
    ref: str = ""
    note: str = ""
    raw: str = ""
    traits: List[Dict[str, Any]] = []
    example: Optional[ExampleInput] = None
    fields: Optional[FieldsInput] = None
    variants: Optional[VariantsInput] = None
    source: Optional[SourceInput] = None

    def get_field(self) -> List[Trait]:
        traits = []
        for t in self.traits:
            name = t["name"]
            value = t["value"]
            trait = GLOBAL_TYPE_REGISTRY.get_field(name)
            if trait is None:
                trait = Trait.from_str(name).default_present(value)
            else:
                trait = trait.default_present(value)
            traits.append(trait)
        return traits

    @beartype
    def get_parsed(self) -> Union[DyType, IrNode]:
        for to_parse in [self.example, self.fields, self.source, self.variants]:
            if to_parse:
                parser = PARSER_REGISTRY.find_parser(to_parse)

                if parser is not None:
                    parsed = parser.parse(self.name, to_parse)
                    break
                else:
                    raise Exception(f"Could not find parser for {to_parse}")

        else:
            raise Exception(f"No invalid input for {self.name}")

        for t in self.get_field():
            parsed.append_field(t)
        return parsed


def read_model_definition(content: str) -> List[ModelDefinition]:
    defs = []
    segments = content.split("---")
    for seg in segments:
        seg = seg.strip()
        if not seg:
            continue

        data = yaml.safe_load(seg)
        if data is None:
            continue
        loaded_model = ModelDefinition.parse_obj(dict(data.items()))
        loaded_model.raw = seg
        defs.append(loaded_model)

    return defs

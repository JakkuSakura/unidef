from pydantic import BaseModel, PrivateAttr

from unidef.utils.typing import *


class InputDefinition(BaseModel):
    def __str__(self):
        s = super().__str__()
        return s[:100]


class ExampleInput(InputDefinition):
    format: str
    text: str


class SourceInput(InputDefinition):
    lang: str
    code: str


class FieldsInput(BaseModel):
    __root__: List[Dict[str, Any]]


class VariantsInput(BaseModel):
    __root__: List[Dict[str, Any]]

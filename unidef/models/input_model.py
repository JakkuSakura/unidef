from unidef.utils.typing import *
from pydantic import BaseModel


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


class FieldsInput(InputDefinition):
    __root__: List[Dict[str, Any]]


class VariantsInput(InputDefinition):
    __root__: List[Dict[str, Any]]

    @property
    def variants(self):
        return self.__root__

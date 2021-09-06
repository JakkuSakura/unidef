from pydantic import BaseModel, PrivateAttr

from unidef.utils.typing_compat import *


class Definition(BaseModel):
    def __str__(self):
        s = super().__str__()
        return s[:100]


class ModelExample(Definition):
    format: str
    text: str


class SourceExample(Definition):
    lang: str
    code: str


class Fields(BaseModel):
    __root__: List[Dict[str, Any]]


class Variants(BaseModel):
    __root__: List[Dict[str, Any]]

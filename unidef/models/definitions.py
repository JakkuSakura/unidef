from pydantic import BaseModel
from unidef.utils.typing_compat import *

class Definition(BaseModel):
    pass


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


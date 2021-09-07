from enum import Enum

from pydantic import BaseModel

from unidef.models.input_model import InputDefinition
from unidef.models.type_model import Type


class Parser:
    def accept(self, fmt: InputDefinition) -> bool:
        raise NotImplementedError()

    def parse(self, name: str, fmt: InputDefinition) -> Type:
        raise NotImplementedError()

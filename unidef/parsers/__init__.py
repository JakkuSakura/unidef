from enum import Enum

from pydantic import BaseModel

from unidef.models.input_model import InputDefinition
from unidef.languages.common.type_model import DyType


class Parser:
    def accept(self, fmt: InputDefinition) -> bool:
        raise NotImplementedError()

    def parse(self, name: str, fmt: InputDefinition) -> DyType:
        raise NotImplementedError()

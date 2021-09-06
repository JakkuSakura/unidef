from enum import Enum

from pydantic import BaseModel

from unidef.models.definitions import Definition
from unidef.models.type_model import Type


class Parser:
    def accept(self, fmt: Definition) -> bool:
        raise NotImplementedError()

    def parse(self, name: str, fmt: Definition) -> Type:
        raise NotImplementedError()

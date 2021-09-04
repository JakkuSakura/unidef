from unidef.models.type_model import Type
from unidef.models.definitions import Definition
from pydantic import BaseModel
from enum import Enum


class Parser:

    def accept(self, fmt: Definition) -> bool:
        raise NotImplementedError()

    def parse(self, name: str, fmt: Definition) -> Type:
        raise NotImplementedError()

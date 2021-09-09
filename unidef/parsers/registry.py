import logging
import sys
from enum import Enum

from beartype import beartype

from unidef.models.input_model import InputDefinition
from unidef.models.type_model import DyType
from unidef.parsers import Parser
from unidef.utils.loader import load_module
from unidef.utils.typing import Optional


class ParserRegistry:
    def __init__(self):
        self.parsers: List[Parser] = []

    def add_parser(self, parser: Parser):
        self.parsers.append(parser)

    def find_parser(self, fmt: InputDefinition) -> Optional[Parser]:
        for p in self.parsers:
            if p.accept(fmt):
                return p


PARSER_REGISTRY = ParserRegistry()


def add_parser(name: str, emitter: str):
    module = load_module(f"unidef.parsers.{name}")
    if module:
        PARSER_REGISTRY.add_parser(module.__dict__[emitter]())


add_parser("json_parser", "JsonParser")
add_parser("fields_parser", "FieldsParser")
add_parser("variants_parser", "VariantsParser")
add_parser("javascript_parser", "JavascriptParser")
add_parser("fix_parser", "FixParser")

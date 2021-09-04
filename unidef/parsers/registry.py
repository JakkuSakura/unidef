import sys
import logging
from beartype import beartype
from unidef.models.type_model import Type
from unidef.models.definitions import Definition
from unidef.utils.typing_compat import Optional
from unidef.utils.loader import load_module
from unidef.parsers import Parser
from enum import Enum


class ParserRegistry:
    def __init__(self):
        self.parsers: List[Parser] = []

    def add_parser(self, parser: Parser):
        self.parsers.append(parser)

    def find_parser(self, fmt: Definition) -> Optional[Parser]:
        for p in self.parsers:
            if p.accept(fmt):
                return p


PARSER_REGISTRY = ParserRegistry()


def add_parser(name: str, emitter: str):
    module = load_module(f'unidef.parsers.{name}')
    if module:
        PARSER_REGISTRY.add_parser(module.__dict__[emitter]())


add_parser('json_parser', 'JsonParser')
add_parser('fields_parser', 'FieldsParser')
add_parser('variants_parser', 'VariantsParser')
add_parser('javascript_parser', 'JavascriptParser')
add_parser('fix_parser', 'FixParser')

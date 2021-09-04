import sys
import logging
from beartype import beartype
from unidef.models.type_model import Type
from unidef.models.definitions import Definition
from unidef.utils.typing_compat import Optional
from unidef.parsers import Parser
from unidef.parsers.json_parser import JsonParser
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

PARSER_REGISTRY.add_parser(JsonParser())
try:
    from unidef.parsers.fix_parser import FixParser

    PARSER_REGISTRY.add_parser(FixParser())
except Exception as e:
    logging.warning('Does not support fix parser %s', e)

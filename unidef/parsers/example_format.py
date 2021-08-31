import sys
import logging
from beartype import beartype
from unidef.models.type_model import Type
from unidef.utils.typing_compat import Optional
from unidef.parsers import ApiParser
from unidef.parsers.json_parser import JsonParser


class ExampleFormatRegistry:
    def __init__(self):
        self.api_parsers: List[ApiParser] = []

    def add_api_parser(self, parser: ApiParser):
        self.api_parsers.append(parser)

    def find_parser(self, fmt: str) -> Optional[ApiParser]:
        for p in self.api_parsers:
            if p.accept(fmt):
                return p


EXAMPLE_FORMAT_REGISTRY = ExampleFormatRegistry()

EXAMPLE_FORMAT_REGISTRY.add_api_parser(JsonParser())
try:
    from unidef.parsers.fix_parser import FixParser

    EXAMPLE_FORMAT_REGISTRY.add_api_parser(FixParser())
except Exception as e:
    logging.warning('Does not support fix parser %s', e)

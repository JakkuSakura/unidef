import sys
import logging
from beartype import beartype
from models.type_model import Type
from utils.typing_compat import Optional
from parsers import ApiParser
from parsers.json_parser import JsonParser


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
    from parsers.fix_parser import FixParser

    EXAMPLE_FORMAT_REGISTRY.add_api_parser(FixParser())
except Exception as e:
    logging.warning('Does not support fix parser %s', e)

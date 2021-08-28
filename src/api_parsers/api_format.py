from beartype import beartype
from models.type_model import Type
from typing import Optional
from api_parsers import ApiParser
from api_parsers.json_parser import JsonParser
from api_parsers.fix_parser import FixParser


class ApiFormatRegistry:
    def __init__(self):
        self.api_parsers: List[ApiParser] = []

    def add_api_parser(self, parser: ApiParser):
        self.api_parsers.append(parser)

    def find_parser(self, fmt: str) -> Optional[ApiParser]:
        for p in self.api_parsers:
            if p.accept(fmt):
                return p


API_FORMAT_REGISTRY = ApiFormatRegistry()

API_FORMAT_REGISTRY.add_api_parser(JsonParser())
API_FORMAT_REGISTRY.add_api_parser(FixParser())

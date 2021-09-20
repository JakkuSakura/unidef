import json
import os
import sys
import unicodedata

from unidef.models.input_model import InputDefinition, ExampleInput
from unidef.languages.common.type_model import *
from unidef.parsers import Parser
from unidef.utils.loader import load_module


class FixParser(Parser):
    BASE_DIR = "quickfix"

    def accept(self, fmt: InputDefinition) -> bool:
        return (
            isinstance(fmt, ExampleInput)
            and fmt.format.lower().startswith("fix")
            and load_module("quickfix")
        )

    def parse(self, name: str, fmt: ExampleInput) -> DyType:
        from unidef.languages.fix.fix_parser import FixParserImpl

        return FixParserImpl().parse(name, fmt)

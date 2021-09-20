import re
import unicodedata

import pyhocon

from unidef.models.input_model import InputDefinition, VariantsInput
from unidef.languages.common.type_model import *
from unidef.parsers import Parser
from unidef.utils.typing import *


class VariantsParser(Parser):
    def accept(self, fmt: InputDefinition) -> bool:
        return isinstance(fmt, VariantsInput)

    def parse(self, name: str, fmt: InputDefinition) -> DyType:
        assert isinstance(fmt, VariantsInput)
        variants = []
        for var in fmt.variants:
            variants.append(Types.variant(var["name"].split()))
        return Types.enum(name, variants)

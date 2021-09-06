import re
import unicodedata

import pyhocon

from unidef.models.definitions import Definition, Variants
from unidef.models.type_model import *
from unidef.parsers import Parser
from unidef.utils.typing_compat import *


class VariantsParser(Parser):
    def accept(self, fmt: Definition) -> bool:
        return isinstance(fmt, Variants)

    def parse(self, name: str, fmt: Definition) -> Type:
        assert isinstance(fmt, Variants)
        variants = []
        for var in fmt.variants:
            variants.append(Types.variant(var["name"].split()))
        return Types.enum(name, variants)

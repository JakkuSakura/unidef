import json
import os
import sys
import unicodedata

from unidef.models.input_model import InputDefinition, ExampleInput
from unidef.models.type_model import *
from unidef.parsers import Parser
from unidef.utils.loader import load_module
import quickfix


class FixParserImpl(Parser):
    BASE_DIR = "quickfix"

    def accept(self, fmt: InputDefinition) -> bool:
        return (
            isinstance(fmt, ExampleInput)
            and fmt.format.lower().startswith("fix")
            and load_module("quickfix")
        )

    def get_dictionary(self, version: str) -> "quickfix.DataDictionary":
        if not os.path.exists(FixParserImpl.BASE_DIR):
            os.system(
                f"git clone https://github.com/quickfix/quickfix {FixParserImpl.BASE_DIR}"
            )
        return quickfix.DataDictionary(f"{FixParserImpl.BASE_DIR}/spec/{version}.xml")

    def parse(self, name: str, fmt: ExampleInput) -> DyType:
        content = fmt.text
        dct = self.get_dictionary(fmt.format)
        kvs = content.split("|")[:-1]
        fields = []
        for kv in kvs:
            k, v = kv.split("=")
            nm = dct.getFieldName(int(k), "")[0]
            try:
                ty = infer_type_from_example(json.loads(v))
            except:
                ty = Types.String
            fields.append(Types.field(nm, ty))
        return Types.struct(name, fields)

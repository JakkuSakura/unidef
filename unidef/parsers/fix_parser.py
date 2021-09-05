import json
import unicodedata
import os
import sys

from unidef.models.type_model import *
from unidef.models.definitions import ModelExample, Definition
from unidef.parsers import Parser
from unidef.utils.loader import load_module


class FixParser(Parser):
    BASE_DIR = 'quickfix'

    def accept(self, fmt: Definition) -> bool:
        return category == Category.EXAMPLE and fmt.lower().startswith('fix') and load_module('quickfix')

    def get_dictionary(self, version: str) -> 'quickfix.DataDictionary':
        if not os.path.exists(FixParser.BASE_DIR):
            os.system(f'git clone https://github.com/quickfix/quickfix {FixParser.BASE_DIR}')
        import quickfix
        return quickfix.DataDictionary(f'{FixParser.BASE_DIR}/spec/{version}.xml')

    def parse(self, name: str, fmt: ModelExample) -> Type:
        import quickfix
        content = fmt.text
        dct = self.get_dictionary(fmt.format)
        kvs = content.split('|')[:-1]
        fields = []
        for kv in kvs:
            k, v = kv.split('=')
            nm = dct.getFieldName(int(k), '')[0]
            try:
                ty = parse_data_example(json.loads(v))
            except:
                ty = Types.String
            fields.append(Types.field(nm, ty))
        return Types.struct(name, fields)

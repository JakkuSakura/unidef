import json
import unicodedata
import quickfix
import os
import sys

from models.type_model import *
from parsers import ApiParser


class FixParser(ApiParser):
    BASE_DIR = 'quickfix'

    def accept(self, fmt: str) -> bool:
        return fmt.lower().startswith('fix')

    def get_dictionary(self, version: str) -> quickfix.DataDictionary:
        if not os.path.exists(FixParser.BASE_DIR):
            os.system(f'git clone https://github.com/quickfix/quickfix {FixParser.BASE_DIR}')

        return quickfix.DataDictionary(f'{FixParser.BASE_DIR}/spec/{version}.xml')

    def parse(self, fmt: str, name: str, content: str) -> Type:
        dct = self.get_dictionary(fmt)
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

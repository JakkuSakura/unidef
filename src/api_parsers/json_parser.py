import unicodedata
import pyhocon
from models.type_model import *
from api_parsers.api_parser import ApiParser


class JsonParser(ApiParser):

    def accept(self, s: str) -> bool:
        return s.lower() == 'json'

    def parse(self, name: str, content: str) -> Type:
        content = unicodedata.normalize('NFKC', content)
        parsed = parse_data_example(dict(pyhocon.ConfigParser.parse(content)), name)

        if parsed.get_trait(Traits.Struct) and name:
            parsed.replace_trait(Traits.Name.init_with(name))
        return parsed

from unidef.parsers import Parser, Definition
from unidef.models.type_model import Type
from unidef.models.definitions import SourceExample


class JavascriptParser(Parser):
    def accept(self, fmt: Definition) -> bool:
        return isinstance(fmt, SourceExample) and fmt.lang == 'javascript'

    def parse(self, name: str, fmt: Definition) -> Type:
        raise NotImplementedError()

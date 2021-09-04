from unidef.parsers import Parser, Definition


class JavascriptParser(Parser):
    def accept(self, fmt: Definition) -> bool:
        return

    def parse(self, name: str, fmt: Definition) -> Type:
        raise NotImplementedError()

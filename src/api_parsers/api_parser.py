from models.type_model import Type


class ApiParser:

    def accept(self, s: str) -> bool:
        raise NotImplementedError()

    def parse(self, name: str, content: str) -> Type:
        raise NotImplementedError()

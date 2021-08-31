from models.type_model import Type


class ApiParser:

    def accept(self, fmt: str) -> bool:
        raise NotImplementedError()

    def parse(self, fmt: str, name: str, content: str) -> Type:
        raise NotImplementedError()

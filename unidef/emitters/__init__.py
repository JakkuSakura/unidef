from unidef.models.config_model import ModelDefinition
from unidef.models.type_model import Type


class Emitter:
    def accept(self, target: str) -> bool:
        raise NotImplementedError()

    def emit_type(self, target: str, ty: Type) -> str:
        raise NotImplementedError()

    def emit_model(self, target: str, model: ModelDefinition) -> str:
        raise NotImplementedError()

from unidef.models.config_model import ModelDefinition
from unidef.models.type_model import DyType


class Emitter:
    def accept(self, target: str) -> bool:
        raise NotImplementedError()

    def emit_type(self, target: str, ty: DyType) -> str:
        raise NotImplementedError()

    def emit_model(self, target: str, model: ModelDefinition) -> str:
        raise NotImplementedError()

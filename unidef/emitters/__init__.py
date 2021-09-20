from unidef.models.config_model import ModelDefinition


class Emitter:
    def accept(self, target: str) -> bool:
        raise NotImplementedError()

    def emit_type(self, target: str, ty) -> str:
        raise NotImplementedError()

    def emit_model(self, target: str, model: ModelDefinition) -> str:
        raise NotImplementedError()

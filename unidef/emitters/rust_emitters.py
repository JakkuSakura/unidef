from unidef.emitters import Emitter
from unidef.languages.common.type_model import DyType
from unidef.models.config_model import ModelDefinition


class RustDataEmitter(Emitter):
    def accept(self, s: str) -> bool:
        return s == "rust"

    def emit_model(self, target: str, model: ModelDefinition) -> str:
        from unidef.languages.rust.rust_data_emitter import \
            emit_rust_model_definition

        return emit_rust_model_definition(model)

    def emit_type(self, target: str, ty: DyType) -> str:
        from unidef.languages.rust.rust_data_emitter import emit_rust_type

        return emit_rust_type(ty)


class RustJsonEmitter(Emitter):
    def accept(self, target: str) -> bool:
        return "rust" in target and "json" in target

    def emit_model(self, target: str, model: ModelDefinition) -> str:
        return self.emit_type(target, model.get_parsed())

    def emit_type(self, target: str, ty: DyType) -> str:
        from unidef.languages.rust.rust_json_emitter import (
            RustFormatter, StructuredFormatter, get_json_crate)

        json_crate = get_json_crate(target)
        node = json_crate.transform(ty)
        formatter = RustFormatter()
        node = formatter.transform(node)
        formatter = StructuredFormatter(nodes=[node])

        return formatter.to_string(strip_left=True)


class RustLangEmitter(Emitter):
    def accept(self, s: str) -> bool:
        return s == "rust_lang"

    def emit_model(self, target: str, model: ModelDefinition) -> str:
        parsed = model.get_parsed()

        return self.emit_type(target, parsed)

    def emit_type(self, target: str, ty) -> str:
        from unidef.languages.rust.rust_lang_emitter import (
            RustEmitterBase, RustFormatter, StructuredFormatter, try_rustfmt)

        builder = RustEmitterBase()
        node = builder.transform(ty)
        formatter = RustFormatter()
        node = formatter.transform(node)
        node = try_rustfmt(str(node))
        return node

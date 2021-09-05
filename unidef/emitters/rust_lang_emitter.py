from unidef.utils.typing_compat import List

from pydantic import BaseModel

from unidef.utils.formatter import IndentedWriter, Formatee, Function, IndentBlock
from unidef.models import type_model, config_model
from unidef.models.type_model import Type, Traits
from unidef.models.transpile_model import Node, Attributes
from unidef.emitters import Emitter
from unidef.models.config_model import ModelDefinition


class RustBuilder:
    def build(self, node: Node) -> str:
        return str(node)


class RustLangEmitter(Emitter):
    def accept(self, s: str) -> bool:
        return s == 'rust_lang'

    def emit_model(self, target: str, model: ModelDefinition) -> str:
        parsed = model.get_parsed()
        assert isinstance(parsed, Node)
        return RustBuilder().build(parsed)

    def emit_type(self, target: str, ty: Type) -> str:
        return RustBuilder().build(parsed)

from unidef.utils.typing_compat import List

from pydantic import BaseModel

from unidef.utils.formatter import IndentedWriter, Formatee, Function, IndentBlock
from unidef.models import type_model, config_model
from unidef.models.type_model import Type, Traits
from unidef.emitters import Emitter
from unidef.models.config_model import ModelDefinition


class EmptyEmitter(Emitter):
    def accept(self, s: str) -> bool:
        return s == 'no_target'

    def emit_model(self, target: str, model: ModelDefinition) -> str:
        model.get_parsed()
        return ''

    def emit_type(self, target: str, ty: Type) -> str:
        return ''

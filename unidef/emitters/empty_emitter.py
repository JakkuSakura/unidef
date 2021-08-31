from utils.typing_compat import List

from pydantic import BaseModel

from utils.formatter import IndentedWriter, Formatee, Function, IndentBlock
from models import type_model, config_model
from models.type_model import Type, Traits
from emitters import Emitter
from models.config_model import ModelDefinition


class EmptyEmitter(Emitter):
    def accept(self, s: str) -> bool:
        return s == 'no_target'

    def emit_model(self, target: str, model: ModelDefinition) -> str:
        model.get_parsed()
        return ''

    def emit_type(self, target: str, ty: Type) -> str:
        return ''

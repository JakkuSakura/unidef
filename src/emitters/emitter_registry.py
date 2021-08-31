import sys

from beartype import beartype
from models.type_model import Type
from utils.typing_compat import Optional
from emitters import Emitter
from emitters.python_model import PythonEmitter
from emitters.rust_model import RustEmitter
from emitters.empty_emitter import EmptyEmitter
class EmitterRegistry:
    def __init__(self):
        self.emitters: List[ApiParser] = []

    def add_emitter(self, parser: Emitter):
        self.emitters.append(parser)

    def find_emitter(self, fmt: str) -> Optional[Emitter]:
        for p in self.emitters:
            if p.accept(fmt):
                return p


EMITTER_REGISTRY = EmitterRegistry()

EMITTER_REGISTRY.add_emitter(PythonEmitter())
EMITTER_REGISTRY.add_emitter(RustEmitter())
EMITTER_REGISTRY.add_emitter(EmptyEmitter())
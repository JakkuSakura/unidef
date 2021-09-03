import sys

from beartype import beartype
from unidef.models.type_model import Type
from unidef.utils.typing_compat import Optional
from unidef.emitters import Emitter
from unidef.emitters.python_model import PythonEmitter
from unidef.emitters.rust_model import RustEmitter
from unidef.emitters.empty_emitter import EmptyEmitter
from unidef.emitters.rust_json_emitter import RustJsonEmitter


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
EMITTER_REGISTRY.add_emitter(RustJsonEmitter())
EMITTER_REGISTRY.add_emitter(EmptyEmitter())

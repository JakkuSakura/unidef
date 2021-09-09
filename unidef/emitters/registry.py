import sys

from beartype import beartype

from unidef.emitters import Emitter
from unidef.models.type_model import DyType
from unidef.utils.loader import load_module
from unidef.utils.typing import Optional


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


def add_emitter(name: str, emitter: str):
    module = load_module(f"unidef.emitters.{name}")
    if module:
        EMITTER_REGISTRY.add_emitter(module.__dict__[emitter]())


add_emitter("python_data_emitter", "PythonDataEmitter")
add_emitter("rust_emitters", "RustDataEmitter")
add_emitter("rust_emitters", "RustJsonEmitter")
add_emitter("rust_emitters", "RustLangEmitter")
add_emitter("sql_model", "SqlEmitter")
add_emitter("empty_emitter", "EmptyEmitter")

import os
import sys

from unidef.emitters import Emitter
from unidef.utils.loader import load_module
from unidef.utils.typing import List, Optional


class EmitterRegistry:
    def __init__(self):
        self.emitters: List[Emitter] = []

    def add_emitter(self, parser: Emitter):
        self.emitters.append(parser)

    def find_emitter(self, fmt: str) -> Optional[Emitter]:
        for p in self.emitters:
            if p.accept(fmt):
                return p


EMITTER_REGISTRY = EmitterRegistry()


def add_emitter(name: str, emitter: str):
    module = load_module(name)
    if module:
        EMITTER_REGISTRY.add_emitter(module.__dict__[emitter]())


sys.path.append(os.path.dirname(os.path.abspath(__file__)))

add_emitter("python_emitters", "PythonPydanticEmitter")
add_emitter("python_emitters", "PythonPeeweeEmitter")
add_emitter("rust_emitters", "RustDataEmitter")
add_emitter("rust_emitters", "RustJsonEmitter")
add_emitter("rust_emitters", "RustLangEmitter")
add_emitter("sql_model", "SqlEmitter")
add_emitter("empty_emitter", "EmptyEmitter")

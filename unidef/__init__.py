import os.path
import sys

from unidef.emitters.emitter_registry import EMITTER_REGISTRY
from unidef.models.config_model import read_model_definition, ModelDefinition, ModelExample
from unidef.utils.typing_compat import *
import argparse

parser = argparse.ArgumentParser(description='define once, export everywhere')
parser.add_argument('--target', '-t', default='no_target', type=str, nargs='?', help='target format')
parser.add_argument('--format', '-f', type=str, nargs='?', help='input format')
parser.add_argument('file', default='/dev/stdin', type=str, nargs='?', help='input file')


def main(target: str, fmt: Optional[str], content: str, output: Callable[[str], None] = print):
    if fmt:
        model = ModelDefinition(name='stdin', example=ModelExample(format=fmt, text=content))
        models = [
            model
        ]
    else:
        models = read_model_definition(content)
    emitter = EMITTER_REGISTRY.find_emitter(target)
    if emitter is None:
        raise Exception(f'Could not find emitter for {target}')
    for loaded_model in models:
        output(emitter.emit_model(target, loaded_model))

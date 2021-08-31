#!/usr/bin/env python3
from emitters.emitter_registry import EMITTER_REGISTRY
from models.config_model import read_model_definition
from utils.typing_compat import *
import argparse

parser = argparse.ArgumentParser(description='define once, export everywhere')
parser.add_argument('--target', '-t', type=str, nargs='?', help='target format')
parser.add_argument('file', type=str, help='input file')


def main(target: Optional[str], content: str):
    target = target or 'no_target'
    models = read_model_definition(content)
    emitter = EMITTER_REGISTRY.find_emitter(target)
    if emitter is None:
        raise Exception(f'Could not find emitter for {target}')
    for loaded_model in models:
        print(emitter.emit_model(target, loaded_model))


if __name__ == '__main__':
    args = parser.parse_args()
    main(args.target, open(args.file).read())

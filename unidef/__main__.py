import sys
import logging


import argparse
import os.path

from beartype import beartype
from pydantic import BaseModel

from unidef.emitters.registry import EMITTER_REGISTRY
from unidef.models.config_model import ModelDefinition, read_model_definition
from unidef.models.input_model import *
from unidef.utils.typing import *

parser = argparse.ArgumentParser(description="define once, export everywhere")
parser.add_argument(
    "--target", "-t", default="no_target", type=str, nargs="?", help="target format"
)
parser.add_argument("--format", "-f", type=str, nargs="?", help="input format")
parser.add_argument("--lang", "-l", type=str, nargs="?", help="input language")
parser.add_argument(
    "file", default="/dev/stdin", type=str, nargs="?", help="input file"
)


class CommandLineConfig(BaseModel):
    target: str
    format: Optional[str]
    lang: Optional[str]
    file: str

    @classmethod
    def from_args(cls, args, **kwargs) -> __qualname__:
        args = dict(
            target=args.target, lang=args.lang, format=args.format, file=args.file
        )
        args.update(kwargs)
        return CommandLineConfig.parse_obj(args)


@beartype
def main(
    config: CommandLineConfig, content: str, output: Callable[[str], None] = print
):
    logging.basicConfig(stream=sys.stderr, level=logging.INFO)

    if config.format or config.lang:
        if config.format:
            key = "example"
            value = ExampleInput(format=config.format, text=content)
        elif config.lang:
            key = "source"
            value = SourceInput(lang=config.lang, code=content)
        else:
            raise Exception("Must specify either format or lang")
        model = ModelDefinition(name="stdin", **{key: value})
        models = [model]
    else:
        models = read_model_definition(content)
    emitter = EMITTER_REGISTRY.find_emitter(config.target)
    if emitter is None:
        raise Exception(f"Could not find emitter for {config.target}")
    for loaded_model in models:
        output(emitter.emit_model(config.target, loaded_model))


if __name__ == "__main__":
    args = parser.parse_args()
    config = CommandLineConfig.from_args(args)
    main(config, open(args.file).read())

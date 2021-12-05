import os
from unidef.__main__ import *


def test_parse_model():
    for target in ['rust']:
        args = parser.parse_args(['examples/model_example.yaml', '-t', target])
        config = CommandLineConfig.from_args(args)
        main(config, open(args.file).read())

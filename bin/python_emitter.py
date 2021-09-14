#!/usr/bin/env python3
import logging
import sys
import os

logging.basicConfig(stream=sys.stderr)
sys.path.append(os.path.dirname(os.path.dirname(__file__)))

from unidef.__main__ import *

if __name__ == '__main__':
    logging.warning('using this wrapper is deprecated, use unidef_emitter instead')
    args = parser.parse_args()
    config = CommandLineConfig.from_args(args, target='python_peewee')

    main(config, open(args.file).read())

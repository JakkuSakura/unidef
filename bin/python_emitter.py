#!/usr/bin/env python3
import logging
from unidef import *
import sys

if __name__ == '__main__':
    logging.warning('using this wrapper is deprecated, use unidef_emitter instead')
    args = parser.parse_args()
    config = CommandLineConfig.from_args(args, target='python_peewee')

    main(config, open(args.file).read())

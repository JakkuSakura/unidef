#!/usr/bin/env python3
import logging
import os.path
import sys

logging.basicConfig(stream=sys.stderr, level=logging.INFO)
sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))

from unidef.__main__ import CommandLineConfig, main, parser

if __name__ == '__main__':
    args = parser.parse_args()
    config = CommandLineConfig.from_args(args)
    main(config, open(args.file).read())

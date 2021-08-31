#!/usr/bin/env python3
import logging
import os.path
import sys

logging.basicConfig(stream=sys.stderr)
sys.path.append(os.path.dirname(os.path.dirname(os.path.realpath(__file__))))

from unidef import parser, main

if __name__ == '__main__':
    args = parser.parse_args()
    main(args.target, args.format, open(args.file).read())

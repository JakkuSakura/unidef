#!/usr/bin/env python3
import logging

from unidef_emitter import *
import sys

if __name__ == '__main__':
    logging.warning('using this wrapper is deprecated, use unidef_emitter instead')
    args = parser.parse_args()
    main('python_peewee', args.format, open(args.file).read())

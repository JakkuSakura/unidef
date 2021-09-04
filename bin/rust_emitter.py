#!/usr/bin/env python3

from unidef_emitter import *
import sys

if __name__ == '__main__':
    logging.warning('using this wrapper is deprecated, use unidef_emitter instead')
    args = parser.parse_args()
    main('rust', args.format, open(args.file).read())

#!/usr/bin/env python3

from unidef import *
import sys

if __name__ == '__main__':
    args = parser.parse_args()
    main('python', open(args.file).read())

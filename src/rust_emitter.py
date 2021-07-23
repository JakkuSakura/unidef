#!/usr/bin/env python3

import argparse

from models.config_model import read_model_definition
from models.rust_model import emit_rust_model_definition


def main():
    parser = argparse.ArgumentParser(description='Export rust source code')
    parser.add_argument('file', type=str, help='input file')

    args = parser.parse_args()

    for loaded_model in read_model_definition(open(args.file)):
        print(emit_rust_model_definition(loaded_model))


if __name__ == '__main__':
    main()

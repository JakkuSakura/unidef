#!/usr/bin/env python3

import argparse
import sys

import yaml

from models.config_model import ModelExample, ExampleFormat, read_model_definition


def main():
    parser = argparse.ArgumentParser(description='Parse json example')
    parser.add_argument('file', type=str, help='input file', nargs='?')

    args = parser.parse_args()

    if not args.file:
        data = sys.stdin.read()
        example = ModelExample(ExampleFormat.JSON, data)
        print(yaml.dump(example.get_parsed().dict()))
    else:
        for loaded_model in read_model_definition(open(args.file)):
            print(yaml.dump(loaded_model.get_parsed().dict()))


if __name__ == '__main__':
    main()

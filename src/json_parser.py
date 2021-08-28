#!/usr/bin/env python3

import argparse
import sys

import yaml

from models.config_model import ModelExample, read_model_definition


def main():
    parser = argparse.ArgumentParser(description='Parse json example')
    parser.add_argument('file', type=str, help='input file', nargs='?')

    args = parser.parse_args()

    if args.file:
        for loaded_model in read_model_definition(open(args.file)):
            print(yaml.dump(loaded_model.get_parsed().dict()))
    else:
        data = sys.stdin.read()
        example = ModelExample(format='json', text=data)
        print(yaml.dump(example.get_parsed().dict()))


if __name__ == '__main__':
    main()

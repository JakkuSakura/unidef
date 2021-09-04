import importlib
import logging


def load_module(name: str):
    try:
        return importlib.import_module(name)
    except Exception as e:
        logging.warning('Could not load module %s %s, skipping', name, e)

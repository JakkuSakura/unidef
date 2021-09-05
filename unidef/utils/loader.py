import importlib
import logging
import traceback


def load_module(name: str):
    try:
        return importlib.import_module(name)
    except Exception as e:
        logging.warning('Could not load module %s %s %s, skipping', name, type(e), e)
        if not isinstance(e, ImportError):
            traceback.print_exc()


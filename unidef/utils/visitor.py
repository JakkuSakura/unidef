from unidef.utils.typing_compat import *


class VisitorPattern:
    def get_functions(self, prefix: str):
        functions = []
        for key in dir(self):
            value = getattr(self, key)
            if isinstance(value, Callable) and key.startswith(prefix):
                node_name = key[len(prefix):]
                functions.append((node_name, value))
        functions.sort(key=lambda x: len(x[0]), reverse=True)
        return functions

from unidef.utils.typing import *


class VisitorPattern:
    def get_functions(self, prefix: str) -> List[Tuple[str, Callable]]:
        functions = []
        for key in dir(self):
            if key.startswith(prefix):
                value = getattr(self, key)
                if isinstance(value, Callable):
                    node_name = key[len(prefix) :]
                    functions.append((node_name, value))
        functions.sort(key=lambda x: len(x[0]), reverse=True)
        return functions

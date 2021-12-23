from unidef.utils.name_convert import *
from unidef.utils.transformer import FuncNodeTransformer, NodeTransformer
from unidef.utils.typing import *


class VisitorPattern:
    def get_functions(self, prefix: str, acceptor=None) -> List[NodeTransformer]:
        if acceptor is None:

            def acceptor(this, node):
                return to_snake_case(type(node).__name__) == this.target_name

        functions = []
        for key in dir(self):
            if key.startswith(prefix):
                value = getattr(self, key)
                if isinstance(value, Callable):
                    node_name = key[len(prefix) :]

                    functions.append(
                        FuncNodeTransformer(
                            target_name=node_name, func=value, acceptor=acceptor
                        )
                    )
        functions.sort(key=lambda x: len(x.target_name), reverse=True)
        return functions

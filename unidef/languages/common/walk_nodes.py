from unidef.models.ir_model import *
from unidef.utils.typing import *


def walk_nodes(node: IrNode, foreach: Callable[[IrNode]]):
    foreach(node)
    for key in node.keys():
        value = node.get_field(Attribute(key=key))
        if isinstance(value, list):
            for v in value:
                if isinstance(v, IrNode):
                    walk_nodes(v, foreach)
        elif isinstance(value, IrNode):
            walk_nodes(value, foreach)

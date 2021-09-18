from unidef.utils.typing import *
from unidef.models.type_model import *
from unidef.models.ir_model import *
from networkx import DiGraph
from unidef.languages.common.type_inference.scope import *


class AssignType:
    @abstractmethod
    def assign_type(self, ty: DyType, **kwargs):
        pass


class AssignTypeFunc(AssignType):
    def __init__(self, f: Callable[[DyType, Dict[str, Any]]]):
        self.func = f

    def assign_type(self, ty: DyType, **kwargs):
        self.func(ty, **kwargs)


class Blackboard:
    def __init__(self):
        self.inferred_cache: Dict[str, DyType] = {}
        self.assign_type: Dict[str, List[AssignType]] = {}
        self.node_mapping: Dict[str, IrNode] = {}
        self.graph = DiGraph()
        self.reverse_graph = DiGraph()

    @beartype
    def add_inferred(self, path: str, inferred: DyType):
        self.inferred_cache[path] = inferred
        for x in self.assign_type.get(path) or []:
            x.assign_type(inferred)

    @beartype
    def get_inferred(self, path: str) -> Optional[DyType]:
        return self.inferred_cache.get(path)

    def add_edge(self, node: AdvancedIrNode, rely_on: AdvancedIrNode, **kwargs):
        self.add_node(node.node_path, node.node)
        self.add_node(rely_on.node_path, rely_on.node)
        self.graph.add_edge(node.node_path, rely_on.node_path, **kwargs)

    def add_node(self, path: str, node: IrNode):
        self.node_mapping[path] = node

    def add_assign_type(self, path: str, ass: AssignType):
        if path not in self.assign_type:
            self.assign_type[path] = []
        self.assign_type[path].append(ass)

    def get_node(self, path: str) -> Optional[AdvancedIrNode]:
        node = self.node_mapping.get(path)
        if node is not None:
            return AdvancedIrNode(node, path)
        return None

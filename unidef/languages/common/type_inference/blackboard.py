from unidef.utils.typing import *
from unidef.models.type_model import *
from unidef.models.ir_model import *
from networkx import DiGraph, Graph
from unidef.languages.common.type_inference.scope import *


@abstract
class PostAssignType:
    def post_assign_type(self, ty: DyType, **kwargs):
        return NotImplementedError()


class PostAssignTypeInferred(PostAssignType):
    def __init__(self, node: IrNode):
        self.node = node

    def post_assign_type(self, ty: DyType, **kwargs):
        self.node.append_field(Attributes.InferredType(ty))


class PostAssignTypeFunc(PostAssignType):
    def __init__(self, f: Callable[[DyType, Dict[str, Any]]]):
        self.func = f

    def post_assign_type(self, ty: DyType, **kwargs):
        self.func(ty, **kwargs)


class EdgeType(Enum):
    EQUAL_TO = "EQUAL_TO"


class TypeRelationBuilder:
    def __init__(self):
        self.graph = Graph()
        self.node_mapping: Dict[str, IrNode] = {}
        self.post_assign_type: Dict[str, List[PostAssignType]] = {}
        self.known_types: Dict[str, DyType] = {}

    def add_node(self, path: str, node: IrNode):
        self.node_mapping[path] = node

    def add_edge(
        self,
        node1: AdvancedIrNode,
        node2: AdvancedIrNode,
        edge_type: EdgeType = EdgeType.EQUAL_TO,
    ):
        self.add_node(node1.node_path, node1.node)
        self.add_node(node2.node_path, node2.node)
        self.graph.add_edge(node1.node_path, node2.node_path, edge_type=edge_type)

    def add_known(self, path: str, ty: DyType):
        ty = ty.copy().remove_field(Traits.RawValue).freeze()
        self.known_types[path] = ty

    def add_post_inferred_type(self, path: str, ass: PostAssignType):
        if path not in self.post_assign_type:
            self.post_assign_type[path] = []
        self.post_assign_type[path].append(ass)


@abstract
class NodeTypeProcessor:
    def __init__(self):
        pass

    def accept(self, node: IrNode) -> bool:
        raise NotImplementedError()

    def prepare_inference(
        self,
        node: IrNode,
        environment: Environment,
        scope: Scope,
        builder: TypeRelationBuilder,
    ) -> None:
        raise NotImplementedError()


class Blackboard:
    def __init__(self):
        self.inferred_cache: Dict[str, DyType] = {}
        self.post_assign_type: Dict[str, List[PostAssignType]] = {}
        self.node_mapping: Dict[str, IrNode] = {}
        self.graph = Graph()
        self.raw_relations: List[TypeRelationBuilder] = []

    @beartype
    def merge_builder(self, builder: TypeRelationBuilder):
        self.raw_relations.append(builder)
        self.inferred_cache.update(builder.known_types)
        for k, v in builder.post_assign_type.items():
            if k not in self.post_assign_type:
                self.post_assign_type[k] = []
            self.post_assign_type[k].extend(v)
        self.node_mapping.update(builder.node_mapping)
        self.graph.update(
            builder.graph.edges(data=True), builder.graph.nodes(data=True)
        )

    @beartype
    def add_inferred(self, path: str, inferred: DyType):
        self.inferred_cache[path] = inferred
        for x in self.post_assign_type.get(path) or []:
            x.post_assign_type(inferred)

    @beartype
    def get_inferred(self, path: str) -> Optional[DyType]:
        return self.inferred_cache.get(path)

    def add_edge(self, node: AdvancedIrNode, rely_on: AdvancedIrNode, **kwargs):
        self.add_node(node.node_path, node.node)
        self.add_node(rely_on.node_path, rely_on.node)
        self.graph.add_edge(node.node_path, rely_on.node_path, **kwargs)

    def add_node(self, path: str, node: IrNode):
        self.node_mapping[path] = node

    def add_post_inferred_type(self, path: str, ass: PostAssignType):
        if path not in self.post_assign_type:
            self.post_assign_type[path] = []
        self.post_assign_type[path].append(ass)

    def get_node(self, path: str) -> Optional[AdvancedIrNode]:
        node = self.node_mapping.get(path)
        if node is not None:
            return AdvancedIrNode(node, path)
        return None

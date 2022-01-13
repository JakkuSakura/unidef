from networkx import DiGraph, Graph

from unidef.languages.common.ir_model import *
from unidef.languages.common.type_inference.scope import *
from unidef.languages.common.type_model import *
from unidef.utils.typing_ext import *


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


class NodeGroup:
    def __init__(self, name, members: Dict[str, str], callback=None):
        self.name = name
        self.members = members
        self.callback = callback
        self.unified = False


class EqualTo:
    pass


class InferBy:
    def __init__(self, func):
        self.func = func


class TypeRelationBuilder:
    def __init__(self):
        self.node_mapping: Dict[str, IrNode] = {}
        self.post_assign_type: Dict[str, List[PostAssignType]] = {}
        self.known_types: Dict[str, DyType] = {}
        self.groups: List[NodeGroup] = []

    def add_node(self, path: str, node: IrNode):
        self.node_mapping[path] = node

    def add_edge(
        self,
        node1: AdvancedIrNode,
        node2: AdvancedIrNode,
    ):
        self.add_node(node1.node_path, node1.node)
        self.add_node(node2.node_path, node2.node)
        self.add_group(
            NodeGroup("equals_to", dict(n1=node1.node_path, n2=node2.node_path))
        )

    def add_group(self, group: NodeGroup):
        self.groups.append(group)

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
        self.raw_relations: List[TypeRelationBuilder] = []
        self.groups: List[NodeGroup] = []

    @beartype
    def merge_builder(self, builder: TypeRelationBuilder):
        self.raw_relations.append(builder)
        self.inferred_cache.update(builder.known_types)
        for k, v in builder.post_assign_type.items():
            if k not in self.post_assign_type:
                self.post_assign_type[k] = []
            self.post_assign_type[k].extend(v)
        self.node_mapping.update(builder.node_mapping)
        self.groups.extend(builder.groups)

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
            return AdvancedIrNode(path, node)
        return None

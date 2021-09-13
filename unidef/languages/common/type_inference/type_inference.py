from unidef.models.ir_model import *
from unidef.utils.transformer import *
from unidef.languages.common.walk_nodes import walk_nodes


class TypeInference(NodeTransformer[IrNode, IrNode]):
    decl: List[IrNode] = []
    inferred_new: int = 0

    def accept(self, node: IrNode) -> bool:
        return node.get_field(Attributes.FunctionDecl)

    def count_declarations(self, node: IrNode):
        if node.get_field(Attributes.VariableDeclaration):
            self.decl.append(node)

    def propagate_types(self, node: IrNode):
        print(node.get_field(Traits.Kind))

    def transform(self, node: IrNode) -> IrNode:
        walk_nodes(node, self.count_declarations)
        self.inferred_new = 0
        print("{} variable declarations".format(len(self.decl)))
        walk_nodes(node, self.propagate_types)
        return node

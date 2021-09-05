from unidef.models.type_model import Type, Trait, Traits
from beartype import beartype


class Attribute(Trait):
    pass


class Attributes:
    child = Attribute.from_str('child')
    statement = Attribute.from_str('statement')
    class_decl = Attribute.from_str('class_decl')
    while_loop = Attribute.from_str('while_loop')
    expression = Attribute.from_str('expression')
    function_call = Attribute.from_str('function_call')
    raw_code = Attribute.from_str('raw_code')
    callee = Attribute.from_str('callee')


class Node(Type):

    @staticmethod
    @beartype
    def from_str(name: str) -> 'Node':
        return Node().replace_trait(Traits.TypeName(name))


class Nodes:
    @staticmethod
    def print_node(content: 'Node') -> 'Node':
        return Node.from_str('print').append_trait(Attributes.child(content))

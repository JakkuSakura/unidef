from unidef.models.type_model import Type, Trait, Traits
from beartype import beartype
from unidef.utils.typing_compat import *
from unidef.utils.safelist import safelist
from pydantic import BaseModel


class Attribute(Trait):
    pass


class Attributes:
    Kind = Attribute.from_str('kind')
    Name = Attribute.from_str('name')
    Id = Attribute.from_str('id')
    Child = Attribute.from_str('child')
    Statement = Attribute.from_str('statement')
    ClassDecl = Attribute.from_str('class_declaration').default(True)
    SuperClass = Attribute.from_str('super_class')
    WhileLoop = Attribute.from_str('while_loop')
    Expression = Attribute.from_str('expression')
    FunctionCall = Attribute.from_str('function_call').default(True)
    FunctionDecl = Attribute.from_str('function_decl').default(True)
    RawCode = Attribute.from_str('raw_code')
    RawValue = Attribute.from_str('raw_value')
    Callee = Attribute.from_str('callee')
    Argument = Attribute.from_str('argument')
    ArgumentName = Attribute.from_str('argument_name')
    ArgumentType = Attribute.from_str('argument_type')
    Literal = Attribute.from_str('literal').default(True)
    Async = Attribute.from_str('async').default(True)
    Return = Attribute.from_str('return')

    Print = Attribute.from_str('print').default(True)
    Require = Attribute.from_str('require')


class Node(Type):
    @classmethod
    @beartype
    def from_str(cls, name: str) -> __qualname__:
        return cls().append_trait(Attributes.Kind(name))

    @classmethod
    @beartype
    def from_attribute(cls, attr: Attribute) -> __qualname__:
        return cls.from_str(attr.name).append_trait(attr)


class RequireNode(BaseModel):
    path: str
    key: Optional[str]
    value: Optional[str]


class Nodes:
    @staticmethod
    def print_node(content: Node) -> Node:
        return (
            Node.from_str(Attributes.Print.name)
                .append_trait(Attributes.Print)
                .append_trait(Attributes.Child(content))
        )

    @staticmethod
    def require_node(import_paths, import_name, raw: Node) -> Node:
        if isinstance(import_name, list):
            if isinstance(import_paths, list):
                zipped = [RequireNode(path=path, key=kv.get(0), value=kv.get(1)) for kv, path in
                          zip(map(safelist, import_name), import_paths)]
            else:
                zipped = [RequireNode(path=import_paths, key=kv.get(0), value=kv.get(1)) for kv in
                          map(safelist, import_name)]
        elif isinstance(import_name, tuple):
            zipped = [RequireNode(path=import_paths, key=import_name[0], value=import_name[1])]
        else:
            zipped = [RequireNode(path=import_paths, key=import_name)]
        return (
            Node.from_attribute(Attributes.Require)
                .extend_traits(Attributes.Require, zipped)
                .append_trait(Attributes.RawCode(raw))
        )

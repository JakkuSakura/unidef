from beartype import beartype
from pydantic import BaseModel, Field

from unidef.models.base_model import MyBaseModel, MyField
from unidef.models.type_model import Trait, Traits, Type
from unidef.utils.safelist import safelist
from unidef.utils.typing_compat import *


class Attribute(MyField):
    pass


class Attributes:
    Kind = Attribute(key="kind")
    Name = Attribute(key="name")
    Id = Attribute(key="id")
    Children = Attribute(key="children", default_present=[], default_absent=[])
    Statement = Attribute(key="statement")
    ClassDecl = Attribute(
        key="class_declaration", default_present=True, default_absent=False
    )
    SuperClasses = Attribute(key="super_class", default_present=[], default_absent=[])
    WhileLoop = Attribute(key="while_loop")
    Expression = Attribute(key="expression")
    FunctionCall = Attribute(
        key="function_call", default_present=True, default_absent=False
    )
    FunctionDecl = Attribute(
        key="function_decl", default_present=True, default_absent=False
    )
    RawCode = Attribute(key="raw_code")
    RawValue = Attribute(key="raw_value")
    Callee = Attribute(key="callee")
    Arguments = Attribute(key="argument", default_present=[], default_absent=[])
    ArgumentName = Attribute(key="argument_name")
    ArgumentType = Attribute(key="argument_type")
    Literal = Attribute(key="literal", default_present=True, default_absent=False)
    Async = Attribute(key="async", default_present=True, default_absent=False)
    Return = Attribute(key="return")
    # FIXME
    VarDecl = Attribute(key="declarations", default_present=[], default_absent=[])

    Print = Attribute(key="print", default_present=True, default_absent=False)
    Require = Attribute(key="require", default_present=[], default_absent=[])
    ObjectProperties = Attribute(
        key="object_expression", default_present=[], default_absent=[]
    )


class Node(MyBaseModel):
    @classmethod
    @beartype
    def from_str(cls, name: str) -> __qualname__:
        return cls().append_trait(Attributes.Kind(name))

    @classmethod
    @beartype
    def from_attribute(cls, attr: Attribute) -> __qualname__:
        return cls.from_str(attr.key).append_trait(attr)

    def append_trait(self, trait: Trait) -> __qualname__:
        return self.append_field(trait)

    def get_trait(self, trait: Trait) -> Any:
        return self.get_field(trait)

    def get_traits(self, trait: Trait) -> List[Any]:
        return self.get_field(trait)

    def extend_traits(self, field: MyField, values: List[Any]) -> __qualname__:
        return self.extend_field(field, values)


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
            .append_trait(Attributes.Children(content))
        )

    @staticmethod
    def require_node(import_paths, import_name, raw: Node) -> Node:
        if isinstance(import_name, list):
            if isinstance(import_paths, list):
                zipped = [
                    RequireNode(path=path, key=kv.get(0), value=kv.get(1))
                    for kv, path in zip(map(safelist, import_name), import_paths)
                ]
            else:
                zipped = [
                    RequireNode(path=import_paths, key=kv.get(0), value=kv.get(1))
                    for kv in map(safelist, import_name)
                ]
        elif isinstance(import_name, tuple):
            zipped = [
                RequireNode(path=import_paths, key=import_name[0], value=import_name[1])
            ]
        else:
            zipped = [RequireNode(path=import_paths, key=import_name)]
        return (
            Node.from_attribute(Attributes.Require)
            .extend_traits(Attributes.Require, zipped)
            .append_trait(Attributes.RawCode(raw))
        )

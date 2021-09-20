import copy

from unidef.languages.common.ir_model import *
from unidef.utils.transformer import *
from unidef.languages.common.walk_nodes import walk_nodes
from unidef.languages.common.type_model import infer_type_from_example
from unidef.utils.typing import *
from unidef.languages.common.type_model import *


class Scope:
    def __init__(self):
        self.function: Optional[FunctionEnv] = None
        self.clazz: Optional[ClassEnv] = None
        self.file: Optional[str] = None

    def copy(self) -> __qualname__:
        return copy.copy(self)


class GlobalNodePath:
    def __init__(self):
        self.nodes: List[str] = []

    @beartype
    def append_scope(self, scope: Scope):
        if scope.clazz:
            self.nodes.append(scope.clazz.name)
        if scope.function:
            self.nodes.append(scope.function.name)

    @beartype
    def append_path(self, path: str):
        self.nodes.append(path)

    def __str__(self):
        return ".".join(self.nodes)


class AdvancedIrNode:
    @beartype
    def __init__(self, path: str, node: Optional[IrNode] = None):
        self.node_path: str = path
        self.node: Optional[IrNode] = node

    def __str__(self):
        return self.node_path


class FunctionEnv:
    def __init__(self, name: str):
        self.name: str = name
        self.arguments: List[AdvancedIrNode] = []
        self.variables: List[AdvancedIrNode] = []

    def __str__(self):
        return "FunctionEnv{{name={}, arguments=[{}], variables=[{}]}}".format(
            self.name,
            ", ".join(
                [str(x.node.get_field(Attributes.ArgumentName)) for x in self.arguments]
            ),
            ", ".join(
                [
                    str(x.node.get_field(Attributes.VariableDeclarationId))
                    for x in self.variables
                ]
            ),
        )


class ClassEnv:
    def __init__(self, node: Optional[IrNode] = None):
        self.properties: List[IrNode] = []
        self.functions: List[FunctionEnv] = []
        self.node: Optional[IrNode] = node

    @property
    def name(self) -> str:
        return self.node.get_field(Attributes.Name)

    def get_function(self, node: IrNode) -> Optional[FunctionEnv]:
        name = node.get_field(Attributes.Name)
        for n in self.functions:
            if n.name == name:
                return n

    def __str__(self):
        return "ClassEnv{{name=[{}], properties=[{}], functions=[{}], node={}}}".format(
            self.name,
            ", ".join([str(x) for x in self.properties]),
            ", ".join([str(x) for x in self.functions]),
            id(self.node),
        )


class Environment:
    def __init__(self):
        self.classes: List[ClassEnv] = []

    @beartype
    def find_class(self, scope: Scope, clazz_name: str) -> Optional[ClassEnv]:
        if clazz_name == "this" or clazz_name == "self":
            return scope.clazz
        for cls in self.classes:
            if cls.name == clazz_name:
                return cls

    @beartype
    def find_declaration(self, node: IrNode, scope: Scope) -> Optional[AdvancedIrNode]:
        if node.get_field(Attributes.Identifier):
            id = node.get_field(Attributes.Identifier)
            for n in scope.function.variables:
                if n.node.get_field(Attributes.VariableDeclarationId) == id:
                    return n
            for n in scope.function.arguments:
                if n.node.get_field(Attributes.ArgumentName) == id:
                    return n

    @beartype
    def find_global_path(self, node: IrNode, scope: Scope) -> GlobalNodePath:
        path = GlobalNodePath()
        path.append_scope(scope)
        if node.get_field(Attributes.ThisExpression):
            path.append_path(scope.clazz.name)
            return path

        elif node.get_field(Attributes.Identifier):
            ident = node.get_field(Attributes.Identifier)
            path.append_path(ident)
            return path
        elif node.get_field(Attributes.VariableDeclaration):
            ident = node.get_field(Attributes.VariableDeclarationId)
            path.append_path(ident)
            return path
        else:
            path.append_path(f"{node.get_field(Attributes.Kind)}@{id(node)}")
            return path

    @beartype
    def get_advanced_ir_node(self, node: IrNode, scope: Scope) -> AdvancedIrNode:
        if node.get_field(Attributes.Identifier):
            decl = self.find_declaration(node, scope)
            assert decl is not None
            return decl
        path = self.find_global_path(node, scope)
        return AdvancedIrNode(str(path), node)

    def __str__(self):
        return "Environment{{classes=[{}]}}".format(
            ", ".join([str(x) for x in self.classes])
        )


class ScopePreparer(BaseModel):
    scope: Scope = Scope()
    environment: Environment = Environment()

    class Config:
        arbitrary_types_allowed = True

    def build_scope(self, node: IrNode):
        if node.get_field(Attributes.ClassDeclaration):
            self.scope.clazz = ClassEnv(node)
            self.environment.classes.append(self.scope.clazz)
        elif node.get_field(Attributes.FunctionDecl):
            self.scope.function = FunctionEnv(node.get_field(Attributes.Name))
            for arg in node.get_field(Attributes.Arguments):
                self.scope.function.arguments.append(
                    self.environment.get_advanced_ir_node(arg, self.scope.copy())
                )
            self.scope.clazz.functions.append(self.scope.function)
        elif node.get_field(Attributes.VariableDeclaration):
            self.scope.function.variables.append(
                self.environment.get_advanced_ir_node(node, self.scope.copy())
            )

    def with_scope(self, node: IrNode):
        if node.get_field(Attributes.ClassDeclaration):
            name = node.get_field(Attributes.Name)
            self.scope.clazz = self.environment.find_class(self.scope, name)
        elif node.get_field(Attributes.FunctionDecl):
            self.scope.function = self.scope.clazz.get_function(node)

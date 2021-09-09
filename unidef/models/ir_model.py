from beartype import beartype
from pydantic import BaseModel, Field

from unidef.models.base_model import MyBaseModel, MyField
from unidef.models.type_model import Trait, Traits, DyType
from unidef.utils.safelist import safelist
from unidef.utils.typing import *


class Attribute(MyField):
    pass


class Attributes:
    Kind = Attribute(key="kind")
    Name = Attribute(key="name")
    Id = Attribute(key="id")
    Children = Attribute(key="children", default_present=[], default_absent=[])
    Statement = Attribute(key="statement")
    ClassDeclaration = Attribute(
        key="class_declaration", default_present=True, default_absent=False
    )
    SuperClasses = Attribute(key="super_class", default_present=[], default_absent=[])

    WhileLoop = Attribute(key="while_loop")

    CForLoop = Attribute(key="c_for_loop", default_present=True, default_absent=False)
    CForLoopInit = Attribute(key="c_for_loop_init")
    CForLoopTest = Attribute(key="c_for_loop_test")
    CForLoopUpdate = Attribute(key="c_for_loop_update")

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
    DefaultValue = Attribute(key="default_value")
    Literal = Attribute(key="literal", default_present=True, default_absent=False)
    Async = Attribute(key="async", default_present=True, default_absent=False)
    Return = Attribute(key="return", allow_none=True)

    VariableDeclarations = Attribute(
        key="variable_declarations", default_present=[], default_absent=[]
    )
    VariableDeclaration = Attribute(
        key="variable_declaration", default_present=True, default_absent=False
    )

    Print = Attribute(key="print", default_present=True, default_absent=False)
    Requires = Attribute(key="requires", default_present=[], default_absent=[])
    Require = Attribute(key="require", default_present=[], default_absent=[])
    RequirePath = Attribute(key="require_path", default_present="", default_absent="")
    RequireKey = Attribute(key="require_key", default_present="", default_absent="")
    RequireValue = Attribute(key="require_value", default_present="", default_absent="")
    ObjectProperties = Attribute(
        key="object_properties", default_present=[], default_absent=[]
    )
    ObjectProperty = Attribute(
        key="object_property", default_present=True, default_absent=False
    )
    KeyName = Attribute(key="key")
    Value = Attribute(key="value")
    ArrayElements = Attribute(
        key="array_elements", default_present=[], default_absent=[]
    )

    TestExpression = Attribute(key="test_expression")
    IfClauses = Attribute(key="if_clauses", default_present=True, default_absent=False)
    IfClause = Attribute(key="if_clause", default_present=True, default_absent=False)
    ElseIfClause = Attribute(
        key="else_if_clause", default_present=True, default_absent=False
    )
    ElseClause = Attribute(
        key="else_clause", default_present=True, default_absent=False
    )

    BlockStatement = Attribute(
        key="block_statement", default_present=True, default_absent=False
    )
    Consequence = Attribute(key="consequence")
    Alternative = Attribute(key="alternative")
    Operator = Attribute(key="operator", default_present="", default_absent="")
    OperatorLeft = Attribute(key="operator_left")
    OperatorMiddle = Attribute(key="operator_middle")
    OperatorRight = Attribute(key="operator_right")
    OperatorSinglePrefix = Attribute(key="operator_single_prefix")
    OperatorSinglePostfix = Attribute(key="operator_single_postfix")

    MemberExpression = Attribute(
        key="member_expression", default_present=True, default_absent=False
    )
    MemberExpressionObject = Attribute(key="member_expression_object")
    MemberExpressionProperty = Attribute(key="member_expression_property")

    Identifier = Attribute(key="identifier", default_present="", default_absent="")
    ThisExpression = Attribute(
        key="this_expression", default_present=True, default_absent=False
    )
    SuperExpression = Attribute(
        key="super_expression", default_present=True, default_absent=False
    )

    Directive = Attribute(key="directive")

    Program = Attribute(key="program", default_present=True, default_absent=False)

    AssignExpression = Attribute(
        key="assign_expression", default_present=True, default_absent=False
    )
    AssignExpressionLeft = Attribute(key="assign_expression_left")
    AssignExpressionRight = Attribute(key="assign_expression_right")

    AwaitExpression = Attribute(key="await_expression")
    ThrowStatement = Attribute(key="throw_statement")
    NewExpression = Attribute(
        key="new_expression", default_present=True, default_absent=False
    )
    ConditionalExpression = Attribute(
        key="conditional_expression", default_present=True, default_absent=False
    )
    BreakStatement = Attribute(
        key="break_statement", default_present=True, default_absent=False
    )
    ContinueStatement = Attribute(
        key="continue_statement", default_present=True, default_absent=False
    )

    TryStatement = Attribute(key="try_statement", default_present=[], default_absent=[])
    CatchClauses = Attribute(key="catch_clauses", default_present=[], default_absent=[])
    CatchClause = Attribute(
        key="catch_clause", default_present=True, default_absent=False
    )
    FinallyClause = Attribute(
        key="finally_statement", default_present=[], default_absent=[]
    )


class IrNode(MyBaseModel):
    @classmethod
    @beartype
    def from_str(cls, name: str) -> __qualname__:
        return cls().append_field(Attributes.Kind(name))

    @classmethod
    @beartype
    def from_attribute(cls, attr: Attribute) -> __qualname__:
        return cls.from_str(attr.key).append_field(attr)


class Nodes:
    @staticmethod
    def print(content: IrNode) -> IrNode:
        return IrNode.from_attribute(Attributes.Print).append_field(
            Attributes.Children(content)
        )

    @staticmethod
    def require(path, key=None, value=None) -> IrNode:
        n = IrNode.from_attribute(Attributes.Require)
        n.append_field(Attributes.RequirePath(path))
        if key:
            n.append_field(Attributes.RequireKey(key))
        if value:
            n.append_field(Attributes.RequireValue(value))
        return n

    @staticmethod
    def requires(import_paths, import_name) -> IrNode:
        if isinstance(import_name, list):
            if isinstance(import_paths, list):
                requires_result = [
                    Nodes.require(path=path, key=kv.get(0), value=kv.get(1))
                    for kv, path in zip(map(safelist, import_name), import_paths)
                ]
            else:
                requires_result = [
                    Nodes.require(path=import_paths, key=kv.get(0), value=kv.get(1))
                    for kv in map(safelist, import_name)
                ]
        elif isinstance(import_name, tuple):
            requires_result = [
                Nodes.require(
                    path=import_paths, key=import_name[0], value=import_name[1]
                )
            ]
        else:
            requires_result = [Nodes.require(path=import_paths, key=import_name)]
        return IrNode.from_attribute(Attributes.Requires(requires_result))

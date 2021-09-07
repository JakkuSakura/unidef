import json
import logging
import traceback

from beartype import beartype

from unidef.models.input_model import SourceInput
from unidef.models.ir_model import Attribute, Attributes, Node, Nodes
from unidef.models.type_model import Traits, Type, Types
from unidef.parsers import InputDefinition, Parser
from unidef.utils.loader import load_module
from unidef.utils.name_convert import *
from unidef.utils.typing import *
from unidef.utils.visitor import VisitorPattern
from unidef.utils.transformer import NodeTransformer


class VisitorBase(NodeTransformer[Any, Type], VisitorPattern):
    functions: Any = None

    def get_recursive(self, obj: Dict, path: str) -> Any:
        for to_visit in path.split("."):
            obj = obj.get(to_visit)
            if obj is None:
                return None
        return obj

    def get_name(
            self, node, member_expression=False, warn=True
    ) -> Union[List[str], List[(str, str)], Optional[str]]:
        if node is None:
            return
        ty = node.get("type")
        if member_expression and ty == "MemberExpression":
            first = self.get_name(
                node.get("object"), warn=warn, member_expression=member_expression
            )
            second = self.get_name(
                node.get("property"), warn=warn, member_expression=member_expression
            )
            return ".".join([x for x in [first, second] if x])
        elif ty == "ThisExpression":
            return "this"
        elif ty == "Super":
            return "super"
        elif ty == "Identifier":
            return node.get("name")
        elif ty == "ObjectPattern":
            properties = node["properties"]
            names = []
            for prop in properties:
                key = prop["key"]
                value = prop["value"]
                names.append(
                    (
                        self.get_name(
                            key, warn=warn, member_expression=member_expression
                        ),
                        self.get_name(
                            value, warn=warn, member_expression=member_expression
                        ),
                    )
                )
            return names
        elif node.get("name"):
            return node.get("name")
        else:
            if warn:
                logging.warning("could not get name from %s", node)
            return

    def match_func_call(self, node, name: str) -> bool:
        if node["type"] != "CallExpression":
            return False
        spt = tuple(name.split("."))
        try:
            if len(spt) == 2:
                obj, method = spt
                callee = node["callee"]
                obj0 = self.get_name(callee, member_expression=True)
                if (
                        obj0 == obj
                        and self.get_name(callee.get("property"), member_expression=True)
                        == method
                ):
                    return True
            elif len(spt) == 1:
                (obj,) = spt
                callee = node["callee"]
                obj0 = self.get_name(callee, member_expression=True)
                return obj0 == obj
        except KeyError as e:
            print(node)
            traceback.print_exc()

        return False

    def transform_program(self, node) -> Node:
        program = Node.from_str("program")
        body = self.transform_node(node["body"]) or []
        program.append_field(Attributes.Children(body))
        return program

    def transform_member_expression(self, node) -> Node:
        n = Node.from_attribute(Attributes.MemberExpression)
        n.append_field(
            Attributes.MemberExpressionObject(self.transform_node(node["object"]))
        )
        n.append_field(
            Attributes.MemberExpressionProperty(self.transform_node(node["property"]))
        )
        return n

    def transform_other(self, node) -> Node:
        if node.get("type"):
            n = Node.from_str(node.pop("type"))
        else:
            n = Node.from_str(node.pop("name"))

        for key, value in node.items():
            value = self.transform_node(value)
            default = [] if isinstance(value, list) else None
            attr = Attribute(key=key, default_present=default)(value)
            if n.exist_field(attr):
                attr.key = key + "_"

            n.append_field(attr)
        return n

    def transform_literal(self, node) -> Node:
        return (
            Node.from_attribute(Attributes.Literal)
                .append_field(Attributes.RawCode(node["raw"]))
                .append_field(Attributes.RawValue(node["value"]))
        )

    def transform_identifier(self, node) -> Node:
        return Node.from_attribute(Attributes.Identifier).append_field(
            Attributes.Name(node["name"])
        )

    def transform_node(self, node) -> Union[Optional[Node], List[Any], Any]:
        if isinstance(node, dict):
            if node.get("type") is None and "operator" in node:
                ty = "operator"
            else:
                ty = to_snake_case(node.get("type"))

            if self.functions is None:
                self.functions = self.get_functions("transform_")
            for name, func in self.functions:
                if name in ty:
                    result = func(node)
                    break
            else:
                result = NotImplemented

            if result is NotImplemented:
                result = self.transform_other(node)
            if result and node.get("comments"):
                comments = []
                for comment in node.get("comments"):
                    # TODO: check comment['type']
                    comments.append(comment["value"])
                result.append_field(Traits.BeforeLineComment(comments))
            return result
        elif isinstance(node, list):
            result = [self.transform_node(n) for n in node]
            return result
        else:
            return node


class VisitorImpl(VisitorBase):
    def transform_variable_declaration(self, node) -> Node:
        declarations = node["declarations"]
        if len(declarations) == 1:
            decl = declarations[0]
            if self.match_func_call(decl["init"], "require"):
                names = self.get_name(decl.get("id"))
                paths = [arg["value"] for arg in decl["init"]["arguments"]]
                assert len(paths) == 1
                paths = paths[0]
                decl_node = self.transform_node(decl)
                req = Nodes.requires(paths, names, decl_node)
                return req
        decls = []
        for decl in declarations:
            id = self.get_name(decl["id"])
            init = self.transform_node(decl["init"])
            decls.append(
                Node.from_attribute(Attributes.VariableDeclaration)
                    .append_field(Attributes.Id(id))
                    .append_field(Attributes.Value(init))
            )
        return Node.from_attribute(Attributes.VariableDeclarations(decls))

    def transform_assignment_expression(self, node):
        name = self.get_name(node["left"], member_expression=True, warn=False)
        if name == "module.exports":
            return self.transform_node(node["right"])
        return NotImplemented

    def transform_class_expression(self, node):
        class_name = self.get_name(node["id"])
        super_class = self.get_name(node["superClass"])

        body = self.transform_node(node["body"]["body"]) or []

        n = (
            Node.from_attribute(Attributes.ClassDecl)
                .append_field(Attributes.Name(class_name))
                .append_field(Attributes.SuperClasses([super_class]))
                .append_field(Attributes.Children(body))
        )

        return n

    def transform_this_expression(self, node):
        assert node["type"] == "ThisExpression"
        return Node.from_attribute(Attributes.ThisExpression)

    def transform_super(self, node):
        assert node["type"] == "Super"
        return Node.from_attribute(Attributes.SuperExpression)

    def transform_method_definition(self, node):
        name = self.get_name(node["key"])
        is_async = node["value"]["async"]
        params = list(map(self.transform_arg, node["value"]["params"]))
        children = self.transform_node(node["value"]["body"]["body"]) or []

        n = Node.from_attribute(Attributes.FunctionDecl)
        n.append_field(Attributes.Name(name))
        n.append_field(Attributes.Async(is_async))
        n.append_field(Attributes.Children(children))
        n.append_field(Attributes.Arguments(params))

        return n

    def transform_arg(self, node) -> Node:
        if node["type"] == "AssignmentPattern":
            name = self.get_name(node["left"])
            default = self.transform_node(node["right"])
        else:
            name = self.get_name(node)
            default = None
        n = Node.from_attribute(Attributes.ArgumentName(name)).append_field(
            Attributes.ArgumentType(
                Types.AllValue.copy().append_field(Traits.NotInferredType).freeze()
            )
        )
        if default:
            n.append_field(Attributes.DefaultValue(default))
        return n

    def transform_call_expression(self, node) -> Optional[Node]:
        if self.match_func_call(node, "console.log"):
            return Nodes.print_node(self.transform_node(node["arguments"]))
        n = Node.from_attribute(Attributes.FunctionCall)
        n.append_field(Attributes.Callee(self.transform_node(node["callee"])))
        arguments = self.transform_node(node["arguments"]) or []
        n.append_field(Attributes.Arguments(arguments))
        return n

    def transform_return_statement(self, node) -> Node:
        return Node.from_attribute(Attributes.Return(self.transform_node(node["argument"])))

    def transform_property(self, node) -> Node:
        return (
            Node.from_attribute(Attributes.ObjectProperty)
                .append_field(Attributes.KeyName(self.transform_node(node["key"])))
                .append_field(Attributes.Value(self.transform_node(node["value"])))
        )

    def transform_object_expression(self, node) -> Node:
        return Node.from_attribute(
            Attributes.ObjectProperties(self.transform_node(node["properties"]))
        )

    def transform_logical_expression(self, node) -> Node:
        return self.transform_operator(node)

    def transform_binary_expression(self, node) -> Node:
        return self.transform_operator(node)

    def transform_unary_expression(self, node) -> Node:
        return self.transform_operator(node)

    def transform_update_expression(self, node) -> Node:
        return Node.from_attribute(Attributes.Statement(self.transform_operator(node)))

    def transform_operator(self, node) -> Node:
        operator = Node.from_attribute(Attributes.Operator(node["operator"]))
        positions = [
            ("left", "left", Attributes.OperatorLeft),
            ("right", "right", Attributes.OperatorRight),
            ("prefix", "argument", Attributes.OperatorSinglePrefix),
            ("postfix", "argument", Attributes.OperatorSinglePostfix),
        ]
        for key, value_key, attr in positions:
            if node.get(key):
                operator.append_field(attr(self.transform_node(node[value_key])))
        if node.get('prefix') is False:
            operator.append_field(Attributes.OperatorSinglePostfix(self.transform_node(node['argument'])))
        return operator

    def transform_if_statement(self, node) -> Node:
        if_clauses = []
        while node:
            if node.get("type") == "BlockStatement":
                attr = Attributes.ElseClause
                test = None
            elif node.get("type") == "IfStatement":
                if if_clauses:
                    attr = Attributes.ElseIfClause
                else:
                    attr = Attributes.IfClause
                test = self.transform_node(node["test"])
            else:
                raise Exception("Could not process in if statement" + str(node))
            body = self.transform_node(node["consequent"])

            n = Node.from_attribute(attr).append_field(Attributes.Consequence(body))
            if test is not None:
                n.append_field(Attributes.TestExpression(test))

            if_clauses.append(n)
            node = node.get("alternate")

        return Node.from_attribute(Attributes.IfClauses).append_field(
            Attributes.Children(if_clauses)
        )

    def transform_block_statement(self, node) -> Node:
        return Node.from_attribute(Attributes.BlockStatement).append_field(
            Attributes.Children(self.transform_node(node["body"]))
        )

    def transform_for_statement(self, node) -> Node:
        n = Node.from_attribute(Attributes.CForLoop)
        init = self.transform_node(node["init"])
        n.append_field(Attributes.CForLoopInit(init))
        test = self.transform_node(node["test"])
        n.append_field(Attributes.CForLoopTest(test))
        update = self.transform_node(node["update"])
        n.append_field(Attributes.CForLoopUpdate(update))
        body = self.transform_node(node["body"]["body"])
        n.append_field(Attributes.Children(body))
        return n


class JavascriptParser(Parser):
    def accept(self, fmt: InputDefinition) -> bool:
        return (
                isinstance(fmt, SourceInput)
                and fmt.lang == "javascript"
                and load_module("esprima")
        )

    def parse(self, name: str, fmt: InputDefinition) -> Node:
        assert isinstance(fmt, SourceInput)
        import esprima

        parsed = esprima.parseScript(fmt.code, {"comment": True})
        node = VisitorImpl().transform_node(parsed.toDict())
        return node

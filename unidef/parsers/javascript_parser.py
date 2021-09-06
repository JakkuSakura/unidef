import json
import logging
import traceback

from beartype import beartype

from unidef.models.definitions import SourceExample
from unidef.models.transpile_model import Attribute, Attributes, Node, Nodes
from unidef.models.type_model import Traits, Type, Types
from unidef.parsers import Definition, Parser
from unidef.utils.loader import load_module
from unidef.utils.name_convert import *
from unidef.utils.typing_compat import *
from unidef.utils.visitor import VisitorPattern


class VisitorBase(VisitorPattern):
    def __init__(self):
        super().__init__()
        self.functions = None

    def get_recursive(self, obj: Dict, path: str) -> Any:
        for to_visit in path.split("."):
            obj = obj.get(to_visit)
            if obj is None:
                return None
        return obj

    def get_name(
            self, node, warn=True
    ) -> Union[List[str], List[(str, str)], Optional[str]]:
        if node is None:
            return
        ty = node.get("type")
        if ty == "MemberExpression":
            first = self.get_name(node.get("object"), warn)
            second = self.get_name(node.get("property"), warn)
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
                names.append((self.get_name(key, warn), self.get_name(value, warn)))
            return names
        elif node.get("name"):
            return node.get("name")
        else:
            if warn:
                logging.warning("could not get name from %s", node)
            return

    def match_func_call(self, node, name: str) -> bool:
        if node['type'] != 'CallExpression':
            return False
        spt = tuple(name.split("."))
        try:
            if len(spt) == 2:
                obj, method = spt
                callee = node["callee"]
                obj0 = self.get_name(callee)
                if obj0 == obj and self.get_name(callee.get('property')) == method:
                    return True
            elif len(spt) == 1:
                (obj,) = spt
                callee = node["callee"]
                obj0 = self.get_name(callee)
                return obj0 == obj
        except KeyError as e:
            print(node)
            traceback.print_exc()

        return False

    def visit_program(self, node) -> Node:
        program = Node.from_str("program")
        body = self.visit_node(node["body"]) or []
        program.append_field(Attributes.Children(body))
        return program

    def visit_other(self, node) -> Node:
        if node.get("type"):
            n = Node.from_str(node.pop("type"))
        else:
            n = Node.from_str(node.pop("name"))

        for key, value in node.items():
            value = self.visit_node(value)
            default = [] if isinstance(value, list) else None
            attr = Attribute(key=key, default_present=default)(value)
            if n.exist_field(attr):
                attr.key = key + "_"

            n.append_field(attr)
        return n

    def visit_literal(self, node) -> Node:
        return (
            Node.from_attribute(Attributes.Literal)
                .append_field(Attributes.RawCode(node["raw"]))
                .append_field(Attributes.RawValue(node["value"]))
        )

    def visit_node(self, node) -> Union[Optional[Node], List[Any], Any]:
        if isinstance(node, dict):
            name = self.get_name(node, warn=False)
            if name:
                return name
            if self.functions is None:
                self.functions = self.get_functions("visit_")

            ty = to_snake_case(node.get("type"))
            for name, func in self.functions:
                if name in ty:
                    result = func(node)
                    break
            else:
                result = NotImplemented

            if result is NotImplemented:
                result = self.visit_other(node)
            if result and node.get("comments"):
                comments = []
                for comment in node.get("comments"):
                    # TODO: check comment['type']
                    comments.append(comment["value"])
                result.append_field(Traits.BeforeLineComment(comments))
            return result
        elif isinstance(node, list):
            result = [self.visit_node(n) for n in node]
            return result
        else:
            return node


class VisitorImpl(VisitorBase):
    pass

    def visit_variable_declaration(self, node) -> Node:
        declarations = node["declarations"]
        if len(declarations) == 1:
            decl = declarations[0]
            if self.match_func_call(decl["init"], "require"):
                names = self.get_name(decl.get("id"))
                paths = [arg["value"] for arg in decl["init"]["arguments"]]
                assert len(paths) == 1
                paths = paths[0]
                decl_node = self.visit_node(decl)
                req = Nodes.require(paths, names, decl_node)
                return req
        return NotImplemented

    def visit_assignment_expression(self, node):
        name = self.get_name(node["left"])
        if name == "module.exports":
            return self.visit_node(node["right"])
        return NotImplemented

    def visit_class_expression(self, node):
        class_name = self.get_name(node["id"])
        super_class = self.get_name(node["superClass"])

        body = self.visit_node(node["body"]["body"]) or []

        n = (
            Node.from_attribute(Attributes.ClassDecl)
            .append_field(Attributes.Name(class_name))
            .append_field(Attributes.SuperClasses([super_class]))
            .append_field(Attributes.Children(body))
        )

        return n

    def visit_method_definition(self, node):
        name = self.get_name(node["key"])
        is_async = node["value"]["async"]
        params = list(map(self.visit_arg, node["value"]["params"]))
        children = self.visit_node(node["value"]["body"]["body"]) or []
        node1 = Node.from_attribute(Attributes.FunctionDecl)
        trait = Attributes.Name(name)
        return (
            node1.append_field(trait)
                .append_field(Attributes.Async(is_async))
                .append_field(Attributes.Children(children))
                .append_field(Attributes.Arguments(params))
        )

    def visit_arg(self, node) -> Node:
        if node['type'] == 'AssignmentPattern':
            name = self.get_name(node['left'])
            default = self.visit_node(node['right'])
        else:
            name = self.get_name(node)
            default = None
        n = Node.from_attribute(
            Attributes.ArgumentName(name)
        ).append_field(Attributes.ArgumentType(Types.AllValue.copy().append_field(Traits.NotInferredType).freeze()))
        if default:
            n.append_field(Attributes.DefaultValue(default))
        return n

    def visit_call_expression(self, node) -> Optional[Node]:
        if self.match_func_call(node, "console.log"):
            return Nodes.print_node(self.visit_node(node["arguments"]))
        n = Node.from_attribute(Attributes.FunctionCall)
        n.append_field(Attributes.Callee(self.visit_node(node["callee"])))
        arguments = self.visit_node(node["arguments"]) or []
        n.append_field(Attributes.Arguments(arguments))
        return n

    def visit_return_statement(self, node) -> Node:
        return Node.from_attribute(Attributes.Return(self.visit_node(node["argument"])))

    def visit_property(self, node) -> Node:
        return (
            Node.from_attribute(Attributes.ObjectProperty)
                .append_field(Attributes.KeyName(self.visit_node(node["key"])))
                .append_field(Attributes.Value(self.visit_node(node["value"])))
        )

    def visit_object_expression(self, node) -> Node:
        return Node.from_attribute(
            Attributes.ObjectProperties(self.visit_node(node["properties"]))
        )


class JavascriptParser(Parser):
    def accept(self, fmt: Definition) -> bool:
        return (
                isinstance(fmt, SourceExample)
                and fmt.lang == "javascript"
                and load_module("esprima")
        )

    def parse(self, name: str, fmt: Definition) -> Node:
        assert isinstance(fmt, SourceExample)
        import esprima

        parsed = esprima.parseScript(fmt.code, {"comment": True})
        node = VisitorImpl().visit_node(parsed.toDict())
        return node

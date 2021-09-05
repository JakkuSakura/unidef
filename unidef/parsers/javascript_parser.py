import stringcase

from unidef.parsers import Parser, Definition
from unidef.models.type_model import Type
from unidef.models.transpile_model import Node, Attributes, Attribute
from unidef.models.definitions import SourceExample
from unidef.utils.typing_compat import *
from beartype import beartype
import pyjsparser


class VisitorBase:
    def __init__(self):
        self.functions = None

    def get_functions(self):
        functions = {}
        for key in dir(self):
            value = getattr(self, key)
            if isinstance(value, Callable) and key.startswith('visit_'):
                node_name = key[len('visit_'):]
                functions[node_name] = value
        return functions

    def visit_program(self, node) -> Node:
        program = Node.from_str('program')
        program.extend_traits(Attributes.child, [self.visit_node(stmt) for stmt in node['body']])
        return program

    def visit_other(self, node) -> Node:
        n = Node.from_str(node.pop('type'))
        for key, value in node.items():
            n.append_trait(Attribute.from_str(key).init_with(self.visit_node(value)))
        return n

    def visit_node(self, node) -> Node:
        if isinstance(node, dict) and node.get('type'):
            if self.functions is None:
                self.functions = self.get_functions()
            ty = stringcase.snakecase(node['type'])
            for name, func in self.functions.items():
                if name in ty:
                    result = func(node)
                    break
            else:
                result = NotImplemented

            if result is NotImplemented:
                return self.visit_other(node)
            else:
                return result
        else:
            return node


class VisitorImpl(VisitorBase):

    def visit_statement(self, node) -> Node:
        return Node.from_str('statement'). \
            append_trait(Attributes.expression(self.visit_node(node['expression'])))

    def visit_call_expression(self, node) -> Node:
        return Node.from_str('func_call').append_trait(Attributes.callee(self.visit_node(node['callee'])))


class JavascriptParser(Parser):
    def accept(self, fmt: Definition) -> bool:
        return isinstance(fmt, SourceExample) and fmt.lang == 'javascript'

    def parse(self, name: str, fmt: Definition) -> Node:
        assert isinstance(fmt, SourceExample)
        parsed = pyjsparser.parse(fmt.code)
        node = VisitorImpl().visit_node(parsed)
        return node

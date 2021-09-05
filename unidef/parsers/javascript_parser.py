from unidef.parsers import Parser, Definition
from unidef.models.type_model import Type
from unidef.utils.name_convert import *
from unidef.models.transpile_model import Node, Attributes, Attribute, Nodes
from unidef.models.definitions import SourceExample
from unidef.utils.typing_compat import *
from unidef.utils.loader import load_module
from beartype import beartype


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
        if node.get('type'):
            n = Node.from_str(node.pop('type'))
        else:
            n = Node.from_str(node.pop('name'))

        for key, value in node.items():
            n.append_trait(Attribute.from_str(key).default(self.visit_node(value)))
        return n

    def visit_literal(self, node) -> Node:
        return Node.from_str('literal').append_trait(Attributes.raw_code(node['raw']))

    def visit_node(self, node) -> Union[Node, List[Any], Any]:
        if isinstance(node, dict):
            type_or_name = node.get('type') or node.get('name')

            if self.functions is None:
                self.functions = self.get_functions()
            ty = to_snake_case(type_or_name)
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
        elif isinstance(node, list):
            return list(map(self.visit_node, node))
        else:
            return node


class VisitorImpl(VisitorBase):
    def match_func_call(self, node, name: str) -> bool:
        obj, method = tuple(name.split('.'))
        callee = node['callee']
        if callee['object']['name'] == obj and callee['property']['name'] == method:
            return True
        else:
            return False

    def visit_statement(self, node) -> Node:
        return Node.from_str('statement'). \
            append_trait(Attributes.expression(self.visit_node(node['expression'])))

    def visit_call_expression(self, node) -> Node:
        if self.match_func_call(node, 'console.log'):
            return Nodes.print_node(self.visit_node(node['arguments']))
        return Node.from_str('func_call').append_trait(Attributes.callee(self.visit_node(node['callee'])))


class JavascriptParser(Parser):
    def accept(self, fmt: Definition) -> bool:
        return isinstance(fmt, SourceExample) and fmt.lang == 'javascript' and load_module('pyjsparser')

    def parse(self, name: str, fmt: Definition) -> Node:
        assert isinstance(fmt, SourceExample)
        import pyjsparser
        parsed = pyjsparser.parse(fmt.code)
        node = VisitorImpl().visit_node(parsed)
        return node

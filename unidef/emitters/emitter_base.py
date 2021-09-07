from pydantic import BaseModel

from unidef.emitters import Emitter
from unidef.models import config_model, type_model
from unidef.models.config_model import ModelDefinition
from unidef.models.transpile_model import Attribute, Attributes, Node
from unidef.models.type_model import Traits, Type
from unidef.utils.formatter import Formatee, Function, IndentBlock, IndentedWriter
from unidef.utils.typing_compat import *
from unidef.utils.name_convert import *
from unidef.utils.visitor import VisitorPattern


class EmitterBase(BaseModel, VisitorPattern):
    functions: Any = None
    formatter: Any = IndentedWriter()

    def emit_others(self, node):
        self.formatter.append_line(str(node))

    def emit_node(self, node):
        if isinstance(node, Node) or isinstance(node, Type):
            if self.functions is None:
                self.functions = self.get_functions("emit_")

            node_name = node.get_field(Attributes.Kind)
            assert node_name, f"Name cannot be empty to emit: {node}"
            node_name = to_snake_case(node_name)

            for name, func in self.functions:
                if name in node_name:
                    result = func(node)
                    break
            else:
                result = NotImplemented
            if result is NotImplemented:
                self.emit_others(node)
        elif isinstance(node, list):
            for n in node:
                self.emit_node(n)
        else:
            raise Exception("Could not emit " + str(node))

    def write(self, elem: Formatee):
        elem.format_with(self.formatter)

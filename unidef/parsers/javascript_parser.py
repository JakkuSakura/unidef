import json
import logging
import traceback

from beartype import beartype

from unidef.models.input_model import SourceInput
from unidef.languages.common.ir_model import Attribute, Attributes, IrNode, Nodes
from unidef.languages.common.type_model import Traits, DyType, Types
from unidef.parsers import InputDefinition, Parser
from unidef.utils.loader import load_module
from unidef.utils.name_convert import *
from unidef.utils.typing import *
from unidef.utils.visitor import VisitorPattern
from unidef.utils.transformer import NodeTransformer


class JavascriptParser(Parser):
    def accept(self, fmt: InputDefinition) -> bool:
        return (
            isinstance(fmt, SourceInput)
            and fmt.lang == "javascript"
            and load_module("esprima")
        )

    def parse(self, name: str, fmt: InputDefinition) -> IrNode:
        from unidef.languages.javascript.javascript_parser import JavascriptParserImpl

        return JavascriptParserImpl().parse(name, fmt)

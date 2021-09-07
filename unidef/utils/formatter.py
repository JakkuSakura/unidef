from beartype import beartype
from pydantic import BaseModel

from unidef.utils.typing_compat import *
from unidef.utils.visitor import VisitorPattern
from unidef.utils.name_convert import *


class SourceNode(BaseModel):
    pass


class IndentNode(SourceNode):
    level: int


class DedentNode(SourceNode):
    level: int


class TextNode(SourceNode):
    text: str


class BracesNode(SourceNode):
    open: str = '{'
    value: SourceNode
    close: str = '}'
    new_line: bool = True


class LineNode(SourceNode):
    sources = []


class Formatter(BaseModel):
    def to_string(self):
        pass


class StructuredFormatter(Formatter, VisitorPattern):
    indent: int = 0
    tab: str = "    "
    nodes: List[SourceNode] = []
    collection: List[str] = []
    functions: Optional[List[(str, Callable)]] = None

    @beartype
    def append_format_node(self, node: SourceNode):
        self.nodes.append(node)

    @beartype
    def format_indent_node(self, node: IndentNode):
        self.indent += node.level

    @beartype
    def format_dedent_node(self, node: DedentNode):
        self.indent -= node.level

    @beartype
    def format_text_node(self, node: TextNode):
        self.collection.append(node.text)

    @beartype
    def format_line_node(self, node: LineNode):
        self.collection.append(self.tab * self.indent)
        for n in node.sources:
            self.format_node(n)
        self.collection.append('\n')

    @beartype
    def format_braces_node(self, node: BracesNode):
        if node.new_line:
            self.append_line(node.open)
            self.indent += 1
        else:
            self.append(node.open)

        self.format_node(node.value)

        if node.new_line:
            self.indent -= 1
            self.append_line(node.close)
        else:
            self.append(node.close)

    @beartype
    def format_node(self, node: SourceNode):
        if self.functions is None:
            self.functions = self.get_functions('format_')
        for name, func in self.functions:
            if to_snake_case(type(node).__name__) == name:
                func(node)
                break
        else:
            raise Exception("No function to format node " + type(node).__name__)

    def to_string(self, strip_left=False):
        self.collection = []
        for n in self.nodes:
            self.format_node(n)
        if strip_left and self.collection[0].isspace():
            return "".join(self.collection[1:])
        else:
            return "".join(self.collection)

    def copy(self, **kwargs) -> __qualname__:
        kwargs['deep'] = True
        return super().copy(**kwargs)

class IndentedWriter(Formatter):
    formatter: StructuredFormatter = StructuredFormatter()
    current_line: List[SourceNode] = []

    def try_indent(self):
        try:
            if len(self.content) == 0 or self.content[-1] == "\n":
                self.content.append(self.tab * self.indent)
        except:
            pass

    @beartype
    def append_line(self, s: str = ""):
        self.current_line.append(TextNode(text=s))
        self.formatter.append_format_node(LineNode(sources=self.current_line))
        self.current_line = []

    @beartype
    def append(self, s: str):
        self.current_line.append(TextNode(text=s))

    def incr_indent(self, level=1):
        self.formatter.append_format_node(IndentNode(level=level))

    def decr_indent(self, level=1):
        self.formatter.append_format_node(DedentNode(level=level))

    def to_string(self, strip_left=False):
        return self.formatter.to_string(strip_left)

    def copy(self, **kwargs) -> __qualname__:
        kwargs['deep'] = True
        return super().copy(**kwargs)

    def clone(self):
        return self.copy()


class IndentedFormatee:
    def format_with(self, formatter: Formatter):
        pass


class Braces(IndentedFormatee):
    def __init__(self, val: IndentedFormatee, open="{", close="}", new_line=True):
        self.value = val
        self.open = open
        self.close = close
        self.new_line = new_line

    def format_with(self, writer: IndentedWriter):
        if self.new_line:
            writer.append_line(self.open)
            writer.incr_indent()
        else:
            writer.append(self.open)

        self.value.format_with(writer)

        if self.new_line:
            writer.decr_indent()
            writer.append_line(self.close)
        else:
            writer.append(self.close)


class IndentBlock(Braces):
    def __init__(self, val: IndentedFormatee):
        super().__init__(val, open=":", close="")


class Function(IndentedFormatee):
    def __init__(self, func):
        self.func = func

    def format_with(self, writer: IndentedWriter):
        self.func(writer)


class Text(IndentedFormatee):
    def __init__(self, text):
        self.text = text

    def format_with(self, writer: IndentedWriter):
        writer.append(self.text)

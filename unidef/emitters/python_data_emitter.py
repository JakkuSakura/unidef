import logging

from beartype import beartype
from pydantic import BaseModel

from unidef.emitters import Emitter
from unidef.models import config_model, type_model
from unidef.models.config_model import ModelDefinition
from unidef.models.type_model import Traits, Type
from unidef.utils.formatter import *
from unidef.utils.name_convert import to_pascal_case, to_snake_case
from unidef.utils.typing import List


def map_type_to_peewee_model(ty: Type, args="") -> str:
    if ty.get_field(Traits.Nullable):
        args += "null=True"
    if ty.get_field(Traits.Primary):
        args += "primary=True"

    if ty.get_field(Traits.Bool):
        return "BoolField({})".format(args)
    elif ty.get_field(Traits.TsUnit):
        return "DateTimeField()"
    elif ty.get_field(Traits.Integer):
        bits = ty.get_field(Traits.BitSize)
        if bits < 32:
            return "SmallIntegerField({})".format(args)
        elif bits == 32:
            return "IntegerField({})".format(args)
        elif bits > 32:
            return "BigIntegerField({})".format(args)
        else:
            raise NotImplementedError()

    elif ty.get_field(Traits.Floating):
        bits = ty.get_field(Traits.BitSize)
        if bits == 32:
            return "FloatField({})".format(args)
        elif bits == 64:
            return "DoubleField({})".format(args)
        else:
            raise NotImplementedError()
    elif ty.get_field(Traits.String) or ty.get_field(Traits.Null):
        return "TextField()"
    elif ty.get_field(Traits.Enum):
        if ty.get_field(Traits.SimpleEnum):
            return "TextField({})".format(args)
        else:
            return "BinaryJSONField({})".format(args)
    return ty.get_field(Traits.TypeName)


PYTHON_KEYWORDS = {
    "and": "and_",
    "as": "as_",
    "assert": "assert_",
    "break": "break_",
    "class": "class_",
    "continue": "continue_",
    "def": "def_",
    "del": "del_",
    "elif": "elif_",
    "else": "else_",
    "except": "except_",
    "False": "False_",
    "finally": "finally_",
    "for": "for_",
    "from": "from_",
    "global": "global_",
    "if": "if_",
    "import": "import_",
    "in": "in_",
    "is": "is_",
    "lambda": "lambda_",
    "None": "None_",
    "nonlocal": "nonlocal_",
    "not": "not_",
    "or": "or_",
    "pass": "pass_",
    "raise": "raise_",
    "return": "return_",
    "True": "True_",
    "try": "try_",
    "while": "while_",
    "with": "with_",
    "yield": "yield_",
}


def map_field_name(name: str) -> str:
    if not name[0].isalpha() and name[0] != "_":
        return "_" + name
    return to_snake_case(PYTHON_KEYWORDS.get(name) or name)


class PythonField(BaseModel):
    name: str
    original_name: str = None
    value: type_model.Type

    def __init__(self, f: Type = None, **kwargs):
        if f:
            kwargs.update(
                {
                    "name": map_field_name(f.get_field(Traits.FieldName)),
                    "original_name": f.get_field(Traits.TypeName),
                    "value": f,
                }
            )

        super().__init__(**kwargs)

    def transform(self) -> SourceNode:
        return LineNode(content=TextNode(text=f"{self.name} = {map_type_to_peewee_model(self.value)}"))


class PythonComment(BaseModel):
    content: List[str]
    python_doc: bool = False

    def __init__(self, lines: List[str], python_doc: bool = False):
        super().__init__(content=lines, python_doc=python_doc)

    def transform(self) -> SourceNode:
        if self.python_doc:
            lines = [LineNode(content=TextNode(text=line)) for line in self.content]
            return BracesNode(value=BulkNode(sources=lines), open='"""', close='"""')
        else:
            lines = [LineNode(content=TextNode(text="# " + line)) for line in self.content]
            return BulkNode(sources=lines)


class PythonClass(BaseModel):
    name: str
    fields: List[PythonField]
    comment: PythonComment = None
    model: str = "BaseModel"

    @staticmethod
    def parse_name(name):
        return to_pascal_case(name)

    def __init__(self, raw: Type, **kwargs):
        if raw:
            kwargs.update(
                {
                    "name": PythonClass.parse_name(raw.get_field(Traits.TypeName)),
                    "fields": [
                        PythonField(f) for f in raw.get_field(Traits.StructFields)
                    ],
                }
            )

        super().__init__(**kwargs)

    def transform(self) -> SourceNode:
        sources = []
        sources.append(TextNode(text=f"class {self.name}({self.model})"))
        in_indent_block = []
        in_indent_block.append(self.comment.transform())

        for field in self.fields:
            in_indent_block.append(field.transform())
        sources.append(BracesNode(value=BulkNode(sources=in_indent_block), open=':', close=''))

        return BulkNode(sources=sources)


class PythonEnum(BaseModel):
    name: str
    variants: List[Type]
    model: str = "enum.Enum"
    raw: Type = None

    @staticmethod
    def parse_name(name):
        return to_pascal_case(name)

    def __init__(self, raw: Type = None, **kwargs):

        if raw:
            kwargs.update(
                {
                    "raw": raw,
                    "name": PythonEnum.parse_name(raw.get_field(Traits.TypeName)),
                    "variants": raw.get_field(Traits.Variant),
                }
            )

        super().__init__(**kwargs)

    def transform(self) -> SourceNode:
        sources = []
        sources.append(TextNode(text=f"class {self.name}({self.model})"))

        in_indent_block = []
        for field in self.variants:
            name = field.get_field(Traits.VariantName)

            in_indent_block.append(
                LineNode(content=TextNode(text="{lname} = '{rname}'".format(lname=map_field_name(name), rname=name))))

        sources.append(BracesNode(value=BulkNode(sources=in_indent_block), open=':', close=''))

        return BulkNode(sources=sources)


class StructRegistry:
    def __init__(self):
        self.structs: List[PythonClass] = []

    def add_struct(self, struct: PythonClass):
        if struct not in self.structs:
            self.structs.append(struct)


def find_all_structs_impl(reg: StructRegistry, s: Type):
    if s.get_field(Traits.Struct):
        reg.add_struct(PythonClass(s))
    else:
        raise NotImplementedError()


@beartype
def find_all_structs(s: Type) -> List[PythonClass]:
    reg = StructRegistry()
    find_all_structs_impl(reg, s)
    return reg.structs


def emit_struct(root: Type) -> SourceNode:
    sources = []
    for python_struct in find_all_structs(root):
        sources.append(python_struct.transform())
    return BulkNode(sources=sources)


def emit_python_model_definition(root: ModelDefinition) -> SourceNode:
    sources = []
    comment = []
    for attr in ["type", "url", "ref", "note"]:
        t = getattr(root, attr)
        if t:
            comment.append(f"{attr}: {t}")
    comment = PythonComment(comment, python_doc=True)
    parsed = root.get_parsed()
    if parsed.get_field(Traits.Struct):
        for i, struct in enumerate(find_all_structs(parsed)):
            python_struct = struct
            if i == 0:
                python_struct.comment = comment
            sources.append(python_struct.transform())
    elif parsed.get_field(Traits.Enum):
        python_struct = PythonEnum(parsed)
        sources.append(python_struct.transform())
    else:
        raise Exception("must be a struct or enum", root)

    return BulkNode(sources=sources)


class PythonDataEmitter(Emitter):
    def accept(self, s: str) -> bool:
        if "python" in s:
            if "peewee" in s:
                return True
            else:
                logging.warning(
                    "target=python is deprecated, use target=python_peewee instead"
                )
                return True

    def emit_model(self, target: str, model: ModelDefinition) -> str:
        formatter = StructuredFormatter(nodes=[emit_python_model_definition(model)])
        return formatter.to_string()

    def emit_type(self, target: str, ty: Type) -> str:
        formatter = StructuredFormatter(nodes=[emit_struct(model)])
        return formatter.to_string()

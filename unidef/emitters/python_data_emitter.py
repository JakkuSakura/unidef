import logging

from beartype import beartype
from pydantic import BaseModel

from unidef.emitters import Emitter
from unidef.models import config_model, type_model
from unidef.models.config_model import ModelDefinition
from unidef.models.type_model import Traits, Type
from unidef.utils.formatter import Formatee, Function, IndentBlock, IndentedWriter
from unidef.utils.name_convert import to_pascal_case, to_snake_case
from unidef.utils.typing_compat import List


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


class PythonField(Formatee, BaseModel):
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

    def format_with(self, writer: IndentedWriter):
        writer.append_line(f"{self.name} = {map_type_to_peewee_model(self.value)}")


class PythonComment(Formatee, BaseModel):
    content: List[str]
    python_doc: bool = False

    def __init__(self, content: str, python_doc: bool = False):
        super().__init__(content=content.splitlines(), python_doc=python_doc)

    def format_with(self, writer: IndentedWriter):
        if self.python_doc:
            writer.append_line('"""')
            for line in self.content:
                writer.append_line(line)
            writer.append_line('"""')
        else:
            for line in self.content:
                writer.append_line("# " + line)


class PythonStruct(Formatee, BaseModel):
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
                    "name": PythonStruct.parse_name(raw.get_field(Traits.TypeName)),
                    "fields": [
                        PythonField(f) for f in raw.get_field(Traits.StructFields)
                    ],
                }
            )

        super().__init__(**kwargs)

    def format_with(self, writer: IndentedWriter):
        writer.append(f"class {self.name}({self.model})")

        def for_field(writer1: IndentedWriter):
            self.comment.format_with(writer1)
            for field in self.fields:
                field.format_with(writer1)

        IndentBlock(Function(for_field)).format_with(writer)


class PythonEnum(Formatee, BaseModel):
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

    def format_with(self, writer: IndentedWriter):
        writer.append(f"class {self.name}({self.model})")

        def for_field(writer1: IndentedWriter):
            for field in self.variants:
                name = field.get_field(Traits.VariantName)
                writer1.append_line(
                    "{lname} = '{rname}'".format(lname=map_field_name(name), rname=name)
                )

        IndentBlock(Function(for_field)).format_with(writer)


class StructRegistry:
    def __init__(self):
        self.structs: List[PythonStruct] = []

    def add_struct(self, struct: PythonStruct):
        if struct not in self.structs:
            self.structs.append(struct)


def find_all_structs_impl(reg: StructRegistry, s: Type):
    if s.get_field(Traits.Struct):
        reg.add_struct(PythonStruct(s))
    else:
        raise NotImplementedError()


@beartype
def find_all_structs(s: Type) -> List[PythonStruct]:
    reg = StructRegistry()
    find_all_structs_impl(reg, s)
    return reg.structs


def emit_struct(root: Type) -> str:
    writer = IndentedWriter()
    for s in find_all_structs(root):
        python_struct = s
        python_struct.format_with(writer)
    return writer.to_string()


def emit_python_model_definition(root: ModelDefinition) -> str:
    writer = IndentedWriter()
    comment = []
    for attr in ["type", "url", "ref", "note"]:
        t = getattr(root, attr)
        if t:
            comment.append(f"{attr}: {t}")
    comment = PythonComment("\n".join(comment), python_doc=True)
    parsed = root.get_parsed()
    if parsed.get_field(Traits.Struct):
        for i, struct in enumerate(find_all_structs(parsed)):
            python_struct = struct
            if i == 0:
                python_struct.comment = comment
            python_struct.format_with(writer)
    elif parsed.get_field(Traits.Enum):
        python_struct = PythonEnum(parsed)
        python_struct.format_with(writer)
    else:
        raise Exception("must be a struct or enum", root)

    return writer.to_string()


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
        return emit_python_model_definition(model)

    def emit_type(self, target: str, ty: Type) -> str:
        return emit_struct(ty)

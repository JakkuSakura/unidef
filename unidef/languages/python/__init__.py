from unidef.emitters import Emitter
from unidef.languages.common.type_model import DyType, FieldType, Traits
from unidef.models.config_model import ModelDefinition
from unidef.utils.formatter import *
from unidef.utils.name_convert import to_pascal_case, to_snake_case
from unidef.utils.typing import *


def map_type_to_peewee_model(ty: DyType, args="") -> str:
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
    elif ty.get_field(Traits.String) or (
        ty.get_field(Traits.Null) and ty.get_field(Traits.FromJson)
    ):
        return "TextField()"
    elif ty.get_field(Traits.Enum):
        if ty.get_field(Traits.SimpleEnum):
            return "TextField({})".format(args)
        else:
            return "BinaryJSONField({})".format(args)
    return ty.get_field(Traits.TypeName)


def map_type_to_pydantic_model(ty: DyType) -> str:
    if ty.get_field(Traits.Nullable):
        ty = ty.copy().remove_field(Traits.Nullable)
        base_name = f"Optional[{map_type_to_pydantic_model(ty)}]"
    elif ty.get_field(Traits.Bool):
        base_name = "bool"
    elif ty.get_field(Traits.TsUnit):
        base_name = "datetime.timestamp"
    elif ty.get_field(Traits.Integer):
        base_name = "int"
    elif ty.get_field(Traits.Floating):
        base_name = "float"
    elif ty.get_field(Traits.String) or (
        ty.get_field(Traits.Null) and ty.get_field(Traits.FromJson)
    ):
        base_name = "str"
    elif ty.exist_field(Traits.Tuple):
        fields = ty.get_field(Traits.Generics)
        base_name = "({})".format(", ".join(map(map_type_to_pydantic_model, fields)))
    else:
        base_name = ty.get_field(Traits.TypeName)

    if ty.exist_field(Traits.Default):
        return base_name + " = " + ty.get_field(Traits.Default)
    else:
        return base_name


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


@beartype
def map_field_name(name: str) -> str:
    if not name[0].isalpha() and name[0] != "_":
        return "_" + name
    return to_snake_case(PYTHON_KEYWORDS.get(name) or name)


class PythonField(BaseModel):
    name: str
    original_name: str = None
    value: DyType

    def __init__(self, f: FieldType = None, **kwargs):
        if f:
            kwargs.update(
                {
                    "name": map_field_name(f.field_name),
                    "original_name": f.field_name,
                    "value": f.field_type,
                }
            )

        super().__init__(**kwargs)

    def transform_peewee(self) -> SourceNode:
        return LineNode(
            content=TextNode(
                text=f"{self.name} = {map_type_to_peewee_model(self.value)}"
            )
        )

    def transform_pydantic(self) -> SourceNode:
        return LineNode(
            content=TextNode(
                text=f"{self.name}: {map_type_to_pydantic_model(self.value)}"
            )
        )


class PythonComment(BaseModel):
    content: List[str]
    python_doc: bool = False

    def __init__(self, lines: List[str], python_doc: bool = False):
        super().__init__(content=lines, python_doc=python_doc)

    def transform(self) -> SourceNode:
        if self.python_doc:
            lines = [LineNode(TextNode(line)) for line in self.content]
            return BracesNode(value=BulkNode(lines), open='"""', close='"""')
        else:
            lines = [LineNode(TextNode("# " + line)) for line in self.content]
            return BulkNode(lines)


class PythonClass(BaseModel):
    name: str
    fields: List[PythonField]
    comment: PythonComment = None
    model: Optional[str]

    @staticmethod
    def parse_name(name):
        return to_pascal_case(name)

    def __init__(self, raw: DyType, **kwargs):
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

    def transform(self, data_model) -> SourceNode:
        sources = []
        if self.model:
            model = self.model
        elif data_model == "pydantic":
            model = "pydantic.BaseModel"
        elif data_model == "peewee":
            # model = "peewee.Model"
            model = "BaseModel"
        else:
            raise Exception("Unrecognized data model: " + data_model)
        sources.append(TextNode(f"class {self.name}({model})"))
        in_indent_block = []
        in_indent_block.append(self.comment.transform())
        if len(self.fields) == 0:
            in_indent_block.append(LineNode(TextNode("pass")))
        for field in self.fields:
            if data_model == "pydantic":
                in_indent_block.append(field.transform_pydantic())
            elif data_model == "peewee":
                in_indent_block.append(field.transform_peewee())
            else:
                raise Exception("Unrecognized data model: " + data_model)
        sources.append(BracesNode(value=BulkNode(in_indent_block), open=":", close=""))

        return BulkNode(sources)


class PythonEnum(BaseModel):
    name: str
    variants: List[DyType]
    model: str = "enum.Enum"
    raw: DyType = None

    @staticmethod
    def parse_name(name):
        return to_pascal_case(name)

    def __init__(self, raw: DyType = None, **kwargs):

        if raw:
            kwargs.update(
                {
                    "raw": raw,
                    "name": PythonEnum.parse_name(raw.get_field(Traits.TypeName)),
                    "variants": raw.get_field(Traits.Variants),
                }
            )

        super().__init__(**kwargs)

    def transform(self) -> SourceNode:
        sources = []
        sources.append(TextNode(f"class {self.name}({self.model})"))

        in_indent_block = []

        if len(self.variants) == 0:
            in_indent_block.append(LineNode(TextNode("pass")))
        for field in self.variants:
            name = field.get_field(Traits.VariantNames)

            in_indent_block.append(
                LineNode(
                    content=TextNode(
                        text="{lname} = '{rname}'".format(
                            lname=map_field_name(name[0]), rname=name[0]
                        )
                    )
                )
            )

        sources.append(BracesNode(value=BulkNode(in_indent_block), open=":", close=""))

        return BulkNode(sources)


class StructRegistry:
    def __init__(self):
        self.structs: List[PythonClass] = []

    def add_struct(self, struct: PythonClass):
        if struct not in self.structs:
            self.structs.append(struct)


def find_all_structs_impl(reg: StructRegistry, s: DyType):
    if s.get_field(Traits.Struct):
        reg.add_struct(PythonClass(s))
    else:
        raise NotImplementedError()


@beartype
def find_all_structs(s: DyType) -> List[PythonClass]:
    reg = StructRegistry()
    find_all_structs_impl(reg, s)
    return reg.structs


def emit_struct(root: DyType, data_model) -> SourceNode:
    sources = []
    for python_struct in find_all_structs(root):
        sources.append(python_struct.transform(data_model))
    return BulkNode(sources)


def emit_python_model_definition(root: ModelDefinition, data_model) -> SourceNode:
    sources = []
    comment = []
    for attr in ["type", "url", "ref", "note"]:
        t = getattr(root, attr)
        if t:
            comment.extend(f"{attr}: {t}".splitlines())
    comment = PythonComment(comment, python_doc=True)
    parsed = root.get_parsed()
    if parsed.get_field(Traits.Struct):
        for i, struct in enumerate(find_all_structs(parsed)):
            python_struct = struct
            if i == 0:
                python_struct.comment = comment
            sources.append(python_struct.transform(data_model))
    elif parsed.get_field(Traits.Enum):
        python_struct = PythonEnum(parsed)
        sources.append(python_struct.transform())
    else:
        raise Exception("must be a struct or enum", root)

    return BulkNode(sources)


class PythonPeeweeEmitter(Emitter):
    def accept(self, s: str) -> bool:
        return s == "python_peewee"

    def emit_model(self, target: str, model: ModelDefinition) -> str:
        formatter = StructuredFormatter(
            nodes=[emit_python_model_definition(model, "peewee")]
        )
        return formatter.to_string()

    def emit_type(self, target: str, ty: DyType) -> str:
        formatter = StructuredFormatter(nodes=[emit_struct(ty, "peewee")])
        return formatter.to_string()


class PythonPydanticEmitter(Emitter):
    def accept(self, s: str) -> bool:
        return s == "python_pydantic"

    def emit_model(self, target: str, model: ModelDefinition) -> str:
        formatter = StructuredFormatter(
            nodes=[emit_python_model_definition(model, "pydantic")]
        )
        return formatter.to_string()

    def emit_type(self, target: str, ty: DyType) -> str:
        formatter = StructuredFormatter(nodes=[emit_struct(ty, "pydantic")])
        return formatter.to_string()

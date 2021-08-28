from typing import List

from pydantic import BaseModel

from formatter import IndentedWriter, Formatee, Function, IndentBlock
from models import type_model, config_model
from models.type_model import Type, Traits


def map_type_to_peewee_model(ty: Type, args='') -> str:
    if ty.get_trait(Traits.Nullable):
        args += 'null=True'
    if ty.get_trait(Traits.Primary):
        args += 'primary=True'

    if ty.get_trait(Traits.Bool):
        return 'BoolField({})'.format(args)
    elif ty.get_trait(Traits.TsUnit):
        return 'DateTimeField()'
    elif ty.get_trait(Traits.Integer):
        bits = ty.get_trait(Traits.BitSize)
        if bits < 32:
            return 'SmallIntegerField({})'.format(args)
        elif bits == 32:
            return 'IntegerField({})'.format(args)
        elif bits > 32:
            return 'BigIntegerField({})'.format(args)
        else:
            raise NotImplementedError()

    elif ty.get_trait(Traits.Floating):
        bits = ty.get_trait(Traits.BitSize)
        if bits == 32:
            return 'FloatField({})'.format(args)
        elif bits == 64:
            return 'DoubleField({})'.format(args)
        else:
            return NotImplementedError()
    elif ty.get_trait(Traits.String) or ty.get_trait(Traits.Null):
        return 'TextField()'
    elif ty.get_trait(Traits.Enum):
        if ty.get_trait(Traits.SimpleEnum):
            return 'TextField({})'.format(args)
        else:
            return 'BinaryJSONField({})'.format(args)

    raise Exception("Cannot map type {} to str".format(ty))


PYTHON_KEYWORDS = {
    'and': 'and_',
    'as': 'as_',
    'assert': 'assert_',
    'break': 'break_',
    'class': 'class_',
    'continue': 'continue_',
    'def': 'def_',
    'del': 'del_',
    'elif': 'elif_',
    'else': 'else_',
    'except': 'except_',
    'False': 'False_',
    'finally': 'finally_',
    'for': 'for_',
    'from': 'from_',
    'global': 'global_',
    'if': 'if_',
    'import': 'import_',
    'in': 'in_',
    'is': 'is_',
    'lambda': 'lambda_',
    'None': 'None_',
    'nonlocal': 'nonlocal_',
    'not': 'not_',
    'or': 'or_',
    'pass': 'pass_',
    'raise': 'raise_',
    'return': 'return_',
    'True': 'True_',
    'try': 'try_',
    'while': 'while_',
    'with': 'with_',
    'yield': 'yield_'
}


def map_field_name(name: str) -> str:
    if not name[0].isalpha() and name[0] != '_':
        return '_' + name
    return PYTHON_KEYWORDS.get(name) or name


class PythonField(Formatee, BaseModel):
    name: str
    original_name: str = None
    value: type_model.Type

    def __init__(self, f: Type = None, **kwargs):
        if f:
            kwargs.update({
                'name': map_field_name(f.get_trait(Traits.Name)),
                'original_name': f.get_trait(Traits.Name),
                'value': f,
            })

        super().__init__(**kwargs)

    def format_with(self, writer: IndentedWriter):
        writer.append_line(f'{self.name} = {map_type_to_peewee_model(self.value)}')


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
                writer.append_line('# ' + line)


class PythonStruct(Formatee, BaseModel):
    name: str
    fields: List[PythonField]
    comment: PythonComment = None
    model: str = 'BaseModel'

    @staticmethod
    def parse_name(name):
        return stringcase.pascalcase(name)

    def __init__(self, raw: Type, **kwargs):
        if raw:
            kwargs.update({
                'name': PythonStruct.parse_name(raw.get_trait(Traits.Name)),
                'fields': [PythonField(f) for f in raw.get_traits(Traits.StructField)],
            })

        super().__init__(**kwargs)

    def format_with(self, writer: IndentedWriter):
        writer.append(f'class {self.name}({self.model})')

        def for_field(writer1: IndentedWriter):
            self.comment.format_with(writer1)
            for field in self.fields:
                field.format_with(writer1)

        IndentBlock(Function(for_field)).format_with(writer)


class PythonEnum(Formatee, BaseModel):
    name: str
    variants: List[Type]
    model: str = 'enum.Enum'
    raw: Type = None

    @staticmethod
    def parse_name(name):
        return stringcase.pascalcase(name)

    def __init__(self, raw: Type = None, **kwargs):

        if raw:
            kwargs.update({
                'raw': raw,
                'name': PythonEnum.parse_name(raw.get_trait(Traits.Name)),
                'variants': raw.get_traits(Traits.Variant),
            })

        super().__init__(**kwargs)

    def format_with(self, writer: IndentedWriter):
        writer.append(f'class {self.name}({self.model})')

        def for_field(writer1: IndentedWriter):
            for field in self.variants:
                name = field.get_trait(Traits.VariantName)
                writer1.append_line(
                    '{lname} = \'{rname}\''.format(lname=map_field_name(name), rname=name))

        IndentBlock(Function(for_field)).format_with(writer)


class StructRegistry:
    def __init__(self):
        self.structs: List[PythonStruct] = []

    def add_struct(self, struct: PythonStruct):
        if struct not in self.structs:
            self.structs.append(struct)


def find_all_structs_impl(reg: StructRegistry, s: Type):
    if s.get_trait(Traits.Struct):
        reg.add_struct(s)
    else:
        raise NotImplementedError()


def find_all_structs(s: Type) -> List[PythonStruct]:
    reg = StructRegistry()
    find_all_structs_impl(reg, s)
    return reg.structs


def emit_struct(root: Type) -> str:
    writer = IndentedWriter()
    for s in find_all_structs(root):
        python_struct = PythonStruct(s)
        python_struct.format_with(writer)
    return writer.to_string()


def emit_rust_model_definition(root: config_model.ModelDefinition) -> str:
    writer = IndentedWriter()
    comment = PythonComment(
        f'''type: {root.type}
url: {root.url}
ref: {root.ref}
note: {root.note}
''', python_doc=True)
    parsed = root.get_parsed()
    if parsed.get_trait(Traits.Struct):
        for i, struct in enumerate(find_all_structs(parsed)):
            python_struct = PythonStruct(struct)
            if i == 0:
                python_struct.comment = comment
            python_struct.format_with(writer)
    elif parsed.get_trait(Traits.Enum):
        python_struct = PythonEnum(parsed)
        python_struct.format_with(writer)
    else:
        raise Exception('must be a struct or enum', root)

    return writer.to_string()

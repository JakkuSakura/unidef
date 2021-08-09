import sys
import traceback
from enum import Enum
from typing import List

import stringcase
from beartype import beartype
from pydantic import BaseModel

from formatter import IndentedWriter, Formatee, Function, Braces, Text
from models import config_model
from models.sql_model import emit_schema_from_model
from models.type_model import to_second_scale, Type, Traits, Types


class ProcMacro(Formatee, BaseModel):
    pass


class SerdeAs(ProcMacro):
    serde_as: str = ''

    def format_with(self, writer: IndentedWriter):
        if not self.serde_as:
            writer.append_line('#[serde_as]')
        else:
            writer.append_line(f'#[serde_as(as = "{self.serde_as}")]')


SERDE_AS = SerdeAs()
SERDE_AS_DISPLAY_FROM_STR = SerdeAs(serde_as='DisplayFromStr')


class Derive(ProcMacro):
    enabled: List[str]

    def __init__(self, enabled: List[str]):
        super().__init__(enabled=enabled)

    def format_with(self, writer: IndentedWriter):
        writer.append_line('#[derive({})]'.format(', '.join(self.enabled)))


DEFAULT_DERIVE = Derive(['Clone', 'Debug', 'Serialize', 'Deserialize', 'PartialEq'])
ENUM_DEFAULT_DERIVE = Derive(['Copy', 'Clone', 'Debug', 'Serialize', 'Deserialize', 'PartialEq',
                              'Eq', 'EnumString', 'Display'])


class Serde(ProcMacro):
    tag: str = ''
    rename: List[str] = ''
    rename_all: str = ''

    def format_with(self, writer: IndentedWriter):
        pairs = []
        if self.tag:
            pairs.append(f'tag = "{self.tag}"')
        for rename in self.rename:
            pairs.append(f'rename = "{rename}"')
        if self.rename_all:
            pairs.append(f'rename_all = "{self.rename_all}"')

        writer.append_line('#[serde({})]'.format(','.join(pairs)))


class Strum(ProcMacro):
    serialize: List[str]

    def format_with(self, writer: IndentedWriter):
        pairs = ['serialize = "{}"'.format(s) for s in self.serialize]
        writer.append_line('#[strum({})]'.format(','.join(pairs)))


class AccessModifier(Enum):
    PRIVATE = ''
    PUBLIC = 'pub '


def map_type_to_rust(ty: Type) -> str:
    # if ty.get_trait(Traits.ValueType):
    #     return map_type_to_str(ty.get_trait(Traits.ValueType))
    if ty.get_trait(Traits.Nullable):
        ty = ty.copy()
        ty.remove_trait(Traits.Nullable)
        return 'Option<{}>'.format(map_type_to_rust(ty))

    if ty.get_trait(Traits.TsUnit):
        return 'TimeStamp' + stringcase.pascalcase(ty.get_trait(Traits.TsUnit))

    if ty.get_trait(Traits.Struct):
        return RustStruct.parse_name(ty.get_trait(Traits.Name))
    elif ty.get_trait(Traits.Enum):
        return RustEnum.parse_variant_name(ty.get_trait(Traits.TypeRef))
    elif ty.get_trait(Traits.Tuple):
        return '({})'.format(', '.join([map_type_to_rust(t) for t in ty.get_traits(Traits.TupleField)]))
    elif ty.get_trait(Traits.Vector):
        return 'Vec<{}>'.format(map_type_to_rust(ty.get_trait(Traits.ValueType)))
    elif ty.get_trait(Traits.Bool):
        return 'bool'
    elif ty.get_trait(Traits.Integer):
        bits = ty.get_trait(Traits.BitSize)
        if ty.get_trait(Traits.Signed):
            return 'i' + str(bits)
        else:
            return 'u' + str(bits)

    elif ty.get_trait(Traits.Floating):
        bits = ty.get_trait(Traits.BitSize)
        return 'f' + str(bits)
    elif ty.get_trait(Traits.Map):
        return 'HashMap<{}, {}>'.format(map_type_to_rust(ty.get_trait(Traits.KeyType)),
                                        map_type_to_rust(ty.get_trait(Traits.ValueType)))
    elif ty.get_trait(Traits.String):
        if ty.get_trait(Traits.Reference):
            lifetime = ty.get_trait(Traits.Lifetime)
            return '&{}str'.format(lifetime and "'" + lifetime + ' ' or '')
        else:
            return 'String'
    elif ty.get_trait(Traits.Unit):
        return '()'
    elif ty.get_trait(Traits.TypeRef):
        return ty.get_trait(Traits.TypeRef)
    raise Exception("Cannot map type {} to str".format(ty))


RUST_KEYWORDS = {
    'as': 'r#as',
    'break': 'r#break',
    'const': 'r#const',
    'continue': 'r#continue',
    'crate': 'r#crate',
    'else': 'r#else',
    'enum': 'r#enum',
    'extern': 'r#extern',
    'false': 'r#false',
    'fn': 'r#fn',
    'for': 'r#for',
    'if': 'r#if',
    'impl': 'r#impl',
    'in': 'r#in',
    'let': 'r#let',
    'loop': 'r#loop',
    'match': 'r#match',
    'mod': 'r#mod',
    'move': 'r#move',
    'mut': 'r#mut',
    'pub': 'r#pub',
    'ref': 'r#ref',
    'return': 'r#return',
    'self': 'r#self',
    'Self': 'r#Self',
    'static': 'r#static',
    'struct': 'r#struct',
    'super': 'r#super',
    'trait': 'r#trait',
    'true': 'r#true',
    'type': 'ty',
    'unsafe': 'r#unsafe',
    'use': 'r#use',
    'where': 'r#where',
    'while': 'r#while',
    'async': 'r#async',
    'await': 'r#await',
    'dyn': 'r#dyn',
}


def map_field_name(name: str) -> str:
    if name[0].isnumeric() and name[0] != '_':
        return '_' + name

    return RUST_KEYWORDS.get(name) or stringcase.snakecase(name)


class RustField(Formatee, BaseModel):
    name: str
    original_name: str = None
    access: AccessModifier = AccessModifier.PUBLIC
    value: Type
    val_in_str: bool = False

    @staticmethod
    def from_name(name: str, ty: str = ''):
        name = map_field_name(name)
        ty_or_name = ty or name
        return RustField(name=name, value=Type.from_str(ty_or_name).append_trait(Traits.TypeRef.init_with(ty_or_name)))

    def __init__(self, ty: Type = None, **kwargs):
        if ty:
            value = ty.get_trait(Traits.ValueType) or ty
            assert value is not None, 'Is not an valid field ' + repr(ty)
            kwargs.update({
                'name': map_field_name(ty.get_trait(Traits.Name)),
                'original_name': ty.get_trait(Traits.Name),
                'value': value,
                'val_in_str': value.get_trait(Traits.StringWrapped) or False
            })

        super().__init__(**kwargs)

    def format_with(self, writer: IndentedWriter):
        if self.val_in_str:
            SERDE_AS_DISPLAY_FROM_STR.format_with(writer)
        if self.original_name != self.name:
            # or self.original_name == self.name and len(self.name) < 3: # For binance's convenience

            Serde(rename=[self.original_name]).format_with(writer)
        writer.append_line(f'{self.access.value}{self.name}: {map_type_to_rust(self.value)},')


class RustComment(Formatee, BaseModel):
    content: List[str]
    cargo_doc: bool = False

    def __init__(self, content: str, cargo_doc: bool = False):
        super().__init__(content=content.splitlines(), cargo_doc=cargo_doc)

    def format_with(self, writer: IndentedWriter):
        if self.cargo_doc:
            for line in self.content:
                writer.append_line('/// ' + line)
                writer.append_line('/// ')
        else:
            for line in self.content:
                writer.append_line('// ' + line)


class RustStruct(Formatee, BaseModel):
    annotations: List[ProcMacro] = []
    access: AccessModifier = AccessModifier.PUBLIC
    name: str
    fields: List[RustField]
    raw: Type = None

    @staticmethod
    @beartype
    def parse_name(name: str):
        return stringcase.pascalcase(name)

    def __init__(self, raw: Type = None, **kwargs):
        if raw:
            annotations = [DEFAULT_DERIVE]

            kwargs.update({
                'raw': raw,
                'name': RustStruct.parse_name(raw.get_trait(Traits.Name)),
                'fields': [RustField(f) for f in raw.get_traits(Traits.StructField)],
                'annotations': annotations
            })

        super().__init__(**kwargs)
        for field in self.fields:
            if field.val_in_str:
                self.annotations.insert(0, SERDE_AS)
                break

    def format_with(self, writer: IndentedWriter):
        for anno in self.annotations:
            anno.format_with(writer)

        writer.append(f'{self.access.value}struct {self.name} ')

        def for_field(writer1: IndentedWriter):
            for field in self.fields:
                field.format_with(writer1)

        Braces(Function(for_field)).format_with(writer)


class RustEnum(Formatee, BaseModel):
    annotations: List[ProcMacro] = []
    access: AccessModifier = AccessModifier.PUBLIC
    name: str
    variants: List[Type]
    raw: Type = None

    @staticmethod
    def parse_variant_name(name: str):
        if name.isupper():
            return name
        else:
            return stringcase.pascalcase(name)

    def __init__(self, raw: Type = None, **kwargs):
        if raw:
            annotations = [ENUM_DEFAULT_DERIVE]

            kwargs.update({
                'raw': raw,
                'name': RustStruct.parse_name(raw.get_trait(Traits.Name)),
                'variants': list(raw.get_traits(Traits.Variant)),
                'annotations': annotations
            })

        super().__init__(**kwargs)

    def format_with(self, writer: IndentedWriter):
        for anno in self.annotations:
            anno.format_with(writer)

        writer.append(f'{self.access.value}enum {self.name} ')

        def for_field(writer1: IndentedWriter):
            for field in self.variants:
                name = list(field.get_traits(Traits.VariantName))
                mapped = map_field_name(name[0])
                if len(name) > 1 or mapped != name[0]:
                    reversed_names = name[:]
                    reversed_names.reverse()
                    Strum(serialize=reversed_names).format_with(writer)

                writer1.append_line(mapped + ',')

        Braces(Function(for_field)).format_with(writer)


class StructRegistry:
    def __init__(self):
        self.structs: List[Type] = []

    def add_struct(self, struct: Type):
        if struct not in self.structs:
            self.structs.append(struct)


class RustFunc(Formatee, BaseModel):
    name: str
    access: AccessModifier = AccessModifier.PUBLIC
    args: List[RustField]
    ret: Type
    content: str

    def format_with(self, writer: IndentedWriter):
        writer.append(self.access.value + ' fn ' + self.name)

        def for_arg(writer1: IndentedWriter):
            for arg in self.args:
                if arg.value.get_trait(Traits.TypeRef) and 'self' in arg.value.get_trait(Traits.Name):
                    writer1.append(arg.name + ', ')
                else:
                    writer1.append(arg.name + ': ' + map_type_to_rust(arg.value) + ', ')

        Braces(Function(for_arg), open='(', close=')', new_line=False).format_with(writer)
        writer.append(' -> ' + map_type_to_rust(self.ret) + ' ')
        Braces(Text(self.content)).format_with(writer)


class RustImpl(Formatee, BaseModel):
    name: str
    trait: str = ''
    functions: List[RustFunc]

    def format_with(self, writer: IndentedWriter):
        if self.trait:
            writer.append(f'''impl {self.trait} for {self.name} ''')
        else:
            writer.append(f'''impl {self.name} ''')

        def for_field(writer1: IndentedWriter):
            for func in self.functions:
                if self.trait:
                    func.access = AccessModifier.PRIVATE
                func.format_with(writer1)

        Braces(Function(for_field)).format_with(writer)


def find_all_structs_impl(reg: StructRegistry, s: Type):
    if s.get_trait(Traits.Struct):
        reg.add_struct(s)
        for field in s.get_traits(Traits.StructField):
            find_all_structs_impl(reg, field)
    elif s.get_trait(Traits.ValueType):
        find_all_structs_impl(reg, s.get_trait(Traits.ValueType))


def find_all_structs(s: Type) -> List[Type]:
    reg = StructRegistry()
    find_all_structs_impl(reg, s)
    return reg.structs


def sql_model_get_sql_ddl(struct: RustStruct) -> RustFunc:
    return RustFunc(name='get_sql_ddl', args=[],
                    ret=Types.String.copy().append_trait(Traits.Reference).append_trait(
                        Traits.Lifetime.init_with('static')),
                    content=f'r#"{emit_schema_from_model(struct.raw)}"#')


def sql_model_get_value_inner(f: RustField) -> str:
    if f.value.get_trait(Traits.Nullable):
        # TODO: not complete
        return 'self.{}.map(|x| x.to_string()).unwrap_or("NULL".to_owned())'.format(f.name)
    elif f.value.get_trait(Traits.Enum):
        if f.value.get_trait(Traits.SimpleEnum):
            return 'self.{}'.format(f.name)
        else:
            return 'serde_json::to_string(&self.{}).unwrap()'.format(f.name)
    elif f.value.get_trait(Traits.TsUnit):
        return 'self.{}.val() as f64 * {}'.format(f.name, to_second_scale(f.value.get_trait(Traits.TsUnit)))
    elif f.value.get_trait(Traits.Struct) or f.value.get_trait(Traits.Vector) or f.value.get_trait(Traits.Tuple):
        return 'serde_json::to_string(&self.{}).unwrap()'.format(f.name)
    else:
        return 'self.{}'.format(f.name)


def sql_model_get_values_inner(struct: RustStruct) -> List[str]:
    values = []
    for f in struct.fields:
        values.append(sql_model_get_value_inner(f))
    return values


def sql_model_field_names_in_format(struct: RustStruct) -> str:
    fields = []
    for field in struct.fields:
        if field.value.get_trait(Traits.SimpleEnum):
            fields.append("'{%s:?}'" % field.name)
        elif field.value.get_trait(Traits.String) or field.value.get_trait(Traits.Enum) or field.value.get_trait(
                Traits.Struct):
            fields.append("'{%s}'" % field.name)
        elif field.value.get_trait(Traits.TsUnit):
            fields.append('to_timestamp({%s})' % field.name)
        else:
            fields.append('{%s}' % field.name)
    return ','.join(fields)


def sql_model_get_insert_into_sql(struct: RustStruct) -> RustFunc:
    values = sql_model_get_values_inner(struct)

    field_names = ','.join([field.name for field in struct.fields])
    field_names_in_format = sql_model_field_names_in_format(struct)
    arguments = ','.join(
        ['%s = %s' % (r_field.name, v) for (r_field, v) in zip(struct.fields, values)])
    return RustFunc(name='get_insert_into_sql',
                    args=[RustField.from_name('&self'),
                          RustField(name="table", value=Types.String.copy().append_trait(Traits.Reference))],
                    ret=Types.String,
                    content=
                    f'''format!(r#"INSERT INTO {{table}} ({field_names}) VALUES ({field_names_in_format});"#,
                     table=table, {arguments})'''
                    )


def sql_model_get_fields_sql(struct: RustStruct) -> RustFunc:
    field_names = ','.join([field.name for field in struct.fields])
    return RustFunc(name='get_fields_sql',
                    args=[RustField.from_name('&self')],
                    ret=Types.String.copy().append_trait(Traits.Reference).append_trait(
                        Traits.Lifetime.init_with('static')),
                    content=
                    f'''r#"{field_names}"#'''
                    )


def sql_model_get_values_sql(struct: RustStruct) -> RustFunc:
    values = sql_model_get_values_inner(struct)

    field_names_in_format = sql_model_field_names_in_format(struct)
    return RustFunc(name='get_values_sql',
                    args=[RustField.from_name('&self')],
                    ret=Types.String,
                    content=
                    f'''format!(r#"{field_names_in_format}"#, 
                   {','.join(['%s = %s' % (r_field.name, v) for (r_field, v) in
                              zip(struct.fields, values)])})'''
                    )


def sql_model_trait(struct: RustStruct, writer: IndentedWriter):
    functions = [
        sql_model_get_sql_ddl(struct),
        sql_model_get_insert_into_sql(struct),
        sql_model_get_values_sql(struct),
        sql_model_get_fields_sql(struct)
    ]

    RustImpl(name=struct.name, trait='SqlModel', functions=functions).format_with(writer)


def from_sql_raw_func(struct: RustStruct) -> RustFunc:
    values = []
    for f in struct.fields:
        values.append(f'''row.get("{f.name}")''')

    content = f'''
     {struct.name} {{
        {','.join(['%s: %s' % (field.name, v) for (field, v) in zip(struct.fields, values)])}
     }}
    '''
    return RustFunc(name='from',
                    args=[RustField.from_name(name='row', ty='Row')],
                    ret=Type.from_str('Self').append_trait(Traits.TypeRef.init_with('Self')),
                    content=content
                    )


def from_sql_raw_trait(struct: RustStruct, writer: IndentedWriter):
    for s in struct.fields:
        if s.value.get_trait(Traits.Enum) or s.value.get_trait(Traits.Struct) or (
                s.value.get_trait(Traits.Integer) and not s.value.get_trait(Traits.Signed)) \
                or s.value.get_trait(Traits.TsUnit):
            print('Do not support', s.value, 'yet, skipping From<Row>', file=sys.stderr)
            return
    functions = [
        from_sql_raw_func(struct),
    ]

    RustImpl(name=struct.name, trait='From<Row>', functions=functions).format_with(writer)


def raw_data_func(raw: str) -> RustFunc:
    return RustFunc(name='get_raw_data', args=[], ret=Types.String.copy().append_trait(Traits.Reference).append_trait(
        Traits.Lifetime.init_with('static')),
                    content=f'r#"{raw}"#')


def emit_rust_model_definition(root: config_model.ModelDefinition) -> str:
    writer = IndentedWriter()
    RustComment(
        f'''type: {root.type}
url: {root.url}
ref: {root.ref}
note: {root.note}
''', cargo_doc=True).format_with(writer)
    parsed = root.get_parsed()
    if parsed.get_trait(Traits.Struct):
        for struct in find_all_structs(parsed):
            if struct.get_trait(Traits.TypeRef):
                continue
            rust_struct = RustStruct(struct)
            rust_struct.format_with(writer)
            funcs = [
                raw_data_func(root.raw)
            ]
            RustImpl(name=rust_struct.name, functions=funcs).format_with(writer)
            backup = writer.clone()
            try:
                sql_model_trait(rust_struct, writer)
                from_sql_raw_trait(rust_struct, writer)
            except Exception as e:
                print('Error happened while generating sql_model_trait, skipping.', str(e), traceback.format_exc(),
                      file=sys.stderr)
                writer = backup
    elif parsed.get_trait(Traits.Enum):
        rust_enum = RustEnum(parsed)
        rust_enum.format_with(writer)
    else:
        raise Exception('must be a struct or enum', root)

    return try_rustfmt(writer.to_string())


def try_rustfmt(s: str):
    import subprocess
    import sys
    try:
        rustfmt = subprocess.Popen(["rustfmt"], stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        rustfmt.stdin.write(s.encode())
        rustfmt.stdin.close()
        parsed = rustfmt.stdout.read().decode()
        error = rustfmt.stderr.read().decode()
        if error:
            print('Error when formatting with rustfmt: ', error, file=sys.stderr)
            return s
        else:
            return parsed
    except Exception as e:
        print('Error while trying to use rustfmt, defaulting to raw', repr(e), file=sys.stderr)
        return s

from models.type_model import Type, Traits
from utils.name_convert import *

def get_real(ty: Type) -> str:
    assert ty.get_trait(Traits.Floating), True
    bits = ty.get_trait(Traits.BitSize)
    if bits == 32:
        return 'float'
    elif bits == 64:
        return 'double precision'
    else:
        raise NotImplementedError()


def get_integer(ty: Type) -> str:
    assert ty.get_trait(Traits.Integer), True
    bits = ty.get_trait(Traits.BitSize)

    if bits < 32:
        return 'smallint'
    elif bits == 32:
        return 'int'
    elif bits > 32:
        return 'bigint'
    else:
        raise NotImplementedError()


def map_type_to_ddl(ty: Type) -> str:
    assert ty is not None
    if ty.get_trait(Traits.Floating):
        return get_real(ty)

    if ty.get_trait(Traits.TsUnit):
        return 'timestamp without time zone'

    if ty.get_trait(Traits.Integer):
        return get_integer(ty)

    if ty.get_trait(Traits.String) or ty.get_trait(Traits.Null):
        return 'text'

    if ty.get_trait(Traits.Bool):
        return 'bool'

    if ty.get_trait(Traits.Struct):
        return 'jsonb'

    if ty.get_trait(Traits.Enum):
        if ty.get_trait(Traits.SimpleEnum):
            return 'text'
        else:
            return 'jsonb'
    raise Exception('Cannot map to sql type', ty)


def get_field(field: Type) -> str:
    base = to_snake_case(field.get_trait(Traits.Name)) + ' ' + map_type_to_ddl(
        field.get_trait(Traits.ValueType) or field)
    if field.get_trait(Traits.Primary):
        base += ' primary key'

    if not field.get_trait(Traits.Nullable):
        base += ' not null'

    return base


def emit_schema_from_model(model: Type) -> str:
    fields = ',\n'.join([get_field(field) for field in model.get_traits(Traits.StructField)])
    return fields


def emit_field_names_from_model(model: Type) -> str:
    fields = ','.join([field.get_trait(Traits.Name) for field in model.get_traits(Traits.StructField)])
    return fields


def main():
    from models.config_model import read_model_definition
    import argparse

    parser = argparse.ArgumentParser(description='Export rust source code')
    parser.add_argument('file', type=str, help='input file')

    args = parser.parse_args()

    for loaded_model in read_model_definition(open(args.file)):
        s = emit_schema_from_model(loaded_model.get_parsed())
        print(s)
        print()


if __name__ == '__main__':
    main()

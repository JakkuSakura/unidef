from models.type_model import Type, Traits


def get_real(ty: Type) -> str:
    assert ty.get_trait(Traits.Floating).value, True
    bits = ty.get_trait(Traits.Size).value
    if bits == 32:
        return 'float'
    elif bits == 64:
        return 'double precision'
    else:
        raise NotImplementedError()


def get_integer(ty: Type) -> str:
    assert ty.get_trait(Traits.Integer), True
    bits = ty.get_trait(Traits.Size)

    if bits < 32:
        return 'smallint'
    elif bits == 32:
        return 'int'
    elif bits > 32:
        return 'bigint'
    else:
        raise NotImplementedError()


def type_mapping(ty: Type) -> str:
    assert ty is not None
    if ty.get_trait(Traits.Floating):
        return get_real(ty)

    if ty.get_trait(Traits.TsUnit):
        return 'timestamp without time zone'

    if ty.get_trait(Traits.Integer):
        return get_integer(ty)

    if ty.get_trait(Traits.Name) == 'string':
        return 'text'

    if ty.get_trait(Traits.Name) == 'bool':
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
    base = field.get_trait(Traits.Name) + ' ' + type_mapping(field)
    if field.get_trait(Traits.Primary):
        base += ' primary key'

    if not field.get_trait(Traits.Nullable):
        base += ' not null'

    return base


def emit_schema_from_model(model: Type) -> str:
    fields = ',\n'.join([get_field(field.value) for field in model.traits if field.name == Traits.Field.name])
    return fields


def emit_field_names_from_model(model: Type) -> str:
    fields = ','.join([field.name for field in model.traits if field.name == Traits.Field.name])
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

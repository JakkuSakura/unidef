import random
from typing import Any, Union, Optional

import stringcase
from beartype import beartype
from pydantic import BaseModel


class Trait(BaseModel):
    name: str
    value: Any = None

    def initialize_with(self, value: Any) -> 'Trait':
        t = self.copy(deep=True)
        t.value = value
        return t

    @staticmethod
    @beartype
    def from_str(name: str) -> 'Trait':
        return Trait(name=name)


Trait.update_forward_refs()


class Traits:
    Name = Trait.from_str('name')
    Size = Trait.from_str('size')
    Signed = Trait.from_str('signed').initialize_with(True)
    Numeric = Trait.from_str('numeric').initialize_with(True)
    Integer = Trait.from_str('integer').initialize_with(True)
    KeyType = Trait.from_str('key')
    ValueType = Trait.from_str('value')
    Parent = Trait.from_str('parent')
    Field = Trait.from_str('field')
    Struct = Trait.from_str('struct')
    TypeRef = Trait.from_str('type_ref')
    Variant = Trait.from_str('variant')
    VariantName = Trait.from_str('variant_name')

    # Format
    StringWrapped = Trait.from_str('string_wrapped').initialize_with(True)
    TsUnit = Trait.from_str('ts_unit')

    # SQL related
    Primary = Trait.from_str('primary').initialize_with(True)
    Nullable = Trait.from_str('nullable').initialize_with(True)


class Type(BaseModel):
    """
    Type is the type model used in this program.
    It allows single inheritance and multiple traits, similar to those in Rust and Java, as used in many other languages.
    """
    traits: list[Trait] = []
    frozen: bool = False

    @beartype
    def with_trait(self, trait: Trait) -> 'Type':
        assert not self.frozen
        self.traits.append(trait)
        return self

    def get_first_trait(self, name: Trait) -> Trait:
        for t in self.traits:
            if t.name == name.name:
                return t

    @beartype
    def with_parent(self, parent: 'Type') -> 'Type':
        return self.with_trait(parent.as_parent())

    def as_parent(self) -> Trait:
        return Traits.Parent.initialize_with(self)

    @staticmethod
    @beartype
    def from_str(name: str) -> 'Type':
        return Type().with_trait(Traits.Name.initialize_with(name))

    def freeze(self) -> 'Type':
        self.frozen = True
        return self

    def copy(self, *args, **kwargs):
        kwargs['deep'] = True
        this = super().copy(*args, **kwargs)
        this.frozen = False
        return this

    def dict(self, **kwargs):
        kwargs["exclude"] = {'frozen'}
        return super().dict(**kwargs)


class Types:
    Bool = Type.from_str('bool').freeze()
    I64 = (Type.from_str('i64')
           .with_trait(Traits.Integer)
           .with_trait(Traits.Size.initialize_with(64))
           .with_trait(Traits.Signed.initialize_with(True))
           .freeze())
    String = Type.from_str('string').freeze()
    Float = (Type.from_str('f32')
             .with_trait(Traits.Numeric)
             .with_trait(Traits.Size.initialize_with(32))
             .with_trait(Traits.Signed.initialize_with(True))
             .freeze())

    Double = (Type.from_str('f64')
              .with_trait(Traits.Numeric)
              .with_trait(Traits.Size.initialize_with(64))
              .with_trait(Traits.Signed.initialize_with(True))
              .freeze())

    Vector = Type.from_str('vector').freeze()

    @staticmethod
    @beartype
    def field(name: str, ty: Type) -> Type:
        return Type.from_str(name).with_trait(Traits.ValueType.initialize_with(ty))

    @staticmethod
    @beartype
    def variant(name: list[str]) -> Type:
        ty = Type.from_str(name[0])
        for n in name:
            ty.with_trait(Traits.VariantName.initialize_with(n))
        return ty

    @staticmethod
    @beartype
    def struct(name: str, fields: list[Type]) -> Type:
        ty = Type.from_str(name)
        for f in fields:
            ty.with_trait(Traits.Field.initialize_with(f))
        return ty

    @staticmethod
    @beartype
    def enum(name: str, variants: list[Type]) -> Type:
        ty = Type.from_str(name)
        for f in variants:
            ty.with_trait(Traits.Variant.initialize_with(f))
        return ty

class TypeAlreadyExistsAndConflict(Exception):
    pass


class TraitAlreadyExistsAndConflict(Exception):
    pass


class TypeRegistry(BaseModel):
    types: dict[str, Type] = {}
    traits: dict[str, Trait] = {}

    def insert_type(self, ty: Type):
        name = ty.get_first_trait(Traits.Name).value
        assert ty.frozen, 'type should be frozen ' + name
        if name not in self.types:
            self.types[name] = ty
        elif self.types[name] != ty:
            raise TypeAlreadyExistsAndConflict(name)

    def insert_trait(self, trait: Trait):
        if trait.name not in self.traits:
            self.traits[trait.name] = trait
        elif self.traits[trait.name] != trait:
            raise TraitAlreadyExistsAndConflict(trait.name)

    def get_type(self, name: str) -> Optional[Type]:
        return self.types.get(name)

    def get_trait(self, name: str) -> Optional[Trait]:
        return self.traits.get(name)

    def is_subclass(self, child: Type, parent: Type) -> bool:
        assert isinstance(child, Type)
        assert isinstance(parent, Type)
        if parent.as_parent() in child.traits:
            return True
        p = child.get_first_trait(Traits.Parent).value
        return self.is_subclass(p, parent)

    def list_types(self) -> list[Type]:
        return self.types.values()

    def list_traits(self) -> list[Trait]:
        return self.traits.values()


GLOBAL_TYPE_REGISTRY = TypeRegistry()

for t in Traits.__dict__.values():
    if isinstance(t, Trait):
        GLOBAL_TYPE_REGISTRY.insert_trait(t)

for t in Types.__dict__.values():
    if isinstance(t, Type):
        GLOBAL_TYPE_REGISTRY.insert_type(t)


def to_second_scale(s: str) -> float:
    if s == 's':
        return 1.0
    elif s == 'ms':
        return 1e-3
    elif s == 'us':
        return 1e-6
    elif s == 'ns':
        return 1e-9
    raise Exception('Cannot convert {} to seconds'.format(s))


def detect_timestamp_unit(inputtext: Union[str, int, float]) -> str:
    inputtext = float(inputtext)

    if 10E7 <= inputtext < 18E7:
        raise Exception('Expected a more recent date? You are missing 1 digit.')

    if inputtext >= 1E16 or inputtext <= -1E16:
        return 'ns'
    elif inputtext >= 1E14 or inputtext <= -1E14:
        return 'us'
    elif inputtext >= 1E11 or inputtext <= -3E10:
        return 'ms'
    else:
        return 's'


def string_wrapped(t: Type) -> Type:
    return t.copy().with_trait(Traits.StringWrapped)


def prefix_join(prefix: str, name: str) -> str:
    if prefix:
        return prefix + '_' + name
    else:
        return name


def parse_data_example(obj: Union[str, int, float, dict, list], prefix: str = '') -> Type:
    if isinstance(obj, str):
        if '.' in obj:
            try:
                float(obj)
                return string_wrapped(Types.Float)
            except:
                pass
        try:
            int(obj)
            return string_wrapped(Types.I64)
        except:
            pass

        return Types.String
    elif isinstance(obj, bool):
        return Types.Bool
    elif isinstance(obj, int):
        prefix = stringcase.snakecase(prefix)
        ty = Types.I64

        if 'ts' in prefix or 'time' in prefix or 'at' in prefix:
            ty = ty.copy().with_trait(Traits.TsUnit.initialize_with(detect_timestamp_unit(obj)))

        return ty
    elif isinstance(obj, float):
        return Types.Double
    elif isinstance(obj, list):
        content = None
        if len(obj):
            content = parse_data_example(obj[0], prefix)
        return Types.Vector.copy().with_trait(Traits.ValueType.initialize_with(content))
    elif isinstance(obj, dict):
        fields = []
        for key, value in obj.items():
            value = parse_data_example(value, key)
            if value.get_first_trait(Traits.Struct):
                for name in value.traits:
                    if name.name == Traits.Name.name:
                        name.value = prefix_join(prefix, key)
            for val in value.traits:
                if val.name == Traits.ValueType.name:
                    if val.value.get_first_trait(Traits.Struct):
                        val.value.get_first_trait(Traits.Name).value = prefix_join(prefix, key) + 's'

            fields.append(Types.field(key, value))
        return Types.struct('struct_' + str(random.randint(0, 1000)), fields)


if __name__ == '__main__':
    GLOBAL_TYPE_REGISTRY.list_types()

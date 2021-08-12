import random
from typing import Any, Union, Optional, Iterator

import stringcase
from beartype import beartype
from pydantic import BaseModel


class Trait(BaseModel):
    name: str
    value: Any = None

    def init_with(self, value: Any) -> 'Trait':
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
    BitSize = Trait.from_str('bit_size')
    Signed = Trait.from_str('signed').init_with(True)
    KeyType = Trait.from_str('key')
    ValueType = Trait.from_str('value')
    Parent = Trait.from_str('parent')
    StructField = Trait.from_str('field').init_with(True)
    Struct = Trait.from_str('struct').init_with(True)
    Enum = Trait.from_str('enum').init_with(True)
    TypeRef = Trait.from_str('type_ref')
    Variant = Trait.from_str('variant')
    VariantName = Trait.from_str('variant_name')

    # Types
    Bool = Trait.from_str('bool').init_with(True)
    Numeric = Trait.from_str('numeric').init_with(True)
    Floating = Trait.from_str('floating').init_with(True)
    Integer = Trait.from_str('integer').init_with(True)
    String = Trait.from_str('string').init_with(True)
    TupleField = Trait.from_str('tuple_field')
    Tuple = Trait.from_str('tuple').init_with(True)
    Vector = Trait.from_str('vector').init_with(True)
    Map = Trait.from_str('map').init_with(True)
    Unit = Trait.from_str('unit').init_with(True)
    Null = Trait.from_str('null').init_with(True)

    # Format
    SimpleEnum = Trait.from_str('simple_enum')
    StringWrapped = Trait.from_str('string_wrapped').init_with(True)
    TsUnit = Trait.from_str('ts_unit')

    # SQL related
    Primary = Trait.from_str('primary').init_with(True)
    Nullable = Trait.from_str('nullable').init_with(True)

    # Rust related
    Reference = Trait.from_str('reference').init_with(True)
    Mutable = Trait.from_str('mutable').init_with(True)
    Lifetime = Trait.from_str('lifetime')


class Type(BaseModel):
    """
    Type is the type model used in this program.
    It allows single inheritance and multiple traits, similar to those in Rust and Java, as used in many other languages.
    """
    traits: list[Trait] = []
    frozen: bool = False

    @beartype
    def append_trait(self, trait: Trait) -> 'Type':
        assert not self.frozen
        self.traits.append(trait)
        return self

    @beartype
    def replace_trait(self, trait: Trait) -> 'Type':
        assert not self.frozen
        for i, t in enumerate(self.traits):
            if t.name == trait.name:
                self.traits[i] = trait
                return self
        self.traits.append(trait)
        return self

    @beartype
    def remove_trait(self, trait: Trait) -> 'Type':
        assert not self.frozen
        new = []
        for i, t in enumerate(self.traits):
            if t.name != trait.name:
                new.append(t)
        self.traits = new
        return self

    def get_trait(self, name: Trait) -> Any:
        for t in self.traits:
            if t.name == name.name:
                return t.value

    def get_traits(self, name: Trait) -> Iterator[Any]:
        for t in self.traits:
            if t.name == name.name:
                yield t.value

    @beartype
    def set_parent(self, parent: 'Type') -> 'Type':
        return self.replace_trait(parent.as_parent())

    def as_parent(self) -> Trait:
        return Traits.Parent.init_with(self)

    @staticmethod
    @beartype
    def from_str(name: str) -> 'Type':
        return Type().replace_trait(Traits.Name.init_with(name))

    def freeze(self) -> 'Type':
        self.frozen = True
        return self

    def copy(self, *args, **kwargs) -> 'Type':
        kwargs['deep'] = True
        this = super().copy(*args, **kwargs)
        this.frozen = False
        return this

    def dict(self, **kwargs):
        kwargs["exclude"] = {'frozen'}
        return super().dict(**kwargs)


class Types:
    Bool = Type.from_str('bool').append_trait(Traits.Bool).freeze()
    I16 = (Type.from_str('i16')
           .append_trait(Traits.Numeric)
           .append_trait(Traits.Integer)
           .append_trait(Traits.BitSize.init_with(16))
           .append_trait(Traits.Signed)
           .freeze())
    I32 = (Type.from_str('i32')
           .append_trait(Traits.Numeric)
           .append_trait(Traits.Integer)
           .append_trait(Traits.BitSize.init_with(32))
           .append_trait(Traits.Signed)
           .freeze())
    I64 = (Type.from_str('i64')
           .append_trait(Traits.Numeric)
           .append_trait(Traits.Integer)
           .append_trait(Traits.BitSize.init_with(64))
           .append_trait(Traits.Signed)
           .freeze())
    String = Type.from_str('string').append_trait(Traits.String).freeze()
    Float = (Type.from_str('f32')
             .append_trait(Traits.Numeric)
             .append_trait(Traits.Floating)
             .append_trait(Traits.BitSize.init_with(32))
             .append_trait(Traits.Signed)
             .freeze())

    Double = (Type.from_str('f64')
              .append_trait(Traits.Numeric)
              .append_trait(Traits.Floating)
              .append_trait(Traits.BitSize.init_with(64))
              .append_trait(Traits.Signed)
              .freeze())

    Vector = Type.from_str('vector').append_trait(Traits.Vector).freeze()

    NoneType = Type.from_str('none').append_trait(Traits.Nullable).append_trait(Traits.Null).freeze()

    @staticmethod
    @beartype
    def field(name: str, ty: Type) -> Type:
        return Type.from_str(name).append_trait(Traits.ValueType.init_with(ty))

    @staticmethod
    @beartype
    def variant(name: list[str]) -> Type:
        ty = Type.from_str(name[0])
        for n in name:
            ty.append_trait(Traits.VariantName.init_with(n))
        return ty

    @staticmethod
    @beartype
    def struct(name: str, fields: list[Type]) -> Type:
        ty = Type.from_str(name).append_trait(Traits.Struct)
        for f in fields:
            ty.append_trait(Traits.StructField.init_with(f))
        return ty

    @staticmethod
    @beartype
    def enum(name: str, variants: list[Type]) -> Type:
        ty = Type.from_str(name).append_trait(Traits.Enum)
        for f in variants:
            ty.append_trait(Traits.Variant.init_with(f))
        return ty


class TypeAlreadyExistsAndConflict(Exception):
    pass


class TraitAlreadyExistsAndConflict(Exception):
    pass


class TypeRegistry(BaseModel):
    types: dict[str, Type] = {}
    traits: dict[str, Trait] = {}
    type_detector: list = []

    def insert_type(self, ty: Type):
        name = ty.get_trait(Traits.Name)
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
        val = self.types.get(name)
        if val:
            return val
        for f in self.type_detector:
            val = f(name)
            if val:
                return val

    def get_trait(self, name: str) -> Optional[Trait]:
        return self.traits.get(name)

    def is_subclass(self, child: Type, parent: Type) -> bool:
        assert isinstance(child, Type)
        assert isinstance(parent, Type)
        if parent.as_parent() in child.traits:
            return True
        p = child.get_trait(Traits.Parent).value
        return self.is_subclass(p, parent)

    def list_types(self) -> list[Type]:
        return self.types.values()

    def list_traits(self) -> list[Trait]:
        return self.traits.values()


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
    return t.copy().replace_trait(Traits.StringWrapped)


def prefix_join(prefix: str, name: str) -> str:
    if prefix:
        return prefix + '_' + name
    else:
        return name


class CouldNotParseDataExample(Exception):
    pass


@beartype
def parse_data_example(obj: Union[str, int, float, dict, list, None], prefix: str = '') -> Type:
    if obj is None:
        return Types.NoneType

    if isinstance(obj, str):
        if '.' in obj:
            try:
                float(obj)
                return string_wrapped(Types.Double)
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

        if 'ts' in prefix or 'time' in prefix or '_at' in prefix:
            ty = ty.copy().replace_trait(Traits.TsUnit.init_with(detect_timestamp_unit(obj)))

        return ty
    elif isinstance(obj, float):
        return Types.Double
    elif isinstance(obj, list):
        content = None
        if len(obj):
            content = parse_data_example(obj[0], prefix)
        return Types.Vector.copy().replace_trait(Traits.ValueType.init_with(content))
    elif isinstance(obj, dict):
        fields = []
        for key, value in obj.items():
            value = parse_data_example(value, prefix_join(prefix, key))
            if value.get_trait(Traits.Struct):
                value.replace_trait(Traits.Name.init_with(prefix_join(prefix, key)))

            for val in value.get_traits(Traits.ValueType):
                if val.get_trait(Traits.Struct):
                    new_name = prefix_join(prefix, key)
                    if new_name.endswith('s'):
                        new_name = new_name[:-1]
                    val.replace_trait(Traits.Name.init_with(new_name))

            fields.append(Types.field(key, value))
        return Types.struct('struct_' + str(random.randint(0, 1000)), fields)
    raise CouldNotParseDataExample(str(obj))


def parse_type_definition(ty: str) -> Type:
    if ty.startswith('timestamp'):
        unit = ty.split('/')[1]
        ty = Types.I64.copy().replace_trait(Traits.TsUnit.init_with(unit))
        return ty

    if 'enum' in stringcase.snakecase(ty):
        ty_name = ty.split('/')[0]
        ty = Types.enum(ty_name, [])
        ty.append_trait(Traits.TypeRef.init_with(ty_name))
        return ty
    else:
        ty_name = ty
        ty = Types.struct(ty_name, [])
        ty.append_trait(Traits.TypeRef.init_with(ty_name))
        return ty


GLOBAL_TYPE_REGISTRY = TypeRegistry()

for t in Traits.__dict__.values():
    if isinstance(t, Trait):
        GLOBAL_TYPE_REGISTRY.insert_trait(t)

for t in Types.__dict__.values():
    if isinstance(t, Type):
        GLOBAL_TYPE_REGISTRY.insert_type(t)

GLOBAL_TYPE_REGISTRY.type_detector.append(parse_type_definition)

if __name__ == '__main__':
    GLOBAL_TYPE_REGISTRY.list_types()

from typing import Optional, Any, Dict, Union

from beartype import beartype
from pydantic import BaseModel
import stringcase
import random


# somehow useless
# class TypeMeta(BaseModel):
#     type_name: str
#     generics: list[Optional['TypeMeta']] = []
#
#     @staticmethod
#     @beartype
#     def from_str(name: str) -> 'TypeMeta':
#         return TypeMeta(type_name=name)
#
#     @staticmethod
#     @beartype
#     def from_generics(name: str, generics: list[Optional['TypeMeta']]) -> 'TypeMeta':
#         meta = TypeMeta(type_name=name, generics=generics)
#         return meta
#
#     def __hash__(self):
#         return hash(self.json())
# TypeMeta.update_forward_refs()


class Trait(BaseModel):
    name: str
    value: Any = None
    frozen: bool = False

    def initialize_with(self, value: Any) -> 'Trait':
        t = self.copy()
        t.value = value
        return t

    def freeze(self) -> 'Trait':
        self.frozen = True
        return self

    @staticmethod
    @beartype
    def from_str(name: str) -> 'Trait':
        return Trait(name=name)


Trait.update_forward_refs()


class Traits:
    Name = Trait.from_str('name')
    Size = Trait.from_str('size')
    Signed = Trait.from_str('signed')
    Numeric = Trait.from_str('numeric')
    Integer = Trait.from_str('integer')
    StringWrapped = Trait.from_str('string_wrapped')
    TsUnit = Trait.from_str('ts_unit')
    KeyType = Trait.from_str('key')
    ValueType = Trait.from_str('value')
    Parent = Trait.from_str('parent')
    Field = Trait.from_str('field')
    Struct = Trait.from_str('struct')


for v in Traits.__dict__.values():
    if isinstance(v, Trait):
        v.freeze()


class Type(BaseModel):
    """
    Type is the type model used in this program.
    It allows single inheritance and multiple traits, similar to those in Rust and Java, as used in many other languages.
    """
    traits: list[Trait] = []
    frozen: bool = False

    def with_trait(self, trait: Trait) -> 'Type':
        assert isinstance(trait, Trait)
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
        this = super().copy(*args, **kwargs)
        this.frozen = False
        return this


class TypeAlreadyExistsAndConflict(Exception):
    pass


class TypeRegistry(BaseModel):
    types: dict[str, Type] = {}

    def insert(self, model: Type):
        if model.type_meta not in self.types:
            self.types[model.type_meta] = model
        elif self.types[model.type_meta] != model:
            raise TypeAlreadyExistsAndConflict(model.type_meta)

    def get(self, meta: str) -> Type:
        return self.types.get(meta)

    def is_subclass(self, child: Type, parent: Type) -> bool:
        assert isinstance(child, Type)
        assert isinstance(parent, Type)
        if parent.as_parent() in child.traits:
            return True
        for i in child.traits:
            if is_parent_instance(i):
                p = self.get(i.value)
                if self.is_subclass(p, parent):
                    return True
        return False

    def list_types(self):
        for ty in self.types.values():
            print('Type', ty.json())


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
    def struct(name: str, fields: list[Type]) -> Type:
        ty = Type.from_str(name)
        for f in fields:
            ty.with_trait(Traits.Field.initialize_with(f))
        return ty


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
    return t.copy().with_trait(Traits.StringWrapped.as_trait())


GLOBAL_REGISTRY = TypeRegistry()

for v in Traits.__dict__.values():
    if isinstance(v, Type):
        GLOBAL_REGISTRY.insert(v)


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
    GLOBAL_REGISTRY.list_types()

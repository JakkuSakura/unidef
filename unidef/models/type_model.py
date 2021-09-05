import random
from unidef.utils.typing_compat import *

from beartype import beartype
from pydantic import BaseModel, PrivateAttr
from unidef.utils.name_convert import *


class Trait(BaseModel):
    name: str
    value: Any = None

    def default(self, value: Any) -> __qualname__:
        return self(value)

    def __call__(self, value: Any) -> __qualname__:
        t = self.copy(deep=True)
        t.value = value
        return t

    @classmethod
    @beartype
    def from_str(cls, name: str) -> __qualname__:
        return cls(name=name)

    def __repr__(self):
        return f'{self.name}: {self.value}'


Trait.update_forward_refs()


class Traits:
    TypeName = Trait.from_str('name')
    FieldName = Trait.from_str('field_name')
    BitSize = Trait.from_str('bit_size')
    Signed = Trait.from_str('signed').default(True)
    KeyType = Trait.from_str('key')
    ValueType = Trait.from_str('value')
    Parent = Trait.from_str('parent')
    StructField = Trait.from_str('field')
    Struct = Trait.from_str('struct').default(True)
    Enum = Trait.from_str('enum').default(True)
    TypeRef = Trait.from_str('type_ref')
    Variant = Trait.from_str('variant')
    VariantName = Trait.from_str('variant_name')
    RawValue = Trait.from_str('raw_value')
    LineComment = Trait.from_str('line_comment')
    Frozen = Trait.from_str('frozen').default(True)

    # Types
    Bool = Trait.from_str('bool').default(True)
    Numeric = Trait.from_str('numeric').default(True)
    Floating = Trait.from_str('floating').default(True)
    Integer = Trait.from_str('integer').default(True)
    String = Trait.from_str('string').default(True)
    TupleField = Trait.from_str('tuple_field')
    Tuple = Trait.from_str('tuple').default(True)
    Vector = Trait.from_str('vector').default(True)
    Map = Trait.from_str('map').default(True)
    Unit = Trait.from_str('unit').default(True)
    Null = Trait.from_str('null').default(True)

    # Format
    SimpleEnum = Trait.from_str('simple_enum')
    StringWrapped = Trait.from_str('string_wrapped').default(True)
    TsUnit = Trait.from_str('ts_unit')

    # SQL related
    Primary = Trait.from_str('primary').default(True)
    Nullable = Trait.from_str('nullable').default(True)

    # Rust related
    Reference = Trait.from_str('reference').default(True)
    Mutable = Trait.from_str('mutable').default(True)
    Lifetime = Trait.from_str('lifetime')
    Derive = Trait.from_str('derive')


class Type(BaseModel):
    """
    Type is the type model used in this program.
    It allows inheritance and multiple traits, similar to those in Rust and Java, as used in many other languages.
    """
    __root__: List[Trait] = []
    @property
    def traits(self):
        return self.__root__

    def __init__(self, **kwargs):
        super().__init__(**kwargs)

    @beartype
    def append_trait(self, trait: Trait) -> __qualname__:
        assert not self.is_frozen()
        self.traits.append(trait)
        return self

    @beartype
    def extend_traits(self, trait: Trait, values: Iterable[Any]) -> __qualname__:
        assert not self.is_frozen()
        for value in values:
            self.traits.append(trait.default(value))
        return self

    @beartype
    def replace_trait(self, trait: Trait) -> __qualname__:
        for i, t in enumerate(self.traits):
            if t.name == trait.name:
                self[i] = trait
                return self
        self.traits.append(trait)
        return self

    @beartype
    def remove_trait(self, trait: Trait) -> __qualname__:
        assert not self.is_frozen()
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

    def get_traits(self, name: Trait) -> List[Any]:
        traits = []
        for t in self.traits:
            if t.name == name.name:
                traits.append(t.value)
        return traits

    @classmethod
    @beartype
    def from_str(cls, name: str) -> __qualname__:
        return cls().replace_trait(Traits.TypeName(name))

    def is_frozen(self):
        return self.get_trait(Traits.Frozen)

    def freeze(self) -> __qualname__:
        return self.append_trait(Traits.Frozen)

    def copy(self, *args, **kwargs) -> __qualname__:
        kwargs['deep'] = True
        this = super().copy(*args, **kwargs)
        this.remove_trait(Traits.Frozen)
        return this

    def __str__(self):
        return f'{type(self).__name__}{self.traits}'

    def __repr__(self):
        return self.__str__()


Type.update_forward_refs()


def build_int(name: str) -> Type:
    ty = Type.from_str(name)

    ty.append_trait(Traits.Numeric)
    ty.append_trait(Traits.Integer)
    ty.append_trait(Traits.BitSize(int(name[1:])))

    if name.startswith('i'):
        ty.append_trait(Traits.Signed)
    else:
        ty.append_trait(Traits.Signed(False))

    return ty


def build_float(name: str) -> Type:
    return (Type.from_str(name)
            .append_trait(Traits.Numeric)
            .append_trait(Traits.Floating)
            .append_trait(Traits.BitSize(int(name[1:])))
            .append_trait(Traits.Signed))


class Types:
    Bool = Type.from_str('bool').append_trait(Traits.Bool).freeze()

    I8 = build_int('i8').freeze()
    I16 = build_int('i16').freeze()
    I32 = build_int('i32').freeze()
    I64 = build_int('i64').freeze()
    I128 = build_int('i128').freeze()

    U8 = build_int('u8').freeze()
    U16 = build_int('u16').freeze()
    U32 = build_int('u32').freeze()
    U64 = build_int('u64').freeze()
    U128 = build_int('u128').freeze()

    String = Type.from_str('string').append_trait(Traits.String).freeze()
    Float = build_float('f32').freeze()

    Double = build_float('f64').freeze()

    Vector = Type.from_str('vector').append_trait(Traits.Vector).freeze()

    NoneType = Type.from_str('none').append_trait(Traits.Nullable).append_trait(Traits.Null).freeze()

    @staticmethod
    @beartype
    def field(name: str, ty: Type) -> Type:
        return ty.append_trait(Traits.FieldName(name))

    @staticmethod
    @beartype
    def variant(name: List[str]) -> Type:
        ty = Type.from_str(name[0])
        for n in name:
            ty.append_trait(Traits.VariantName(n))
        return ty

    @staticmethod
    @beartype
    def struct(name: str, fields: List[Type]) -> Type:
        ty = Type.from_str(name).append_trait(Traits.Struct)
        for f in fields:
            ty.append_trait(Traits.StructField(f))
        return ty

    @staticmethod
    @beartype
    def enum(name: str, variants: List[Type]) -> Type:
        ty = Type.from_str(name).append_trait(Traits.Enum)
        for f in variants:
            ty.append_trait(Traits.Variant(f))
        return ty


class TypeAlreadyExistsAndConflict(Exception):
    pass


class TraitAlreadyExistsAndConflict(Exception):
    pass


class TypeRegistry(BaseModel):
    types: Dict[str, Type] = {}
    traits: Dict[str, Trait] = {}
    type_detector: list = []

    @beartype
    def insert_type(self, ty: Type):
        name = ty.get_trait(Traits.TypeName)
        assert ty.is_frozen(), f'type {name} should be frozen'
        if name not in self.types:
            self.types[name] = ty
        elif self.types[name] != ty:
            raise TypeAlreadyExistsAndConflict(name)

    @beartype
    def insert_trait(self, trait: Trait):
        if trait.name not in self.traits:
            self.traits[trait.name] = trait
        elif self.traits[trait.name] != trait:
            raise TraitAlreadyExistsAndConflict(trait.name)

    @beartype
    def get_type(self, name: str) -> Optional[Type]:
        val = self.types.get(name)
        if val:
            return val
        for f in self.type_detector:
            val = f(name)
            if val:
                return val

    @beartype
    def get_trait(self, name: str) -> Optional[Trait]:
        return self.__root__.get(name)

    @beartype
    def is_subclass(self, child: Type, parent: Type) -> bool:
        assert isinstance(child, Type)
        assert isinstance(parent, Type)
        if Traits.Parent(parent) in child.__root__:
            return True
        p = child.get_trait(Traits.Parent).value
        return self.is_subclass(p, parent)

    @beartype
    def list_types(self) -> List[Type]:
        return list(self.types.values())

    @beartype
    def list_traits(self) -> List[Trait]:
        return list(self.__root__.values())


@beartype
def to_second_scale(s: str) -> float:
    if s == 'sec' or s == 's':
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
    if abs(inputtext) < 1:
        return 'ms'
    if 10E7 <= inputtext < 18E7:
        raise Exception('Expected a more recent date? You are missing 1 digit.')

    if inputtext >= 1E16 or inputtext <= -1E16:
        return 'ns'
    elif inputtext >= 1E14 or inputtext <= -1E14:
        return 'us'
    elif inputtext >= 1E11 or inputtext <= -3E10:
        return 'ms'
    else:
        return 'sec'


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
    def inner(obj, prefix) -> Type:
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
            prefix = to_snake_case(prefix)

            ty = Types.I64
            # TODO: detect words in the field name without prefix
            if '_ts' in prefix or 'time' in prefix or '_at' in prefix:
                ty = ty.copy().append_trait(
                    Traits.TsUnit(detect_timestamp_unit(obj))
                ).replace_trait(
                    Traits.TypeName('timestamp')
                )

            return ty
        elif isinstance(obj, float):
            return Types.Double
        elif isinstance(obj, list):
            content = None
            if len(obj):
                content = parse_data_example(obj[0], prefix)
            return Types.Vector.copy().replace_trait(Traits.ValueType(content))
        elif isinstance(obj, dict):
            fields = []
            for key, value in obj.items():
                value = parse_data_example(value, prefix_join(prefix, key))
                if value.get_trait(Traits.Struct):
                    value.replace_trait(Traits.TypeName(prefix_join(prefix, key)))

                for val in value.get_traits(Traits.ValueType):
                    if val.get_trait(Traits.Struct):
                        new_name = prefix_join(prefix, key)
                        if new_name.endswith('s'):
                            new_name = new_name[:-1]
                        val.replace_trait(Traits.TypeName(new_name))

                fields.append(Types.field(key, value))
            return Types.struct('struct_' + str(random.randint(0, 1000)), fields)
        raise CouldNotParseDataExample(str(obj))

    return inner(obj, prefix).copy().append_trait(Traits.RawValue(obj))


def walk_type(node: Type, process: Callable[[int, Type], None], depth=0) -> None:
    if node.get_trait(Traits.Struct):
        for field in node.get_traits(Traits.StructField):
            process(depth, field)
            walk_type(field, process, depth + 1)
    if node.get_trait(Traits.Vector):
        ty = node.get_trait(Traits.ValueType)
        process(depth, ty)
        walk_type(ty, process, depth + 1)
    else:
        process(depth, node)


def walk_type_with_count(node: Type, process: Callable[[int, int, str, Type], None]) -> None:
    counts = {}

    def pre_process(depth, ty: Type):
        name = ty.get_trait(Traits.FieldName)
        if name:
            if name not in counts:
                counts[name] = 0
            counts[name] += 1
            process(depth, counts[name], name, ty)

    walk_type(node, pre_process)


def parse_type_definition(ty: str) -> Type:
    if ty.startswith('timestamp'):
        unit = ty.split('/')[1]
        ty = Types.I64.copy().replace_trait(Traits.TsUnit(unit))
        return ty

    if 'enum' in to_snake_case(ty):
        ty_name = ty.split('/')[0]
        ty = Types.enum(ty_name, [])
        ty.append_trait(Traits.TypeRef(ty_name))
        return ty
    else:
        ty_name = ty
        ty = Types.struct(ty_name, [])
        ty.append_trait(Traits.TypeRef(ty_name))
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

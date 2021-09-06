import random

from beartype import beartype
from pydantic import BaseModel, validator

from unidef.models.base_model import *
from unidef.utils.name_convert import *
from unidef.utils.typing_compat import *


class Trait(MyField):
    pass


class Traits:
    Kind = Trait(key="kind", default_present="", default_absent="")
    TypeName = Trait(key="name")
    FieldName = Trait(key="field_name")
    BitSize = Trait(key="bit_size")
    Signed = Trait(key="signed", default_present=True, default_absent=False)
    KeyType = Trait(key="key")
    ValueType = Trait(key="value", default_present=[], default_absent=[])
    Parent = Trait(key="parent")
    StructFields = Trait(key="field", default_present=[], default_absent=[])
    Struct = Trait(key="struct", default_present="", default_absent="")
    Enum = Trait(key="enum", default_present="", default_absent="")
    TypeRef = Trait(key="type_ref")
    Variant = Trait(key="variant")
    VariantName = Trait(key="variant_name")
    RawValue = Trait(key="raw_value", allow_none=True)
    # TODO: distinguish in line or before line comments
    BeforeLineComment = Trait(
        key="before_line_comment", default_present=[], default_absent=[]
    )
    InLineComment = Trait(key="in_line_comment", default_present=[], default_absent=[])
    BlockComment = Trait(key="block_comment")

    # Types
    Bool = Trait(key="bool", default_present=True, default_absent=False)
    Numeric = Trait(key="numeric", default_present=True, default_absent=False)
    Floating = Trait(key="floating", default_present=True, default_absent=False)
    Integer = Trait(key="integer", default_present=True, default_absent=False)
    String = Trait(key="string", default_present=True, default_absent=False)
    TupleField = Trait(key="tuple_field", default_present=[], default_absent=[])
    Tuple = Trait(key="tuple", default_present=True, default_absent=False)
    Vector = Trait(key="vector", default_present=True, default_absent=False)
    Map = Trait(key="map", default_present=True, default_absent=False)
    Unit = Trait(key="unit", default_present=True, default_absent=False)
    Null = Trait(key="null", default_present=True, default_absent=False)
    AllValue = Trait(key="all_value", default_present=True, default_absent=False)

    # Format
    SimpleEnum = Trait(key="simple_enum", default_present=True, default_absent=False)
    StringWrapped = Trait(
        key="string_wrapped", default_present=True, default_absent=False
    )
    TsUnit = Trait(key="ts_unit")

    # SQL related
    Primary = Trait(key="primary", default_present=True, default_absent=False)
    Nullable = Trait(key="nullable", default_present=True, default_absent=False)

    # Rust related
    Reference = Trait(key="reference", default_present=True, default_absent=False)
    Mutable = Trait(key="mutable", default_present=True, default_absent=False)
    Lifetime = Trait(key="lifetime")
    Derive = Trait(key="derive", default_present=[], default_absent=[])


class Type(MyBaseModel):
    """
    Type is the type model used in this program.
    It allows inheritance and multiple traits, similar to those in Rust and Java, as used in many other languages.
    """

    @classmethod
    @beartype
    def from_str(cls, name: str) -> __qualname__:
        return cls().append_trait(Traits.TypeName(name))

    @classmethod
    @beartype
    def from_trait(cls, name: str, trait: Trait) -> __qualname__:
        return (
            cls.from_str(name).append_trait(Traits.Kind(trait.key)).append_trait(trait)
        )

    def append_trait(self, trait: Trait) -> __qualname__:
        return self.append_field(trait)

    def get_trait(self, trait: Trait) -> Any:
        return self.get_field(trait)

    def get_traits(self, trait: Trait) -> List[Any]:
        return self.get_field(trait)

    def replace_trait(self, trait: Trait) -> __qualname__:
        return self.replace_field(trait)

    def extend_traits(self, field: Trait, values: Iterable[Any]) -> __qualname__:
        return self.extend_field(field, values)


def build_int(name: str) -> Type:
    ty = Type.from_trait(name, Traits.Integer)
    ty.append_trait(Traits.Numeric)
    ty.append_trait(Traits.BitSize(int(name[1:])))

    if name.startswith("i"):
        ty.append_trait(Traits.Signed)
    else:
        ty.append_trait(Traits.Signed(False))

    return ty


def build_float(name: str) -> Type:
    return (
        Type.from_trait(name, Traits.Floating)
        .append_trait(Traits.Numeric)
        .append_trait(Traits.BitSize(int(name[1:])))
        .append_trait(Traits.Signed)
    )


class Types:
    Bool = Type.from_trait("bool", Traits.Bool).freeze()

    I8 = build_int("i8").freeze()
    I16 = build_int("i16").freeze()
    I32 = build_int("i32").freeze()
    I64 = build_int("i64").freeze()
    I128 = build_int("i128").freeze()

    U8 = build_int("u8").freeze()
    U16 = build_int("u16").freeze()
    U32 = build_int("u32").freeze()
    U64 = build_int("u64").freeze()
    U128 = build_int("u128").freeze()

    String = Type.from_trait("string", Traits.String).freeze()
    Float = build_float("f32").freeze()

    Double = build_float("f64").freeze()

    Vector = Type.from_trait("vector", Traits.Vector).freeze()

    NoneType = (
        Type.from_trait("none", Traits.Null).append_trait(Traits.Nullable).freeze()
    )
    AllValue = Type.from_trait("all_value", Traits.AllValue).freeze()

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
        ty = Type.from_trait(name, Traits.Struct(name))
        ty.append_trait(Traits.StructFields(fields))
        return ty

    @staticmethod
    @beartype
    def enum(name: str, variants: List[Type]) -> Type:
        ty = Type.from_trait(Traits.Enum(name))
        ty.append_trait(Traits.Variant(variants))
        return ty


class TypeRegistry(BaseModel):
    types: Dict[str, Type] = {}
    traits = {}
    type_detector: list = []

    @beartype
    def insert_type(self, ty: Type):
        name = ty.get_trait(Traits.TypeName)
        assert ty.is_frozen(), f"type {name} should be frozen"
        if name not in self.types:
            self.types[name] = ty
        elif self.types[name] != ty:
            raise Exception(f"TypeAlreadyExistsAndConflict{name}")

    @beartype
    def insert_trait(self, trait: Trait):
        if trait.key not in self.traits:
            self.traits[trait.key] = trait
        elif self.traits[trait.key] != trait:
            raise Exception(f"TraitAlreadyExistsAndConflict{trait.name}")

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
        return self.traits.get(name)

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
    if s == "sec" or s == "s":
        return 1.0
    elif s == "ms":
        return 1e-3
    elif s == "us":
        return 1e-6
    elif s == "ns":
        return 1e-9
    raise Exception("Cannot convert {} to seconds".format(s))


def detect_timestamp_unit(inputtext: Union[str, int, float]) -> str:
    inputtext = float(inputtext)
    if abs(inputtext) < 1:
        return "ms"
    if 10e7 <= inputtext < 18e7:
        raise Exception("Expected a more recent date? You are missing 1 digit.")

    if inputtext >= 1e16 or inputtext <= -1e16:
        return "ns"
    elif inputtext >= 1e14 or inputtext <= -1e14:
        return "us"
    elif inputtext >= 1e11 or inputtext <= -3e10:
        return "ms"
    else:
        return "sec"


def string_wrapped(trait: Type) -> Type:
    return trait.copy().replace_trait(Traits.StringWrapped)


def prefix_join(prefix: str, name: str) -> str:
    if prefix:
        return prefix + "_" + name
    else:
        return name


class CouldNotParseDataExample(Exception):
    pass


@beartype
def parse_data_example(
    obj: Union[str, int, float, dict, list, None], prefix: str = ""
) -> Type:
    def inner(obj, prefix) -> Type:
        if obj is None:
            return Types.NoneType

        if isinstance(obj, str):
            if "." in obj:
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
            if "_ts" in prefix or "time" in prefix or "_at" in prefix:
                ty = (
                    ty.copy()
                    .append_trait(Traits.TsUnit(detect_timestamp_unit(obj)))
                    .replace_trait(Traits.TypeName("timestamp"))
                )

            return ty
        elif isinstance(obj, float):
            return Types.Double
        elif isinstance(obj, list):
            content = None
            if len(obj):
                content = parse_data_example(obj[0], prefix)
            return Types.Vector.copy().replace_trait(Traits.ValueType([content]))
        elif isinstance(obj, dict):
            fields = []
            for key, value in obj.items():
                value = parse_data_example(value, prefix_join(prefix, key))
                if value.get_trait(Traits.Struct):
                    value.replace_trait(Traits.TypeName(prefix_join(prefix, key)))

                for val in value.get_traits(Traits.ValueType):
                    if val.get_trait(Traits.Struct):
                        new_name = prefix_join(prefix, key)
                        if new_name.endswith("s"):
                            new_name = new_name[:-1]
                        val.replace_trait(Traits.TypeName(new_name))

                fields.append(Types.field(key, value))
            return Types.struct("struct_" + str(random.randint(0, 1000)), fields)
        raise CouldNotParseDataExample(str(obj))

    return inner(obj, prefix).copy().append_trait(Traits.RawValue(obj))


def walk_type(node: Type, process: Callable[[int, Type], None], depth=0) -> None:
    if node.get_trait(Traits.Struct):
        for field in node.get_traits(Traits.StructFields):
            process(depth, field)
            walk_type(field, process, depth + 1)
    if node.get_trait(Traits.Vector):
        for ty in node.get_trait(Traits.ValueType):
            process(depth, ty)
            walk_type(ty, process, depth + 1)
    else:
        process(depth, node)


def walk_type_with_count(
    node: Type, process: Callable[[int, int, str, Type], None]
) -> None:
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
    if ty.startswith("timestamp"):
        unit = ty.split("/")[1]
        ty = Types.I64.copy().replace_trait(Traits.TsUnit(unit))
        return ty

    if "enum" in to_snake_case(ty):
        ty_name = ty.split("/")[0]
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

if __name__ == "__main__":
    GLOBAL_TYPE_REGISTRY.list_types()

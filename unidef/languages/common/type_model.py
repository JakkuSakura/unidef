import random

from unidef.models.base_model import *
from unidef.utils.name_convert import *
from unidef.utils.typing import *


class Trait(TypedField):
    pass


class Traits:
    Kind = Trait(key="kind", ty=str)
    TypeName = Trait(key="name", ty=str)
    FieldName = Trait(key="field_name", ty=str)
    BitSize = Trait(key="bit_size", ty=int)
    Signed = Trait(key="signed", ty=bool)
    KeyType = Trait(key="key", ty=str)
    # deprecated, the same as generics
    ValueTypes = Trait(key="value", ty=list, default=[])
    Parent = Trait(key="parent", ty=Any)
    StructFields = Trait(key="fields", ty=list)
    Struct = Trait(key="struct", ty=bool, default=False)
    Enum = Trait(key="enum", ty=bool)
    TypeRef = Trait(key="type_ref", ty=str)
    Variants = Trait(key="variants", ty=Any)
    VariantNames = Trait(key="variant_name", ty=List[str])
    RawValue = Trait(key="raw_value", ty=Any)
    Generics = Trait(key="generics", ty=List["DyType"])

    # TODO: distinguish in line or before line comments
    BeforeLineComment = Trait(key="before_line_comment", ty=List[str], default=[])
    InLineComment = Trait(key="in_line_comment", ty=str)
    BlockComment = Trait(key="block_comment", ty=str)

    # Types
    Bool = Trait(key="bool", ty=bool)
    Numeric = Trait(key="numeric", ty=bool)
    Floating = Trait(key="floating", ty=bool)
    Integer = Trait(key="integer", ty=bool)
    String = Trait(key="string", ty=bool)
    Tuple = Trait(key="tuple", ty=bool)
    Vector = Trait(key="vector", ty=bool)
    Map = Trait(key="map", ty=bool)
    Unit = Trait(key="unit", ty=bool)
    Null = Trait(key="null", ty=bool)
    Object = Trait(key="object", ty=bool)
    AllValue = Trait(key="all_value", ty=bool)

    NotInferredType = Trait(
        key="not_inferred_type", ty=bool
    )

    Default = Trait(key="default", ty=Any)

    FromJson = Trait(key="from_json", ty=bool, default=False)
    # Format
    SimpleEnum = Trait(key="simple_enum", ty=bool)
    StringWrapped = Trait(
        key="string_wrapped", ty=bool
    )
    TsUnit = Trait(key="ts_unit", ty=str)

    # SQL related
    Primary = Trait(key="primary", ty=bool)
    Nullable = Trait(key="nullable", ty=bool)

    # Rust related
    Reference = Trait(key="reference", ty=bool)
    Mutable = Trait(key="mutable", ty=bool)
    Lifetime = Trait(key="lifetime", ty=str)
    Derive = Trait(key="derive", ty=List[str], default=[])

    TypeVariable = Trait(key="type_variable", ty=Any)


class DyType(MixedModel):
    """
    Type is the type model used in this program.
    It allows inheritance and multiple traits, similar to those in Rust and Java, as used in many other languages.
    """
    name: str
    kind: str = ''

    @classmethod
    @beartype
    def from_str(cls, name: str) -> __qualname__:
        return cls(name=name)

    @classmethod
    @beartype
    def from_trait(cls, name: str, trait: FieldValue) -> __qualname__:
        this = cls(name=name, kind=trait.key).append_field(trait)
        return this


class GenericType(DyType):
    kind: str = "generic"
    generics: List[DyType]


class TupleType(GenericType):
    kind: str = 'tuple'
    tuple: bool = True

    def __init__(self, *values: DyType, **kwargs):
        super().__init__(generics=values, **kwargs)


class VectorType(GenericType):
    kind: str = "vector"
    vector: bool = True

    def __init__(self, value: DyType, **kwargs):
        super().__init__(generics=[value], **kwargs)


class IntegerType(DyType):
    kind: str = 'integer'
    integer: bool = True
    numeric: bool = True
    bit_size: int
    signed: bool


def build_int(name: str) -> DyType:
    bit_size = int(name[1:])
    signed = name.startswith("i")
    return IntegerType(name=name, bit_size=bit_size, signed=signed)


class FloatingType(DyType):
    kind: str = 'floating'
    floating: bool = True
    integer: bool = False
    numeric: bool = True
    bit_size: int
    signed: bool = True


def build_float(name: str) -> DyType:
    return FloatingType(name=name, bit_size=int(name[1:]))


class FieldType(MixedModel):
    field_name: str
    field_type: DyType


class StructType(DyType):
    kind: str = 'struct'
    struct: bool = True
    name: str
    fields: List[FieldType]
    data_type: bool = True


class VariantType(DyType):
    variant_names: List[str]


class EnumType(DyType):
    kind: str = 'enum'
    enum: bool = True
    name: str
    variants: List[VariantType]


class Types:
    Bool = DyType.from_trait("bool", Traits.Bool(True)).freeze()

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

    String = DyType.from_trait("string", Traits.String(True)).freeze()

    Float = build_float("f32").freeze()
    Double = build_float("f64").freeze()

    NoneType = (
        DyType.from_trait("none", Traits.Null(True)).append_field(Traits.Nullable(True)).freeze()
    )

    Unit = DyType.from_trait("unit", Traits.Unit(True)).append_field(Traits.Null(True)).freeze()

    AllValue = DyType.from_trait("all_value", Traits.AllValue(True)).freeze()
    Object = (
        DyType.from_trait("object", Traits.Object(True))
            .append_field(Traits.Map(True))
            .append_field(Traits.ValueTypes([String, AllValue]))
            .freeze()
    )



class TypeRegistry(BaseModel):
    types: Dict[str, DyType] = {}
    traits = {}
    type_detector: list = []

    @beartype
    def insert_type(self, ty: DyType):
        name = ty.get_field(Traits.TypeName)
        assert ty.is_frozen(), f"type {name} should be frozen"
        if name not in self.types:
            self.types[name] = ty
        elif self.types[name] != ty:
            raise Exception(f"TypeAlreadyExistsAndConflict {name}")

    @beartype
    def insert_trait(self, trait: Trait):
        if trait.key not in self.traits:
            self.traits[trait.key] = trait
        elif self.traits[trait.key] != trait:
            raise Exception(f"Trait {trait.key} already exists")

    @beartype
    def get_type(self, name: str) -> Optional[DyType]:
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
    def is_subclass(self, child: DyType, parent: DyType) -> bool:
        if Traits.Parent(parent) in child.keys():
            return True
        p = child.get_field(Traits.Parent).value
        return self.is_subclass(p, parent)

    @beartype
    def list_types(self) -> List[DyType]:
        return list(self.types.values())

    @beartype
    def list_traits(self) -> List[Trait]:
        return list(self.traits.values())


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


def string_wrapped(trait: DyType) -> DyType:
    return trait.copy().replace_field(Traits.StringWrapped)


def prefix_join(prefix: str, name: str) -> str:
    if prefix:
        return prefix + "_" + name
    else:
        return name


@beartype
def infer_type_from_example(
        obj0: Union[str, int, float, dict, list, None], prefix0: str = ""
) -> DyType:
    def inner(obj, prefix) -> DyType:
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
                        .append_field(Traits.TsUnit(detect_timestamp_unit(obj)))
                        .replace_field(Traits.TypeName("timestamp"))
                )

            return ty
        elif isinstance(obj, float):
            return Types.Double
        elif isinstance(obj, list):
            content = Types.AllValue
            if len(obj):
                content = infer_type_from_example(obj[0], prefix)
            return VectorType(content)
        elif isinstance(obj, dict):
            fields = []
            for key, value in obj.items():
                value = infer_type_from_example(value, prefix_join(prefix, key))
                if value.get_field(Traits.Struct):
                    value.replace_field(Traits.TypeName(prefix_join(prefix, key)))

                for val in value.get_field(Traits.ValueTypes):
                    if val.get_field(Traits.Struct):
                        new_name = prefix_join(prefix, key)
                        if new_name.endswith("s"):
                            new_name = new_name[:-1]
                        val.replace_field(Traits.TypeName(new_name))

                fields.append(FieldType(field_name=key, field_type=value))
            return StructType(name="struct_" + str(random.randint(0, 1000)), fields=fields, is_data_type=True)
        raise Exception(f"Could not infer type from {obj}")

    return inner(obj0, prefix0).copy().append_field(Traits.FromJson(True)).append_field(Traits.RawValue(obj0))


def walk_type(node: DyType, process: Callable[[int, DyType], None], depth=0) -> None:
    if node.get_field(Traits.Struct):
        for field in node.get_field(Traits.StructFields):
            walk_type(field, process, depth + 1)
    elif node.get_field(Traits.Vector):
        for ty in node.get_field(Traits.Generics):
            walk_type(ty, process, depth + 1)
    else:
        process(depth, node)


def walk_type_with_count(
        node: DyType, process: Callable[[int, int, str, DyType], None]
) -> None:
    counts = {}

    def pre_process(depth, ty: DyType):
        name = ty.get_field(Traits.FieldName)
        if name:
            if name not in counts:
                counts[name] = 0
            counts[name] += 1
            process(depth, counts[name], name, ty)

    walk_type(node, pre_process)


def parse_type_definition(ty: str) -> DyType:
    if ty.startswith("(") and ty.endswith(")"):
        types = [GLOBAL_TYPE_REGISTRY.get_type(x.strip()) for x in ty[1:-1].split(",")]
        return TupleType(name=ty, tuple_fields=types)
    if ty in ["String", "string", "str", "&str"]:
        return Types.String
    if ty.startswith("timestamp"):
        unit = ty.split("/")[1]
        ty = Types.I64.copy().replace_field(Traits.TsUnit(unit))
        return ty

    if "enum" in to_snake_case(ty):
        ty_name = ty.split("/")[0]
        ty = EnumType(name=ty_name, variants=[])
        ty.append_field(Traits.TypeRef(ty_name))
        return ty
    else:
        ty_name = ty
        ty = StructType(name=ty_name, fields=[], is_data_type=True)
        ty.append_field(Traits.TypeRef(ty_name))
        return ty


GLOBAL_TYPE_REGISTRY = TypeRegistry()

for t in Traits.__dict__.values():
    if isinstance(t, Trait):
        GLOBAL_TYPE_REGISTRY.insert_trait(t)

for t in Types.__dict__.values():
    if isinstance(t, DyType):
        GLOBAL_TYPE_REGISTRY.insert_type(t)

GLOBAL_TYPE_REGISTRY.type_detector.append(parse_type_definition)

if __name__ == "__main__":
    GLOBAL_TYPE_REGISTRY.list_types()

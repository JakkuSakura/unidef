from unidef.languages.common.type_model import *
from unidef.utils.formatter import *
from unidef.utils.name_convert import *
from unidef.utils.transformer import *
from unidef.utils.typing_ext import *
from unidef.utils.vtable import VTable

RUST_KEYWORDS = {
    "as": "r#as",
    "break": "r#break",
    "const": "r#const",
    "continue": "r#continue",
    "crate": "r#crate",
    "else": "r#else",
    "enum": "r#enum",
    "extern": "r#extern",
    "false": "r#false",
    "fn": "r#fn",
    "for": "r#for",
    "if": "r#if",
    "impl": "r#impl",
    "in": "r#in",
    "let": "r#let",
    "loop": "r#loop",
    "match": "r#match",
    "mod": "r#mod",
    "move": "r#move",
    "mut": "r#mut",
    "pub": "r#pub",
    "ref": "r#ref",
    "return": "r#return",
    "self": "r#self",
    "Self": "r#Self",
    "static": "r#static",
    "struct": "r#struct",
    "super": "r#super",
    "trait": "r#trait",
    "true": "r#true",
    "type": "ty",
    "unsafe": "r#unsafe",
    "use": "r#use",
    "where": "r#where",
    "while": "r#while",
    "async": "r#async",
    "await": "r#await",
    "dyn": "r#dyn",
}


@abstract
class RustAstNode(NodeTransformable):
    pass


class ProcMacro(RustAstNode):
    pass


class SerdeAs(ProcMacro):
    serde_as: str = ""


SERDE_AS = SerdeAs()
SERDE_AS_DISPLAY_FROM_STR = SerdeAs(serde_as="DisplayFromStr")


class Derive(ProcMacro):
    enabled: List[str]

    def __init__(self, enabled: List[str]):
        super().__init__(enabled=enabled)

    def append(self, s: str):
        self.enabled.append(s)


DEFAULT_DERIVE = Derive(
    ["Clone", "Debug", "PartialEq", "serde::Serialize", "serde::Deserialize"]
)
ENUM_DEFAULT_DERIVE = Derive(
    [
        "Copy",
        "Clone",
        "Debug",
        "PartialEq",
        "Eq",
        "serde::Serialize",
        "serde::Deserialize",
        "strum::EnumString",
        "strum::Display",
    ]
)


class Serde(ProcMacro):
    tag: str = ""
    rename: List[str] = ""
    rename_all: str = ""


class Strum(ProcMacro):
    serialize: List[str]


class AccessModifier(Enum):
    PRIVATE = ""
    PUBLIC = "pub "


def map_field_name(name: str) -> str:
    if name[0].isnumeric() and name[0] != "_":
        return "_" + name

    return RUST_KEYWORDS.get(name) or to_snake_case(name)


def map_func_name(name: str) -> str:
    if name[0].isnumeric() and name[0] != "_":
        return "_" + name

    return RUST_KEYWORDS.get(name) or to_snake_case(name)


class RustFieldNode(RustAstNode):
    name: str
    original_name: str = None
    access: AccessModifier = AccessModifier.PUBLIC
    value: DyType
    val_in_str: bool = False

    @staticmethod
    def from_name(name: str, ty: str = ""):
        if not name.startswith("&"):
            name = map_field_name(name)
        ty_or_name = ty or name
        return RustFieldNode(
            name=name,
            value=DyType.from_str(ty_or_name).append_field(Traits.TypeRef(ty_or_name)),
        )

    def __init__(self, ty: FieldType = None, **kwargs):
        if ty:
            value = ty

            assert value is not None, "Is not an valid field " + repr(ty)
            kwargs.update(
                {
                    "name": map_field_name(ty.field_name),
                    "original_name": ty.field_name,
                    "value": value.field_type,
                    "val_in_str": value.field_type.get_field(Traits.StringWrapped)
                                  or False,
                }
            )

        super().__init__(**kwargs)


class RustCommentNode(RustAstNode):
    content: List[str]
    cargo_doc: bool = False

    def __init__(self, content: List[str], cargo_doc: bool = False):
        super().__init__(content=content, cargo_doc=cargo_doc)


class RustStructNode(RustAstNode):
    annotations: List[ProcMacro] = []
    access: AccessModifier = AccessModifier.PUBLIC
    name: str
    fields: List[RustFieldNode]
    raw: DyType = None
    derive: Optional[Derive] = None

    @staticmethod
    @beartype
    def parse_name(name: str):
        return to_pascal_case(name)

    def __init__(self, raw: DyType = None, **kwargs):
        if raw:
            is_data_type = kwargs.get("is_data_type")
            if is_data_type:
                derive = DEFAULT_DERIVE.copy()
                annotations = [derive]
            else:
                derive = None
                annotations = []

            kwargs.update(
                {
                    "raw": raw,
                    "name": RustStructNode.parse_name(raw.get_field(Traits.TypeName)),
                    "fields": [
                        RustFieldNode(f) for f in raw.get_field(Traits.StructFields)
                    ],
                    "annotations": annotations,
                    "derive": derive,
                }
            )

        super().__init__(**kwargs)
        for field in self.fields:
            if field.val_in_str:
                self.annotations.insert(0, SERDE_AS)
                break
        if self.raw:
            for derive in self.raw.get_field(Traits.Derive):
                self.derive.append(derive)


class RustEnumNode(RustAstNode):
    annotations: List[ProcMacro] = []
    access: AccessModifier = AccessModifier.PUBLIC
    name: str
    variants: List[VariantType]
    raw: DyType = None

    @staticmethod
    def parse_variant_name(name: str):
        if name.isupper():
            return name
        else:
            return to_pascal_case(name)

    def __init__(self, raw: EnumType = None, **kwargs):
        if raw:
            annotations = [ENUM_DEFAULT_DERIVE]

            kwargs.update(
                {
                    "raw": raw,
                    "name": RustStructNode.parse_name(raw.get_field(Traits.TypeName)),
                    "variants": list(raw.get_field(Traits.Variants)),
                    "annotations": annotations,
                }
            )

        super().__init__(**kwargs)


class StructRegistry:
    def __init__(self):
        self.structs: List[DyType] = []

    def add_struct(self, struct: DyType):
        if struct not in self.structs:
            self.structs.append(struct)


class RustArgumentPairNode(RustAstNode):
    mutable: Optional[bool] = None
    name: str
    type: str


class RustFuncDeclNode(RustAstNode):
    name: str
    access: AccessModifier = AccessModifier.PUBLIC
    is_async: bool = False
    args: List[RustArgumentPairNode]
    ret: Optional[Union[RustAstNode, DyType]]
    content: Union[str, List[RustAstNode]]


class RustImplNode(RustAstNode):
    name: str
    trait: str = ""
    functions: List[RustAstNode]


class RustStatementNode(RustAstNode):
    nodes: List[RustAstNode] = []
    raw: str = ""

    def __init__(self, **kwargs):
        assert (
                int(bool(kwargs.get("nodes") is not None))
                ^ int(bool(kwargs.get("raw") is not None))
                == 1
        ), "only nodes xor raw can be set"
        super().__init__(**kwargs)


class RustRawNode(RustAstNode):
    raw: str
    new_line: bool = False

    def __init__(self, raw, new_line=False, *args, **kwargs):
        super().__init__(raw=raw, new_line=new_line, *args, **kwargs)


class RustBulkNode(RustAstNode):
    nodes: List[RustAstNode]

    def __init__(self, nodes, **kwargs):
        super().__init__(nodes=nodes)


class RustBlockNode(RustAstNode):
    nodes: List[RustAstNode]
    new_line: bool = True


class RustLineNode(RustAstNode):
    node: RustAstNode

    def __init__(self, node, **kwargs):
        super().__init__(node=node, **kwargs)


class RustIndentedNode(RustAstNode):
    nodes: List[RustAstNode]

    def __init__(self, nodes, **kwargs):
        super().__init__(nodes=nodes, **kwargs)


class RustReturnNode(RustAstNode):
    returnee: Optional[RustAstNode]


class RustFuncCallNode(RustAstNode):
    callee: RustAstNode
    arguments: List[RustAstNode]


class RustUseNode(RustAstNode):
    path: str
    rename: str = ""


class RustVariableDeclaration(RustAstNode):
    mutability: Optional[bool] = None
    name: str
    ty: Optional[DyType] = None
    init: Optional[RustAstNode]


def map_type_to_rust(ty: DyType) -> str:
    # if ty.get_field(Traits.ValueType):
    #     return map_type_to_str(ty.get_field(Traits.ValueType))
    if ty.get_field(Traits.Nullable):
        ty = ty.copy()
        ty.remove_field(Traits.Nullable)
        return "Option<{}>".format(map_type_to_rust(ty))
    if ty.get_field(Traits.Null) and ty.get_field(Traits.FromJson):
        return "String"
    elif ty.get_field(Traits.TsUnit):
        return "TimeStamp" + to_pascal_case(ty.get_field(Traits.TsUnit))
    elif ty.get_field(Traits.Struct):
        if ty.get_field(Traits.TypeRef):
            return ty.get_field(Traits.TypeRef)
        else:
            return RustStructNode.parse_name(ty.get_field(Traits.TypeName))
    elif ty.get_field(Traits.Enum):
        return RustEnumNode.parse_variant_name(ty.get_field(Traits.TypeRef))
    elif ty.get_field(Traits.Tuple):
        return "({})".format(
            ", ".join([map_type_to_rust(t) for t in ty.get_field(Traits.Generics)])
        )
    elif ty.get_field(Traits.Vector):
        return "Vec<{}>".format(map_type_to_rust(ty.get_field(Traits.Generics)[0]))
    elif ty.get_field(Traits.Bool):
        return "bool"
    elif ty.get_field(Traits.AllValue):
        return "serde_json::Value"
    elif ty.get_field(Traits.Integer):
        bits = ty.get_field(Traits.BitSize)
        if ty.get_field(Traits.Signed):
            return "i" + str(bits)
        else:
            return "u" + str(bits)

    elif ty.get_field(Traits.Floating):
        bits = ty.get_field(Traits.BitSize)
        return "f" + str(bits)
    elif ty.get_field(Traits.Map):
        key, value = tuple(ty.get_field(Traits.ValueTypes))
        return "HashMap<{}, {}>".format(map_type_to_rust(key), map_type_to_rust(value))
    elif ty.get_field(Traits.String):
        if ty.get_field(Traits.Reference):
            lifetime = ty.get_field(Traits.Lifetime)
            return "&{}str".format(lifetime and "'" + lifetime + " " or "")
        else:
            return "String"
    elif ty.get_field(Traits.Unit):
        return "()"
    elif ty.get_field(Traits.TypeRef):
        tr: str = ty.get_field(Traits.TypeRef)
        generics: List[DyType] = ty.get_field(Traits.Generics) or []
        for i, repl in enumerate(generics):
            repl_str = map_type_to_rust(repl)
            # print('before replacement', tr, repl_str)
            tr = tr.replace(f"${i + 1}", repl_str)
            # print('after replacement', tr)
        return tr
    # elif ty.get_field(Traits.Raw):
    #     return ty.get_field(Traits.Raw)

    raise Exception("Cannot map type {} to str".format(ty.get_field(Traits.TypeName)))


class RustFormatter(VTable):
    def transform(self, node) -> Code:
        return self(node)


    def transform_rust_return_node(self, node: RustReturnNode) -> Code:
        if node.returnee:
            return Code("return {{ returnee }};", returnee=self.transform(node.returnee))

        else:
            return Code("return;")


    def transform_rust_line_node(self, node: RustLineNode) -> Code:
        return Code("{{ val }}\n", val=self.transform(node.node))


    def transform_rust_argument_pair_node(
            self, node: RustArgumentPairNode
    ) -> Code:
        if node.mutable:
            mut = "mut "
        else:
            mut = ""
        sources = []
        if not node.name.startswith("&") and node.type:
            sources.append(": ")
            if isinstance(node.type, str):
                sources.append(node.type)
            elif isinstance(node.type, RustAstNode):
                sources.append(self.transform(node.type))
            elif isinstance(node.type, DyType):
                sources.append(map_type_to_rust(node.type))
            else:
                raise Exception(
                    "Could not process type for " + type(node.type).__name__
                )
        return Code("""\
{{ mut }}{{ name }}{{ others }}
""", mut=mut, name=node.name, others="".join(map(str, sources)))


    def transform_rust_statement_node(self, node: RustStatementNode) -> Code:
        if node.nodes:
            sources = []
            for n in node.nodes:
                sources.append(self.transform(n))
            sources.append("; ")

            return Code("{{ sources }}", sources=''.join(map(str, sources)))
        if node.raw:
            return Code("{{ raw }}", raw=node.raw)

        raise Exception("You must set either nodes or raw")


    def transform_rust_block_node(self, node: RustBlockNode) -> Code:
        lines = [self.transform(n) for n in node.nodes]
        return Code(r"""{{ lines }}{{ new_line }}""", lines='\n'.join(map(str, lines)), new_line='\n' if node.new_line else '')


    def transform_rust_bulk_node(self, node: RustBulkNode) -> Code:
        lines = [self.transform(n) for n in node.nodes]
        return Code(r"""{{ lines }}""", lines=''.join(map(str, lines)))


    def transform_rust_raw_node(self, node: RustRawNode) -> Code:
        return Code(r"""{{ text }}{{ new_line }}""", text=node.raw, new_line='\n' if node.new_line else '')


    def transform_serde(self, node: Serde) -> Code:
        pairs = []
        if node.tag:
            pairs.append(f'tag = "{node.tag}"')
        for rename in node.rename:
            pairs.append(f'rename = "{rename}"')
        if node.rename_all:
            pairs.append(f'rename_all = "{node.rename_all}"')

        return Code("""#[serde({{ pairs }})]\n""", pairs=", ".join(pairs))


    def transform_derive(self, node: Derive) -> Code:
        return Code("""#[derive({{ enabled }})]\n""", enabled=", ".join(node.enabled))


    def transform_serde_as(self, node: SerdeAs) -> Code:
        if not node.serde_as:
            content = "#[serde_with::serde_as]"
        else:
            content = f'#[serde_as(as = "{node.serde_as}")]'

        return Code(content)


    def transform_rust_field_node(self, node: RustFieldNode) -> Code:
        sources = []
        if node.val_in_str:
            sources.append(self.transform(SERDE_AS_DISPLAY_FROM_STR))
        if node.original_name and node.original_name != node.name:
            sources.append(self.transform(Serde(rename=[node.original_name])))
        for comment in node.value.get_field(Traits.BeforeLineComment):
            sources.append(
                self.transform(RustCommentNode(comment.splitlines(), cargo_doc=True))
            )

        sources.append(
            f"{node.access.value}{node.name}: {map_type_to_rust(node.value)}"
        )
        return Code("{{ sources }}", sources=", ".join(sources))


    def transform_rust_comment_node(self, node: RustCommentNode) -> Code:
        sources = []
        if node.cargo_doc:
            for line in node.content:
                sources.append("/// " + line)
                sources.append("///")
        else:
            for line in node.content:
                sources.append("// " + line)
        sources.append('')
        code = Code("{{ sources }}", sources='\n'.join(sources))
        return code


    def transform_rust_struct_node(self, node: RustStructNode) -> Code:
        sources = []
        for anno in node.annotations:
            sources.append(self.transform(anno))

        sources.append(f"{node.access.value}struct {node.name} ")
        in_braces = []
        if node.fields:
            line = []
            for i, field in enumerate(node.fields):
                if i > 0:
                    line.append(", ")
                    in_braces.append(Code("{{ line }}\n", line=''.join(map(str, line))))
                    line = []

                line.append(self.transform(field))
            in_braces.append(Code("{{ line }}\n", line=''.join(map(str, line))))
        sources.append(Code("""\
{
    {{ in_braces }}
}


""", in_braces='\n'.join(map(str, in_braces))))
        return Code("""{{ sources }}""", sources='\n'.join(map(str, sources)))


    def transform_rust_enum_node(self, node: RustEnumNode) -> Code:
        sources = []
        for anno in node.annotations:
            sources.append(self.transform(anno))

        sources.append(f"{node.access.value}enum {node.name} ")
        in_braces = []
        for field in node.variants:
            name = list(field.variant_names)
            mapped = map_field_name(name[0])
            if len(name) > 1 or mapped != name[0]:
                reversed_names = name[:]
                reversed_names.reverse()
                in_braces.append(self.transform(Strum(serialize=reversed_names)))

            in_braces.append(mapped + ", ")
        return Code("""\
{{ sources }}{
    {{ in_braces }}
}
""", sources="".join(map(str, sources)), in_braces=''.join(map(str, in_braces)))


    def transform_strum(self, node: Strum) -> Code:
        return Code(
            """#[strum({{ val }})]""",
            val=", ".join(['serialize = "{}"'.format(x) for x in node.serialize])

        )


    def transform_rust_impl_node(self, node: RustImplNode) -> Code:

        if node.trait:
            head = f"impl {node.trait} for {node.name}"
        else:
            head = f"impl {node.name}"
        in_braces = []
        for func in node.functions:
            if node.trait and isinstance(func, RustFuncDeclNode):
                func.access = AccessModifier.PRIVATE
            in_braces.append(self.transform(func))
        return Code("""\
{{ head }} {
    {{ value }}
}


""", head=head, value='\n'.join(map(str, in_braces)))


    def transform_rust_func_decl_node(self, node: RustFuncDeclNode) -> Code:
        if node.access == AccessModifier.PUBLIC:
            access_value = "pub "
        else:
            access_value = ""
        if node.is_async:
            async_value = "async "
        else:
            async_value = ""

        in_braces = []
        for i, arg in enumerate(node.args):
            if i > 0:
                in_braces.append(", ")
            in_braces.append(self.transform(arg))

        if isinstance(node.ret, DyType):
            if not node.ret.get_field(Traits.Unit):
                ret_type = " -> " + map_type_to_rust(node.ret)
            else:
                ret_type = ''
        elif isinstance(node.ret, RustAstNode):
            ret_type = ''.join([" -> ", self.transform(node.ret)])
        else:
            ret_type = ''
        if isinstance(node.content, str):
            content = [node.content]
        elif isinstance(node.content, list):
            content = []
            for c in node.content:
                content.append(self.transform(c))
        else:
            raise NotImplementedError(str(type(node.content)))
        return Code("""\
{{ access }}{{ async_value }}fn {{name}}({{ args }}){{ ret_type }} {
    {{ content }}
}

""",
                    access=access_value,
                    async_value=async_value,
                    name=node.name,
                    args=''.join(map(str, in_braces)),
                    ret_type=ret_type,
                    content=''.join(map(str, content)))


    def transform_rust_func_call_node(self, node: RustFuncCallNode) -> Code:
        args = []
        for i, a in enumerate(node.arguments):
            if i > 0:
                args.append(", ")

            args.append(self.transform(a))

        return Code("""\
{{ callee }}({{ args }})
""", callee=self.transform(node.callee), args=''.join(map(str, args)))


    def transform_rust_use_node(self, node: RustUseNode) -> Code:
        if node.rename and node.rename != node.path.split("::")[-1]:
            return Code(f"use {node.path} as {node.rename}")
        else:
            return Code(f"use {node.path};")


    def transform_rust_variable_declaration(
            self, node: RustVariableDeclaration
    ) -> Code:

        if node.mutability:
            mutability = " mut"
        else:
            mutability = ""
        sources = [f"let{mutability} {node.name}"]
        if node.ty:
            sources.extend(
                [": ", map_type_to_rust(node.ty)]
            )
        if node.init:
            sources.extend([" = ", self.transform(node.init), ";"])

        return Code("{{ val }}", val=''.join(list(map(str, sources))))


def try_rustfmt(s: str) -> str:
    import subprocess

    try:
        rustfmt = subprocess.Popen(
            ["rustfmt"],
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
        )
        rustfmt.stdin.write(s.encode())
        rustfmt.stdin.close()
        parsed = rustfmt.stdout.read().decode()
        error = rustfmt.stderr.read().decode()
        if error:
            logging.error("Error when formatting with rustfmt: %s", error)
            return s
        else:
            return parsed
    except Exception as e:
        logging.error("Error while trying to use rustfmt, defaulting to raw %s", e)
        return s

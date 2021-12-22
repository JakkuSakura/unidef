import logging
from unidef.utils.typing import *
from unidef.languages.common.type_model import *
from unidef.utils.transformer import *
from unidef.utils.formatter import *
from unidef.utils.name_convert import *

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

    def __init__(self, ty: DyType = None, **kwargs):
        if ty:
            value = ty

            assert value is not None, "Is not an valid field " + repr(ty)
            kwargs.update(
                {
                    "name": map_field_name(ty.get_field(Traits.FieldName)),
                    "original_name": ty.get_field(Traits.FieldName),
                    "value": value,
                    "val_in_str": value.get_field(Traits.StringWrapped) or False,
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
            is_data_type = kwargs.get('is_data_type')
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
                    "derive": derive
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
    variants: List[DyType]
    raw: DyType = None

    @staticmethod
    def parse_variant_name(name: str):
        if name.isupper():
            return name
        else:
            return to_pascal_case(name)

    def __init__(self, raw: DyType = None, **kwargs):
        if raw:
            annotations = [ENUM_DEFAULT_DERIVE]

            kwargs.update(
                {
                    "raw": raw,
                    "name": RustStructNode.parse_name(raw.get_field(Traits.TypeName)),
                    "variants": list(raw.get_field(Traits.Variant)),
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


class RustBulkNode(RustAstNode):
    nodes: List[RustAstNode]


class RustBlockNode(RustAstNode):
    nodes: List[RustAstNode]
    new_line: bool = True


class RustIndentedNode(RustAstNode):
    nodes: List[RustAstNode]


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
            ", ".join([map_type_to_rust(t) for t in ty.get_field(Traits.TupleFields)])
        )
    elif ty.get_field(Traits.Vector):
        return "Vec<{}>".format(map_type_to_rust(ty.get_field(Traits.ValueTypes)[0]))
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
        return "HashMap<{}, {}>".format(
            map_type_to_rust(key),
            map_type_to_rust(value)
        )
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


class RustFormatter(NodeTransformer[RustAstNode, SourceNode], VisitorPattern):
    functions: Optional[List[NodeTransformer]] = None

    @beartype
    def transform(self, node: RustAstNode) -> SourceNode:
        if self.functions is None:
            self.functions = self.get_functions("transform_")
        for func in self.functions:
            if func.accept(node):
                return func.transform(node)
        else:
            raise Exception("Could not format " + type(node).__name__)

    @beartype
    def transform_rust_return_node(self, node: RustReturnNode) -> SourceNode:
        if node.returnee:
            return self.transform_rust_statement_node(
                RustStatementNode(nodes=[RustRawNode(raw="return "), node.returnee])
            )
        else:
            return self.transform_rust_statement_node(
                RustStatementNode(nodes=[RustRawNode(raw="return")])
            )

    @beartype
    def transform_rust_argument_pair_node(
            self, node: RustArgumentPairNode
    ) -> SourceNode:
        sources = []
        if node.mutable:
            sources.append(TextNode(text="mut "))

        sources.append(TextNode(text=node.name))
        if not node.name.startswith("&") and node.type:
            sources.append(TextNode(text=": "))
            if isinstance(node.type, str):
                sources.append(TextNode(text=node.type))
            elif isinstance(node.type, RustAstNode):
                sources.append(node.type)
            elif isinstance(node.type, DyType):
                sources.append(map_type_to_rust(node.type))
            else:
                raise Exception(
                    "Could not process type for " + type(node.type).__name__
                )
        return BulkNode(sources=sources)

    @beartype
    def transform_rust_statement_node(self, node: RustStatementNode) -> SourceNode:
        if node.nodes:
            sources = []
            for n in node.nodes:
                sources.append(self.transform(n))
            sources.append(TextNode(text="; "))
            return LineNode(content=BulkNode(sources=sources))
        if node.raw:
            return LineNode(content=TextNode(text=node.raw))

        raise Exception("You must set either nodes or raw")

    @beartype
    def transform_rust_block_node(self, node: RustBlockNode) -> SourceNode:
        lines = [self.transform(n) for n in node.nodes]
        return BracesNode(value=BulkNode(sources=lines), post_new_line=node.new_line)

    @beartype
    def transform_rust_bulk_node(self, node: RustBulkNode) -> SourceNode:
        return BulkNode(sources=[self.transform(n) for n in node.nodes])

    @beartype
    def transform_rust_raw_node(self, node: RustRawNode) -> SourceNode:
        text = TextNode(text=node.raw)
        if node.new_line:
            text = LineNode(content=text)
        return text

    @beartype
    def transform_serde(self, node: Serde) -> SourceNode:
        pairs = []
        if node.tag:
            pairs.append(f'tag = "{node.tag}"')
        for rename in node.rename:
            pairs.append(f'rename = "{rename}"')
        if node.rename_all:
            pairs.append(f'rename_all = "{node.rename_all}"')

        line = "#[serde({})]".format(",".join(pairs))
        return LineNode(content=TextNode(text=line))

    @beartype
    @beartype
    def transform_derive(self, node: Derive) -> SourceNode:
        return LineNode(
            content=TextNode(text="#[derive({})]".format(", ".join(node.enabled)))
        )

    @beartype
    def transform_serde_as(self, node: SerdeAs) -> SourceNode:
        if not node.serde_as:
            content = "#[serde_with::serde_as]"
        else:
            content = f'#[serde_as(as = "{node.serde_as}")]'

        return LineNode(content=TextNode(text=content))

    @beartype
    def transform_rust_field_node(self, node: RustFieldNode) -> SourceNode:
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
            TextNode(
                text=f"{node.access.value}{node.name}: {map_type_to_rust(node.value)}"
            )
        )
        return BulkNode(sources=sources)

    @beartype
    def transform_rust_comment_node(self, node: RustCommentNode) -> SourceNode:
        sources = []
        if node.cargo_doc:
            for line in node.content:
                sources.append(LineNode(content=TextNode(text="/// " + line)))
                sources.append(LineNode(content=TextNode(text="///")))
        else:
            for line in node.content:
                sources.append(LineNode(content=TextNode(text="// " + line)))
        return BulkNode(sources=sources)

    @beartype
    def transform_rust_struct_node(self, node: RustStructNode):
        sources = []
        for anno in node.annotations:
            sources.append(self.transform(anno))

        sources.append(TextNode(text=f"{node.access.value}struct {node.name} "))
        in_braces = []
        if node.fields:
            line = []
            for i, field in enumerate(node.fields):
                if i > 0:
                    line.append(TextNode(text=","))
                    in_braces.append(LineNode(content=BulkNode(sources=line)))
                    line = []

                line.append(self.transform(field))
            in_braces.append(LineNode(content=BulkNode(sources=line)))
        sources.append(BracesNode(value=BulkNode(sources=in_braces)))
        return BulkNode(sources=sources)

    @beartype
    def transform_rust_enum_node(self, node: RustEnumNode) -> SourceNode:
        sources = []
        for anno in node.annotations:
            sources.append(self.transform(anno))

        sources.append(TextNode(text=f"{node.access.value}enum {node.name} "))
        in_braces = []
        for field in node.variants:
            name = list(field.get_field(Traits.VariantNames))
            mapped = map_field_name(name[0])
            if len(name) > 1 or mapped != name[0]:
                reversed_names = name[:]
                reversed_names.reverse()
                in_braces.append(self.transform(Strum(serialize=reversed_names)))

            in_braces.append(TextNode(text=mapped + ","))
        sources.append(BracesNode(value=BulkNode(sources=in_braces)))
        return BulkNode(sources=sources)

    @beartype
    def transform_strum(self, node: Strum) -> SourceNode:
        return LineNode(
            content=TextNode(text='#[strum({})]'.format(['serialize = "{}"'.format(x) for x in node.serialize])))

    @beartype
    def transform_rust_impl_node(self, node: RustImplNode) -> SourceNode:
        sources = []
        if node.trait:
            sources.append(TextNode(text=f"impl {node.trait} for {node.name} "))
        else:
            sources.append(TextNode(text=f"impl {node.name} "))
        in_braces = []
        for func in node.functions:
            if node.trait and isinstance(func, RustFuncDeclNode):
                func.access = AccessModifier.PRIVATE
            in_braces.append(self.transform(func))
        sources.append(BracesNode(value=BulkNode(sources=in_braces)))
        return BulkNode(sources=sources)

    @beartype
    def transform_rust_func_decl_node(self, node: RustFuncDeclNode) -> SourceNode:
        sources = []
        if node.access == AccessModifier.PUBLIC:
            access_value = "pub "
        else:
            access_value = ""
        if node.is_async:
            async_value = "async "
        else:
            async_value = ""
        sources.append(TextNode(text=access_value + async_value + "fn " + node.name))
        in_braces = []
        for i, arg in enumerate(node.args):
            if i > 0:
                in_braces.append(TextNode(text=", "))
            in_braces.append(self.transform(arg))
        sources.append(
            BracesNode(
                value=BulkNode(sources=in_braces), open="(", close=")", new_line=False
            )
        )
        if isinstance(node.ret, DyType):
            if not node.ret.get_field(Traits.Unit):
                ty = " -> " + map_type_to_rust(node.ret) + " "
                sources.append(TextNode(text=ty))
            else:
                sources.append(TextNode(text=" "))
        elif isinstance(node.ret, RustAstNode):
            sources.append(TextNode(text=" -> "))
            sources.append(self.transform(node.ret))
            sources.append(TextNode(text=" "))
        if isinstance(node.content, str):
            sources.append(BracesNode(value=TextNode(text=node.content)))
        elif isinstance(node.content, list):
            in_braces = []
            for c in node.content:
                in_braces.append(self.transform(c))
            sources.append(BracesNode(value=BulkNode(sources=in_braces)))
        else:
            raise NotImplementedError(str(type(node.content)))
        return BulkNode(sources=sources)

    @beartype
    def transform_rust_func_call_node(self, node: RustFuncCallNode):
        sources = []
        sources.append(self.transform(node.callee))
        sources.append(TextNode(text="("))
        for i, a in enumerate(node.arguments):
            if i > 0:
                sources.append(TextNode(text=", "))

            sources.append(self.transform(a))
        sources.append(TextNode(text=")"))
        return BulkNode(sources=sources)

    @beartype
    def transform_rust_use_node(self, node: RustUseNode) -> SourceNode:
        if node.rename and node.rename != node.path.split("::")[-1]:
            return LineNode(content=TextNode(text=f"use {node.path} as {node.rename}"))
        else:
            return LineNode(content=TextNode(text=f"use {node.path};"))

    @beartype
    def transform_rust_variable_declaration(
            self, node: RustVariableDeclaration
    ) -> SourceNode:
        if node.mutability:
            mutability = " mut"
        else:
            mutability = ""
        sources = [RustRawNode(raw=f"let{mutability} {node.name}")]
        if node.ty:
            sources.extend(
                [RustRawNode(raw=": "), RustRawNode(raw=map_type_to_rust(node.ty))]
            )
        if node.init:
            sources.append(RustRawNode(raw=" = "))
            sources.append(node.init)

        return self.transform_rust_statement_node(RustStatementNode(nodes=sources))


def try_rustfmt(s: str) -> str:
    import subprocess
    import sys

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

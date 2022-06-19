package unidef.languages.rust

import unidef.common.ty.*

case class RustCommon(
    alternativeTypeEncoder: Option[TypeEncoder[String]] = None,
    alternativeTypeDecoder: Option[TypeDecoder[String]] = None
) extends TypeEncoder[String]
    with TypeDecoder[String] {
  // TODO: retain all rust type details
  val KEYWORD_MAPPING: Map[String, String] = Map(
    "as" -> "r#as",
    "break" -> "r#break",
    "const" -> "r#const",
    "continue" -> "r#continue",
    "crate" -> "r#crate",
    "else" -> "r#else",
    "enum" -> "r#enum",
    "extern" -> "r#extern",
    "false" -> "r#false",
    "fn" -> "r#fn",
    "for" -> "r#for",
    "if" -> "r#if",
    "impl" -> "r#impl",
    "in" -> "r#in",
    "let" -> "r#let",
    "loop" -> "r#loop",
    "match" -> "r#match",
    "mod" -> "r#mod",
    "move" -> "r#move",
    "mut" -> "r#mut",
    "pub" -> "r#pub",
    "ref" -> "r#ref",
    "return" -> "r#return",
    "self" -> "r#self",
    "Self" -> "r#Self",
    "static" -> "r#static",
    "struct" -> "r#struct",
    "super" -> "r#super",
    "trait" -> "r#trait",
    "true" -> "r#true",
    "type" -> "ty",
    "unsafe" -> "r#unsafe",
    "use" -> "r#use",
    "where" -> "r#where",
    "while" -> "r#while",
    "async" -> "r#async",
    "await" -> "r#await",
    "dyn" -> "r#dyn"
  )
  val DEFAULT_DERIVE: List[String] =
    List("Clone", "Debug", "PartialEq", "serde::Serialize", "serde::Deserialize")
  val ENUM_DEFAULT_DERIVE: List[String] = List(
    "Copy",
    "Clone",
    "Debug",
    "PartialEq",
    "Eq",
    "serde::Serialize",
    "serde::Deserialize",
    "strum::EnumString",
    "strum::Display"
  )

  override def encode(ty: TyNode): Option[String] = {
    ty match {
      case x: TyOption =>
        encode(x.value).map(x => s"Option<${x}>")
      case x: TyString =>
        Some(s"String")
      case x: TyNamed =>
        Some(x.ref)
      case x: TyTuple =>
        val types = x.values.map(encode)
        types.filter(_.isEmpty).foreach(return None)
        Some(types.mkString("(", ", ", ")"))
      case x: TyList =>
        encode(x.value).map(x => s"Vec<${x}>")
      case x: TyJsonAny =>
        Some("serde_json::Value")
      case x: TyInteger if x.bitSize.isDefined && x.signed.isDefined =>
        val size = if (x.signed.get) {
          "i"
        } else {
          "u"
        }
        val bitSize = x.bitSize.get match {
          case BitSize.B8 => "8"
          case BitSize.B16 => "16"
          case BitSize.B32 => "32"
          case BitSize.B64 => "64"
          case BitSize.B128 => "128"
          case BitSize.B256 => "256"
          case BitSize.Native => "size"
          case _ => return None
        }
        Some(s"${size}${bitSize}")
      case x: TyFloat =>
        val bitSize = x.bitSize.get match {
          case BitSize.B32 => "32"
          case BitSize.B64 => "64"
          case _ => return None
        }
        Some(s"f$bitSize")
      case x: TyMap =>
        encode(x.key).flatMap(k => encode(x.value).map(v => s"HashMap<${k}, ${v}>"))

      case x => alternativeTypeEncoder.flatMap(_.encode(x))
    }
  }

  override def decode(name: String): Option[TyNode] = name match {
    case s"Option<$s>" =>
      decode(s).map(Types.option)
    case "String" =>
      Some(Types.string())
    case s"Vec<$s>" =>
      decode(s).map(Types.list)
    case s"&str" => Some(TyReferenceBuilder().referee(Types.string()).build())
    case s"HashMap<$s1, $s2>" =>
      decode(s1).flatMap(k => decode(s2).map(v => Types.map(k, v, "hash")))
    case s"BTreeMap<$s1, $s2>" =>
      decode(s1).flatMap(k => decode(s2).map(v => Types.map(k, v, "btree")))
    case "i8" => Some(Types.i8())
    case "i16" => Some(Types.i16())
    case "i32" => Some(Types.i32())
    case "i64" => Some(Types.i64())
    case "i128" => Some(Types.i128())
    case "isize" => Some(Types.isize())
    case "u8" => Some(Types.u8())
    case "u16" => Some(Types.u16())
    case "u32" => Some(Types.u32())
    case "u64" => Some(Types.u64())
    case "u128" => Some(Types.u128())
    case "usize" => Some(Types.usize())
    case "f32" => Some(Types.f32())
    case "f64" => Some(Types.f64())
    case "serde_json::Value" =>
      Some(TyJsonAnyBuilder().build())
    case s => alternativeTypeDecoder.flatMap(_.decode(s))
  }
}

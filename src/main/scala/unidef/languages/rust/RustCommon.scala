package unidef.languages.rust

import unidef.common.ty.{
  BitSize,
  TyEnum,
  TyFloat,
  TyInteger,
  TyJsonAny,
  TyList,
  TyMap,
  TyNamed,
  TyNode,
  TyOptional,
  TyString,
  TyTuple,
  TyUnit,
  TypeDecoder,
  TypeEncoder
}

case class RustCommon() extends TypeEncoder[String] with TypeDecoder[String] {
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
      case x: TyOptional =>
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
//      case x: TyReference =>
//        Some("String")
      case x: TyString =>
        Some("String")
    }
  }

  override def decode(name: String): Option[TyNode] = ???
}

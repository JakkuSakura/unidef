package unidef.languages.scala

import unidef.common.ty.*
import unidef.utils.TextTool

case class ScalaCommon(
    alternativeTypeEncoder: Option[TypeEncoder[String]] = None,
    alternativeTypeDecoder: Option[TypeDecoder[String]] = None
) extends TypeEncoder[String]
    with TypeDecoder[String] {
  // TODO: retain all scala type details
  override def encode(ty: TyNode): Option[String] =
    ty match {
      case _: TyInteger => Some("Int")
      case _: TyString => Some("String")
      case _: TyUnit => Some("Unit")
      case _: TyBoolean => Some("Boolean")
      case t: TyOption => encode(t.value).map(x => s"Option[${x}]")
      case _: TyAny => Some("Any")
      case t: TyList => encode(t.value).map(x => s"List[${x}]")
      case t: TySeq => encode(t.value).map(x => s"Seq[${x}]")
      case x: TyNamed => Some(x.ref)
      case TyNode => Some("TyNode")
      case _ => alternativeTypeEncoder.flatMap(_.encode(ty))
    }

  override def decode(ty: String): Option[TyNode] = ty match {
    case s"scala.$s" => decode(s)
    case "Int" => Some(Types.i32())
    case "String" => Some(Types.string())
    case "Unit" => Some(Types.unit())
    case "Boolean" => Some(Types.bool())
    case "Any" => Some(Types.any())
    case "Long" => Some(Types.i64())
    case "Float" => Some(Types.f32())
    case "Double" => Some(Types.f64())
    case s"Option[$s]" => decode(s).map(Types.option)
    case s"List[$s]" => decode(s).map(Types.list)
    case s"Seq[$s]" => decode(s).map(x => TySeqImpl(x))
    case s"HashMap<$s1, $s2>" =>
      decode(s1).flatMap(k => decode(s2).map(v => Types.map(k, v, "hash")))
    case s"TreeMap<$s1, $s2>" =>
      decode(s1).flatMap(k => decode(s2).map(v => Types.map(k, v, "tree")))
    case s"Set[$s]" => decode(s).map(x => TySetImpl(x))
    case x => alternativeTypeDecoder.flatMap(_.decode(x))
  }
}

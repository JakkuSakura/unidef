package unidef.languages.scala

import unidef.languages.common.*
import unidef.utils.TextTool

class ScalaCommon() extends TypeEncoder[String] {
  override def encode(ty: TyNode): Option[String] =
    ty match {
      case _: TyInteger => Some("Int")
      case _: TyString => Some("String")
      case TyUnit => Some("Unit")
      case _: TyBoolean => Some("Boolean")
      case t: TyOptional => encode(t.getContent.get).map(x => s"Option[${x}]")
      case TyAny => Some("Any")
      case t: TyList => encode(t.getContent.get).map(x => s"List[${x}]")
      case TyNamed(name) => Some(TextTool.toPascalCase(name))
      case TyNode => Some("TyNode")
      case _ => None
    }
}

package unidef.languages.scala

import unidef.languages.common.*
import unidef.utils.TextTool

class ScalaCommon() extends TypeEncoder[String] {
  override def encode(ty: TyNode): Option[String] =
    ty match {
      case _: TyInteger => Some("Int")
      case TyString => Some("String")
      case TyUnit => Some("Unit")
      case TyBoolean => Some("Boolean")
      case TyOptional(t) => encode(t).map(x => s"Option[${x}]")
      case TyAny => Some("Any")
      case TyList(t) => encode(t).map(x => s"List[${x}]")
      case TyNamed(name) => Some(TextTool.toPascalCase(name))
      case _ => None
    }
}

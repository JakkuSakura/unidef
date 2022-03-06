package unidef.languages.scala

import unidef.languages.common.*

class ScalaCommon() extends TypeEncoder {
  override def encode(ty: TyNode): Option[String] =
    ty match {
      case _: TyInteger => Some("Int")
      case TyString => Some("String")
      case TyUnit => Some("Unit")
      case TyOptional(t) => encode(t).map(x => s"Option[${x}]")
      case TyList(t) => encode(t).map(x => s"List[${x}]")
      case _ => None
    }
}

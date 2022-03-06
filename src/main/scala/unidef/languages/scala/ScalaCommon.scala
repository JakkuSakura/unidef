package unidef.languages.scala

import unidef.languages.common.{TyInteger, TyNode, TyUnit, TypeDecoder, TypeEncoder}

class ScalaCommon() extends TypeEncoder {
  override def encode(ty: TyNode): Option[String] =
    ty match {
      case _: TyInteger => Some("Int")
      case TyUnit => Some("Unit")
      case _ => None
    }
}

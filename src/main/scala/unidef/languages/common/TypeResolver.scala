package unidef.languages.common

import com.typesafe.scalalogging.Logger
import unidef.utils.TextTool

import scala.collection.mutable

trait TypeResolver {
  def decode(typeName: String): Option[TyNode]
}

object EmptyTypeResolver extends TypeResolver {
  override def decode(typeName: String): Option[TyNode] = None
}

case class TypeRegistry() extends TypeResolver {
  val logger: Logger = Logger[this.type]
  val mapping = new mutable.HashMap[String, TyNode]()

  def add(s: String, ty: TyNode with Extendable, source_lang: String): Unit = {
    logger.debug(s"Add type $s from $source_lang")
    mapping += TextTool.toSnakeCase(s) -> ty.setValue(KeyLanguage, source_lang)
  }

  override def decode(typeName: String): Option[TyNode] = {
    mapping.get(TextTool.toSnakeCase(typeName))
  }

}

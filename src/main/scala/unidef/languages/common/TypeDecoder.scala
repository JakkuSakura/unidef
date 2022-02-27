package unidef.languages.common

import com.typesafe.scalalogging.Logger
import unidef.utils.TextTool

import scala.collection.mutable

trait TypeDecoder {
  def decode(name: String): Option[TyNode]
}
trait TypeEncoder {
  def encode(ty: TyNode): Option[String]
}

object EmptyTypeDecoder extends TypeDecoder {
  override def decode(name: String): Option[TyNode] = None
}

case class TypeRegistry() extends TypeDecoder {
  val logger: Logger = Logger[this.type]
  val mapping = new mutable.HashMap[String, TyNode]()

  def add(s: String, ty: TyNode with Extendable, source_lang: String): Unit = {
    logger.debug(s"Add type $s from $source_lang")
    mapping += TextTool.toSnakeCase(s) -> ty.setValue(KeyLanguage, source_lang)
  }

  override def decode(name: String): Option[TyNode] = {
    mapping.get(TextTool.toSnakeCase(name))
  }

}

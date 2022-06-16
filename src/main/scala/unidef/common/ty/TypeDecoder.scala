package unidef.common.ty

import com.typesafe.scalalogging.Logger
import unidef.common.Extendable
import unidef.common.ast.KeyLanguage
import unidef.utils.{TextTool, TypeDecodeException, TypeEncodeException}

import scala.collection.mutable

trait TypeDecoder[I] {
  def decode(name: I): Option[TyNode]
  def decodeOrThrow(name: I, lang: String = ""): TyNode =
    decode(name).getOrElse(throw TypeDecodeException[I](s"Cannot decode $lang", name))
}
trait TypeEncoder[O] {

  def encode(ty: TyNode): Option[O]
  def encodeOrThrow(ty: TyNode, lang: String = ""): O =
    encode(ty).getOrElse(throw TypeEncodeException(s"Cannot encode $lang", ty))
}

object EmptyTypeDecoder extends TypeDecoder[String] {
  override def decode(name: String): Option[TyNode] = None
}

case class TypeRegistry() extends TypeDecoder[String] {
  val logger: Logger = Logger[this.type]
  val mapping = new mutable.HashMap[String, TyNode]()

  def add(s: String, ty: TyNode, source_lang: String): Unit = {
    logger.debug(s"Add type $s from $source_lang")
    mapping += TextTool.toSnakeCase(s) -> ty
  }

  override def decode(name: String): Option[TyNode] = {
    mapping.get(TextTool.toSnakeCase(name))
  }

}

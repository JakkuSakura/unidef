package unidef.common

import io.circe.Decoder
import unidef.common.ty.*
import unidef.utils.TextTool
import unidef.utils.TextTool.toSnakeCase

import scala.collection.mutable

// Assume TypedValue is always type checked.

trait Keyword {
  type V <: Any

  def name: String =
    toSnakeCase(getClass.getSimpleName.stripSuffix("$").stripPrefix("Key"))
  def decoder: Option[Decoder[V]] = None
}

trait KeywordBoolean extends Keyword {
  override type V = Boolean
  override def decoder: Option[Decoder[Boolean]] = Some(Decoder.decodeBoolean)
}

trait KeywordString extends Keyword {
  override type V = String
  override def decoder: Option[Decoder[V]] = Some(Decoder.decodeString)
}

trait KeywordInt extends Keyword {
  override type V = Int
  override def decoder: Option[Decoder[V]] = Some(Decoder.decodeInt)
}

trait KeywordOnly extends Keyword {
  override type V = Unit
  override def decoder: Option[Decoder[V]] = None
}

class Extendable(
  private val params: mutable.Map[String, Any] = mutable.HashMap()
) {

  def getValue(key: Keyword): Option[key.V] =
    params.get(key.name).asInstanceOf[Option[key.V]]

  def setValue[EK <: Keyword](key: EK, v: key.V): this.type = {
    params += key.name -> v
    this
  }

  def trySetValue[EK <: Keyword](key: EK, v: Option[key.V]): this.type = {
    v match {
      case Some(value) => params += key.name -> value
      case None        =>
    }
    this
  }

  // type unsafe for sure
  def setValue(kv: (Keyword, Any)): this.type = {
    params += kv._1.name -> kv._2
    this
  }
  
  def copyExtended(other: Extendable): this.type = {
    params ++= other.params
    this
  }
}

trait KeywordProvider {
  def keysOnField: Seq[Keyword] = Seq()
  def keysOnFuncDecl: Seq[Keyword] = Seq()
  def keysOnClassDecl: Seq[Keyword] = Seq()
}

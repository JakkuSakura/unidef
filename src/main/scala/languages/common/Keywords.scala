package com.jeekrs.unidef
package languages.common

import io.circe.Decoder

import scala.collection.mutable

// Assume TypedValue is always type checked.

trait Keyword {
  type V <: Any

  def name: String = getClass.getSimpleName.stripSuffix("$").toLowerCase
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

class Extendable(params: mutable.Map[Keyword, Any] = mutable.HashMap()) {
  def getValue(key: Keyword): Option[key.V] =
    params.get(key).asInstanceOf[Option[key.V]]

  def setValue[EK <: Keyword, VV <: EK#V](key: EK, v: VV): this.type = {
    params += key -> v
    this
  }

  // type unsafe for sure
  def setValue(kv: (Keyword, Any)): this.type = {
    params += kv
    this
  }
}

trait KeywordProvider {
  def keysOnField: Seq[Keyword] = Seq()
  def keysOnFuncDecl: Seq[Keyword] = Seq()
  def keysOnClassDecl: Seq[Keyword] = Seq()
}

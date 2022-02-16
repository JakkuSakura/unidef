package com.jeekrs.unidef
package languages.common

import io.circe.Decoder

import scala.collection.mutable

// Assume TypedValue is always type checked.

trait Keyword {
  type V
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

  def setValue[EK <: Keyword { type V = VV }, VV](key: Keyword, v: VV): Unit =
    params += key -> v
  def setValue(kv: (Keyword, Any)): Unit =
    params += kv
}

trait KeywordProvider {
  def keysOnField: List[Keyword] = List()
  def keysOnFuncDecl: List[Keyword] = List()
  def keysOnClassDecl: List[Keyword] = List()
}

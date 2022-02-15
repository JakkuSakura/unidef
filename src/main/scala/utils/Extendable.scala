package com.jeekrs.unidef
package utils

import io.circe.Decoder

import scala.collection.mutable

// Assume TypedValue is always type checked.

trait ExtKey {
  type V
  def name: String = getClass.getSimpleName.stripSuffix("$").toLowerCase
  def decoder: Option[Decoder[V]] = None
}

trait ExtKeyBoolean extends ExtKey {
  override type V = Boolean
  override def decoder: Option[Decoder[Boolean]] = Some(Decoder.decodeBoolean)
}

trait ExtKeyString extends ExtKey {
  override type V = String
  override def decoder: Option[Decoder[V]] = Some(Decoder.decodeString)
}

trait ExtKeyInt extends ExtKey {
  override type V = Int
  override def decoder: Option[Decoder[V]] = Some(Decoder.decodeInt)
}

class Extendable(params: mutable.Map[ExtKey, Any] = mutable.HashMap()) {
  def getValue(key: ExtKey): Option[key.V] =
    params.get(key).asInstanceOf[Option[key.V]]

  def setValue[EK <: ExtKey { type V = VV }, VV](key: ExtKey, v: VV): Unit =
    params += key -> v
  def setValue(kv: (ExtKey, Any)): Unit =
    params += kv
}

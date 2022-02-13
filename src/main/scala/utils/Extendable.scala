package com.jeekrs.unidef
package utils

import io.circe.Decoder

import scala.collection.mutable

// Assume TypedValue is always type checked.
case class TypedValue(key: ExtKey, value: Any)

trait ExtKey {
  type V
  def apply(v: V): TypedValue = TypedValue(this, v)
  def name: String = getClass.getSimpleName.toLowerCase
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
  def getValue[EK <: ExtKey](key: EK): Option[key.V] =
    params.get(key).asInstanceOf[Option[key.V]]

  def setValue(typedValue: TypedValue): Unit =
    params += typedValue.key -> typedValue.value
}

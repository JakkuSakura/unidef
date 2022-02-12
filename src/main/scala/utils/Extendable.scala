package com.jeekrs.unidef
package utils

import java.util.TimeZone
import scala.collection.mutable

// Assume TypedValue is always type checked.
class TypedValue(val key: ExtKey, val value: Any)

trait ExtKey:
    type V
    def apply(v: V): TypedValue = TypedValue(this, v)

class Extendable(params: mutable.Map[ExtKey, Any] = mutable.HashMap()):
    def getValue[EK <: ExtKey](key: EK): Option[key.V] = params.get(key).asInstanceOf[Option[key.V]]

    def setValue(typedValue: TypedValue): Unit = params += typedValue.key -> typedValue.value

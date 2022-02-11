package com.jeekrs.unidef
package languages.common

import java.util.TimeZone
import scala.collection.mutable
import scala.concurrent.duration.TimeUnit

class IrNode(params: mutable.Map[ExtKey, Any] = mutable.HashMap()):
    def getValue[EK <: ExtKey](key: EK): Option[key.V] = params.get(key).asInstanceOf[Option[key.V]]

    def setValue(typedValue: TypedValue): Unit = params += typedValue.key -> typedValue.value

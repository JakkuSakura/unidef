package com.jeekrs.unidef
package languages.common

// Assume TypedValue is always type checked.
class TypedValue(val key: ExtKey, val value: Any)

trait ExtKey:
    type V
    def apply(v: V): TypedValue = TypedValue(this, v)
    
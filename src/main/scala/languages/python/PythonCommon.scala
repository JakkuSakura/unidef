package com.jeekrs.unidef
package languages.python

import languages.common._

object PythonCommon {
  def convertType(node: TyNode): String =
    node match {
      case _: TyInteger                          => "int"
      case _: TyFloat                            => "float"
      case TyString                              => "str"
      case TyChar                                => "str"
      case TyStruct("unnamed", fields, dataType) => "Dict[str, Any]"
      case TyStruct(name, fields, dataType)      => name
      case TyDict(k, v)                          => s"Dict[${convertType(k)}, ${convertType(v)}]"
      case TyList(v)                             => s"List[${convertType(v)}]"
      case TySet(v)                              => s"Set[${convertType(v)}]"
      case TyJsonObject                          => "Any"
      case TyUnit                                => "NoneType"
      case TyTimeStamp(timeUnit, timezone)       => "datetime.datetime"
      case t                                     => s"'$t'"
    }
}

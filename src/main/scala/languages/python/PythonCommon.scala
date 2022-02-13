package com.jeekrs.unidef
package languages.python

import languages.common._

object PythonCommon {
  def convertType(node: TyNode): String =
    node match {
      case _: IntegerType                     => "int"
      case _: FloatType                       => "float"
      case StringType                         => "str"
      case CharType                           => "str"
      case StructType(name, fields, dataType) => name
      case DictType(k, v)                     => s"Dict[${convertType(k)}, ${convertType(v)}]"
      case ListType(v)                        => s"List[${convertType(v)}]"
      case SetType(v)                         => s"Set[${convertType(v)}]"
      case t                                  => s"'$t'"
    }
}

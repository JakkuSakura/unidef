package unidef.languages.python

import unidef.languages.common._

import java.util.concurrent.TimeUnit

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
      case TyTimeStamp()                         => "datetime.datetime"
      case TyBoolean                             => "bool"
      case t                                     => s"'$t'"
    }
  def convertTypeFromPy(ty: String): TyNode =
    ty match {
      case "int"               => TyInteger(BitSize.Unlimited)
      case "float"             => TyFloat(BitSize.Unlimited)
      case "str"               => TyString
      case "bool"              => TyBoolean
      case "NoneType"          => TyUnit
      case "datetime.datetime" => TyTimeStamp()
      case "Dict[str, Any]"    => TyJsonObject
      case _                   => TyString
    }
}

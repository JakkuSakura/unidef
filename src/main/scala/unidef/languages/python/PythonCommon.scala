package unidef.languages.python

import unidef.languages.common._

object PythonCommon {
  def convertType(node: TyNode): String =
    node match {
      case _: TyInteger => "int"
      case _: TyFloat   => "float"
      case TyString     => "str"
      case TyChar       => "str"
      case t @ TyStruct(_) if t.getValue(Name).isDefined =>
        t.getValue(Name).get
      case TyStruct(_)   => "Dict[str, Any]"
      case TyRecord      => "List[Dict[str, Any]]"
      case TyDict(k, v)  => s"Dict[${convertType(k)}, ${convertType(v)}]"
      case TyList(v)     => s"List[${convertType(v)}]"
      case TySet(v)      => s"Set[${convertType(v)}]"
      case TyJsonObject  => "Any"
      case TyUnit        => "NoneType"
      case TyTimeStamp() => "datetime.datetime"
      case TyBoolean     => "bool"
      case TyByteArray   => "bytes"
      case TyUuid        => "uuid.UUID"
      case TyInet        => "str" // FIXME: InetAddress
      case t             => s"'$t'"
    }
  def convertTypeFromPy(ty: String): TyNode =
    ty match {
      case "int"                  => TyInteger(BitSize.Unlimited)
      case "float"                => TyFloat(BitSize.Unlimited)
      case "str"                  => TyString
      case "bool"                 => TyBoolean
      case "NoneType"             => TyUnit
      case "datetime.datetime"    => TyTimeStamp()
      case "Dict[str, Any]"       => TyJsonObject
      case "List[Dict[str, Any]]" => TyRecord
      case "bytes"                => TyByteArray
      case "uuid.UUID" | "UUID"   => TyUuid
      case _                      => TyString
    }
}

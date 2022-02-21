package unidef.languages.python

import unidef.languages.common._

object PythonCommon {
  def convertType(node: TyNode)(implicit resolver: TypeResolver): String =
    node match {
      case _: TyInteger => "int"
      case _: TyFloat   => "float"
      case TyString     => "str"
      case TyChar       => "str"
      case t @ TyStruct() if t.getValue(KeyName).isDefined =>
        PythonNamingConvention.toStructName(t.getValue(KeyName).get)
      case TyStruct()    => "Dict[str, Any]"
      case TyRecord      => "List[Dict[str, Any]]"
      case TyDict(k, v)  => s"Dict[${convertType(k)}, ${convertType(v)}]"
      case TyList(v)     => s"List[${convertType(v)}]"
      case TySet(v)      => s"Set[${convertType(v)}]"
      case TyJsonObject  => "Any"
      case TyUnit        => "None"
      case TyTimeStamp() => "datetime.datetime"
      case TyBoolean     => "bool"
      case TyByteArray   => "bytes"
      case TyUuid        => "uuid.UUID"
      case TyInet        => "str" // FIXME: InetAddress
      case TyNamed(name) =>
        resolver
          .decode(name)
          .map(convertType)
          .getOrElse(s"'${PythonNamingConvention.toClassName(name)}'")
      case x @ TyEnum(_) if x.getValue(KeyName).isDefined =>
        PythonNamingConvention.toClassName(x.getValue(KeyName).get)
      case TyEnum(_) => "str" // TODO: use solid enum if possible
      case t         => s"'$t'"
    }
  def convertTypeFromPy(ty: String): TyNode =
    ty match {
      case "int"                  => TyInteger(BitSize.Unlimited)
      case "float"                => TyFloat(BitSize.Unlimited)
      case "str"                  => TyString
      case "bool"                 => TyBoolean
      case "NoneType" | "None"    => TyUnit
      case "datetime.datetime"    => TyTimeStamp()
      case "Dict[str, Any]"       => TyJsonObject
      case "List[Dict[str, Any]]" => TyRecord
      case "bytes"                => TyByteArray
      case "uuid.UUID" | "UUID"   => TyUuid
      case _                      => TyString
    }

}

package unidef.languages.python

import unidef.languages.common._

case class PythonCommon(naming: NamingConvention = PythonNamingConvention) {
  def convertType(node: TyNode): Option[String] =
    node match {
      case _: TyInteger => Some("int")
      case _: TyFloat   => Some("float")
      case TyString     => Some("str")
      case TyChar       => Some("str")
      case t @ TyStruct() if t.getValue(KeyName).isDefined =>
        Some(naming.toStructName(t.getValue(KeyName).get))
      case TyStruct()    => Some("Dict[str, Any]")
      case TyRecord      => Some("List[Dict[str, Any]]")
      case TyDict(k, v)  => Some(s"Dict[${convertType(k)}, ${convertType(v)}]")
      case TyList(v)     => Some(s"List[${convertType(v)}]")
      case TySet(v)      => Some(s"Set[${convertType(v)}]")
      case TyJsonObject  => Some("Any")
      case TyUnit        => Some("None")
      case TyTimeStamp() => Some("datetime.datetime")
      case TyBoolean     => Some("bool")
      case TyByteArray   => Some("bytes")
      case TyUuid        => Some("uuid.UUID")
      case TyInet        => Some("str") // FIXME: InetAddress
      case x @ TyEnum(_) if x.getValue(KeyName).isDefined =>
        Some(naming.toClassName(x.getValue(KeyName).get))
      case TyEnum(_) => Some("str") // TODO: use solid enum if possible
      case _ => None
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

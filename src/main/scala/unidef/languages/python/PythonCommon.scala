package unidef.languages.python

import unidef.languages.common._

class PythonCommon(val naming: NamingConvention = PythonNamingConvention) {
  def convertType(node: TyNode, importManager: Option[ImportManager] = None): Option[String] =
    node match {
      case _: TyInteger => Some("int")
      case _: TyFloat => Some("float")
      case TyString => Some("str")
      case TyChar => Some("str")
      case t @ TyStruct() if t.getValue(KeyName).isDefined =>
        Some(naming.toStructName(t.getValue(KeyName).get))
      case TyStruct() => {
        importManager.foreach(_ += AstImport("typing", Seq("Dict")))
        Some("Dict[str, Any]")
      }
      case TyRecord => {
        importManager.foreach(_ += AstImport("typing", Seq("Dict")))
        importManager.foreach(_ += AstImport("typing", Seq("List")))
        Some("List[Dict[str, Any]]")
      }
      case TyDict(k, v) => {
        importManager.foreach(_ += AstImport("typing", Seq("Dict")))
        (convertType(k, importManager), convertType(v, importManager)) match {
          case (Some(k), Some(v)) => Some(s"Dict[${}, ${}]")
          case _ => None
        }
      }
      case TyList(v) => {
        importManager.foreach(_ += AstImport("typing", Seq("Dict")))
        convertType(v, importManager).map(x => s"List[${x}]")
      }
      case TySet(v) => {
        importManager.foreach(_ += AstImport("typing", Seq("Set")))
        convertType(v, importManager).map(x => s"Set[${x}]")
      }
      case TyJsonObject | TyJsonAny() => {
        importManager.foreach(_ += AstImport("typing", Seq("Any")))
        Some("Any")
      }
      case TyUnit => Some("None")
      case TyTimeStamp() => {
        importManager.foreach(_ += AstImport("datetime"))
        Some("datetime.datetime")
      }
      case TyBoolean => Some("bool")
      case TyByteArray => Some("bytes")
      case TyUuid => {
        importManager.foreach(_ += AstImport("uuid"))
        Some("uuid.UUID")
      }
      case TyInet => Some("str") // FIXME: InetAddress
      case x @ TyEnum(_) if x.getValue(KeyName).isDefined =>
        Some(naming.toClassName(x.getValue(KeyName).get))
      case TyEnum(_) => Some("str") // TODO: use solid enum if possible
      case _ => None
    }
  def convertTypeFromPy(ty: String): TyNode =
    ty match {
      case "int" => TyInteger(BitSize.Unlimited)
      case "float" => TyFloat(BitSize.Unlimited)
      case "str" => TyString
      case "bool" => TyBoolean
      case "NoneType" | "None" => TyUnit
      case "datetime.datetime" => TyTimeStamp()
      case "Dict[str, Any]" => TyJsonObject
      case "List[Dict[str, Any]]" => TyRecord
      case "bytes" => TyByteArray
      case "uuid.UUID" | "UUID" => TyUuid
      case _ => TyString
    }

}

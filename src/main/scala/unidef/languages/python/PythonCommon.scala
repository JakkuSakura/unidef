package unidef.languages.python

import unidef.languages.common._

class PythonCommon(val naming: NamingConvention = PythonNamingConvention)
    extends TypeEncoder[String]
    with TypeDecoder[String] {
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
      case TyJsonAny() => {
        importManager.foreach(_ += AstImport("typing", Seq("Any")))
        Some("Any")
      }
      case TyJsonObject => {
        importManager.foreach(_ += AstImport("typing", Seq("Any")))
        importManager.foreach(_ += AstImport("typing", Seq("Dict")))
        Some("Dict[str, Any]")
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
  def convertTypeFromPy(ty: String): Option[TyNode] =
    ty match {
      case "int" => Some(TyInteger(BitSize.Unlimited))
      case "float" => Some(TyFloat(BitSize.Unlimited))
      case "str" => Some(TyString)
      case "bool" => Some(TyBoolean)
      case "NoneType" | "None" => Some(TyUnit)
      case "datetime.datetime" => Some(TyTimeStamp())
      case "Dict[str, Any]" => Some(TyJsonObject)
      case "List[Dict[str, Any]]" => Some(TyRecord)
      case "bytes" => Some(TyByteArray)
      case "uuid.UUID" | "UUID" => Some(TyUuid)
      case _ => None
    }

  override def encode(ty: TyNode): Option[String] = convertType(ty)

  override def decode(name: String): Option[TyNode] = convertTypeFromPy(name)
}

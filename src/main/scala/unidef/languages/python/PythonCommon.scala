package unidef.languages.python

import unidef.languages.common._

class PythonCommon(val naming: NamingConvention = PythonNamingConvention)
    extends TypeEncoder[String]
    with TypeDecoder[String] {
  def convertType(node: TyNode, importManager: Option[ImportManager] = None): Option[String] =
    node match {
      case _: TyInteger => Some("int")
      case _: TyReal => Some("float")
      case _: TyString => Some("str")
      // case TyChar => Some("str")
      case t: TyStruct if t.getName.isDefined =>
        Some(naming.toStructName(t.getName.get))
      case _: TyStruct => {
        importManager.foreach(_ += AstImport("typing", Seq("Dict")))
        Some("Dict[str, Any]")
      }
      // case _: TyRecord => {
      //  importManager.foreach(_ += AstImport("typing", Seq("Dict")))
      //  importManager.foreach(_ += AstImport("typing", Seq("List")))
      //  Some("List[Dict[str, Any]]")
      // }
      case TyDict(k, v) => {
        importManager.foreach(_ += AstImport("typing", Seq("Dict")))
        (convertType(k, importManager), convertType(v, importManager)) match {
          case (Some(k), Some(v)) => Some(s"Dict[${}, ${}]")
          case _ => None
        }
      }
      case l: TyList => {
        importManager.foreach(_ += AstImport("typing", Seq("Dict")))
        convertType(l.getContent.getOrElse(TyAny), importManager).map(x => s"List[${x}]")
      }
      case s: TySet => {
        importManager.foreach(_ += AstImport("typing", Seq("Set")))
        convertType(s.getContent.getOrElse(TyAny), importManager).map(x => s"Set[${x}]")
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
      case _: TyBoolean => Some("bool")
      case _: TyByteArray => Some("bytes")
      case TyUuid => {
        importManager.foreach(_ += AstImport("uuid"))
        Some("uuid.UUID")
      }
      case TyInet => Some("str") // FIXME: InetAddress
      case x @ TyEnum(_) if x.getValue(KeyName).isDefined =>
        Some(naming.toClassName(x.getValue(KeyName).get))
      case TyEnum(_) =>
        Some("str") // TODO: use solid enum if possible
      case TyAny =>
        importManager.foreach(_ += AstImport("typing", Seq("Any")))
        Some("Any")
      case _ => None
    }
  def convertTypeFromPy(ty: String): Option[TyNode] =
    ty match {
      case "int" => Some(TyIntegerImpl(Some(BitSize.Unlimited), Some(true)))
      case "float" => Some(TyFloatImpl(Some(BitSize.Unlimited)))
      case "str" => Some(TyStringImpl())
      case "bool" => Some(TyBooleanImpl())
      case "NoneType" | "None" => Some(TyUnit)
      case "datetime.datetime" => Some(TyTimeStamp())
      case "Dict[str, Any]" => Some(TyJsonObject)
      // case "List[Dict[str, Any]]" => Some(TyRecordImpl())
      case "bytes" => Some(TyByteArrayImpl())
      case "uuid.UUID" | "UUID" => Some(TyUuid)
      case _ => None
    }

  override def encode(ty: TyNode): Option[String] = convertType(ty)

  override def decode(name: String): Option[TyNode] = convertTypeFromPy(name)
}

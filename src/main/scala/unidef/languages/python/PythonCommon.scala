package unidef.languages.python

import unidef.common.NamingConvention
import unidef.common.ast.{AstImport, ImportManager}
import unidef.common.ty.*

case class PythonCommon(
    naming: NamingConvention = PythonNamingConvention,
    alternativeTypeEncoder: Option[TypeEncoder[String]] = None,
    alternativeTypeDecoder: Option[TypeDecoder[String]] = None
) extends TypeEncoder[String]
    with TypeDecoder[String] {
  def convertType(node: TyNode, importManager: Option[ImportManager] = None): Option[String] =
    node match {
      // handle unknown case
      case x: TyOption =>
        convertType(x.value, importManager)
          .map(s =>
            importManager.foreach(_ += AstImport("typing.Optional"))
            s"Optional[$s]"
          )
      case _: TyInteger => Some("int")
      case _: TyOid => Some("int")
      case _: TyReal => Some("float")
      case _: TyString => Some("str")
      case _: TyChar => Some("str")
      case t: TyStruct if t.name.isDefined =>
        Some(naming.toStructName(t.name.get))
      case _: TyStruct =>
        importManager.foreach(_ += AstImport("typing.Dict"))
        Some("Dict[str, Any]")

      case _: TyRecord =>
        importManager.foreach(_ += AstImport("typing.Dict"))
        importManager.foreach(_ += AstImport("typing.List"))
        Some("List[Dict[str, Any]]")

      case m: TyMap =>
        val k = m.key
        val v = m.value
        importManager.foreach(_ += AstImport("typing.Dict"))
        (convertType(k, importManager), convertType(v, importManager)) match {
          case (Some(k), Some(v)) => Some(s"Dict[${}, ${}]")
          case _ => None
        }

      case l: TyList =>
        importManager.foreach(_ += AstImport("typing.List"))
        // handle unknown case
        convertType(l.value, importManager).map(x => s"List[${x}]")
      case s: TySet =>
        importManager.foreach(_ += AstImport("typing.Set"))
        // handle unknown case
        convertType(s.value, importManager).map(x => s"Set[${x}]")
      case _: TyJsonAny =>
        importManager.foreach(_ += AstImport("typing.Any"))
        Some("Any")
      case _: TyJsonObject =>
        importManager.foreach(_ += AstImport("typing.Any"))
        importManager.foreach(_ += AstImport("typing.Dict"))
        Some("Dict[str, Any]")
      case _: TyUnit => Some("None")
      case _: TyTimeStamp =>
        importManager.foreach(_ += AstImport("datetime"))
        Some("datetime.datetime")
      case _: TyBoolean => Some("bool")
      case _: TyByteArray => Some("bytes")
      case _: TyUuid =>
        importManager.foreach(_ += AstImport("uuid"))
        Some("uuid.UUID")
      case _: TyInet => Some("str") // FIXME: InetAddress
      case x: TyEnum if x.name.isDefined =>
        Some(naming.toClassName(x.name.get))
      case _: TyEnum =>
        Some("str") // TODO: use solid enum if possible
      case _: TyAny =>
        importManager.foreach(_ += AstImport("typing.Any"))
        Some("Any")
      case _ => alternativeTypeEncoder.flatMap(_.encode(node))
    }
  def convertTypeFromPy(ty: String): Option[TyNode] =
    ty match {
      case "int" => Some(TyIntegerImpl(Some(BitSize.Unlimited), Some(true)))
      case "float" => Some(TyFloatImpl(Some(BitSize.Unlimited)))
      case "str" => Some(Types.string())
      case "bool" => Some(Types.bool())
      case "NoneType" | "None" => Some(Types.unit())
      case "datetime.datetime" => Some(TyTimeStampBuilder().build())
      case "Dict[str, Any]" => Some(TyJsonObjectBuilder().build())
      case "List[Dict[str, Any]]" => Some(TyRecordImpl())
      case "bytes" => Some(TyByteArrayImpl())
      case "uuid.UUID" | "UUID" => Some(TyUuidImpl())
      case _ => alternativeTypeDecoder.flatMap(_.decode(ty))
    }

  override def encode(ty: TyNode): Option[String] = convertType(ty)

  override def decode(name: String): Option[TyNode] = convertTypeFromPy(name)
}

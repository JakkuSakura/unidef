package unidef.languages.sql

import com.typesafe.scalalogging.Logger
import unidef.common.{KeywordBoolean, KeywordString, NamingConvention}
import unidef.common.ty.*
import unidef.languages.sql.{KeyNullable, KeyPrimary}
import unidef.utils.TypeEncodeException
import java.util.concurrent.TimeUnit
import scala.collection.mutable
object SqlCommon {
  // multiple rows
  case object KeyRecords extends KeywordBoolean

  case object KeySchema extends KeywordString
}

class TyOid extends TyInteger {
  def bitSize: Option[BitSize] = Some(BitSize.B32)

  def sized: Option[Boolean] = Some(false)

}
class SqlCommon(naming: NamingConvention = SqlNamingConvention)
    extends TypeDecoder[String]
    with TypeEncoder[String] {

  def convertReal(ty: TyReal): String = ty match {
    case x: TyDecimal if x.precision.isDefined && x.scale.isDefined =>
      s"decimal(${x.precision.get}, ${x.scale.get})"
    case _: TyDecimal => s"decimal"
    case x: TyFloat if x.bitSize.contains(BitSize.B32) => "real"
    case x: TyFloat if x.bitSize.contains(BitSize.B64) => "float" // "double precision"
  }

  def convertInt(ty: TyInteger): String = ty.bitSize match {
    case Some(BitSize.B16) => "smallint"
    case Some(BitSize.B64) => "bigint"
    case Some(x) if x != BitSize.B32 => s"integer($x)"
    case _ => "integer"
  }

  def convertType(ty: TyNode): String = ty match {
    case t: TyNamed => t.name
    case t => encode(t).getOrElse(throw TypeEncodeException("SQL", t))
  }

  override def encode(ty: TyNode): Option[String] = ty match {
    case t: TyOptional => encode(t.content).map(s => s"$s = NULL")
    case t: TyReal => Some(convertReal(t))
    case t: TyOid => Some("oid")
    case t: TyInteger => Some(convertInt(t))
    case t: TyTimeStamp if t.hasTimeZone.contains(true) =>
      Some("timestamp with time zone")
    // case TimeStampType(_, false) => "timestamp without time zone"
    case _: TyTimeStamp => Some("timestamp")
    case _: TyString => Some("text")
    case _: TyStruct => Some("jsonb")
    case x: TyEnum if x.simpleEnum.contains(false) => Some("jsonb")
    case x: TyEnum if x.name.isDefined =>
      x.name
    case _: TyEnum => Some("text")
    case TyJsonObject => Some("jsonb")
    case t: TyJsonAny if !t.getValue(KeyIsBinary).contains(false) => Some("jsonb")
    case t: TyJsonAny => Some("json")
    case _: TyUnit => Some("void")
    case _: TyBoolean => Some("boolean")
    case _: TyByteArray => Some("bytea")
    case _: TyInet => Some("inet")
    case _: TyUuid => Some("uuid")
    case _: TyRecord => Some("record")
    case t: TyList => encode(t.content).map(x => s"${x}[]")
    case _ => None
  }

  override def decode(ty: String): Option[TyNode] = {
    ty match {
      case s"$ty[]" => decode(ty).map(x => TyListImpl(x))
      case "bigint" | "bigserial" => Some(TyIntegerImpl(Some(BitSize.B64), Some(true)))
      case "integer" | "int" | "serial" => Some(TyIntegerImpl(Some(BitSize.B32), Some(true)))
      case "smallint" => Some(TyIntegerImpl(Some(BitSize.B16), Some(true)))
      case "double precision" | "float" => Some(TyFloatImpl(Some(BitSize.B64)))
      case "real" => Some(TyFloatImpl(Some(BitSize.B32)))
      case "decimal" | "numeric" => Some(TyDecimalImpl(None, None))
      case "timestamp" | "timestamp without time zone" =>
        Some(
          TyTimeStamp(timeUnit = Some(TimeUnit.MILLISECONDS), hasTimeZone = Some(false))
        )
      case "timestamp with time zone" =>
        Some(
          TyTimeStamp(timeUnit = Some(TimeUnit.MILLISECONDS), hasTimeZone = Some(true))
        )
      case "text" | "varchar" => Some(TyStringImpl())
      case "jsonb" => Some(TyJsonAny().setValue(KeyIsBinary, true))
      case "json" => Some(TyJsonAny().setValue(KeyIsBinary, false))
      case "void" => Some(TyUnitImpl())
      case "oid" => Some(TyOid())
      case "bool" | "boolean" => Some(TyBooleanImpl())
      case "bytea" => Some(TyByteArrayImpl())
      case "inet" => Some(TyInetImpl())
      case "uuid" => Some(TyUuidImpl())
      case "record" => Some(TyRecordImpl())
      case _ => None
    }
  }

  def convertToSqlField(node: TyField): SqlField = {
    val attributes = new mutable.StringBuilder()
//    if (node.getValue(KeyPrimary).contains(true))
//      attributes ++= " PRIMARY KEY"
//    if (!node.getValue(KeyNullable).contains(true))
//      attributes ++= " NOT NULL"
    // TODO auto incr
    SqlField(
      naming.toFieldName(node.name.get),
      convertType(node.value),
      attributes.toString
    )
  }

}

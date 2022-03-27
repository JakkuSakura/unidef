package unidef.languages.sql

import com.typesafe.scalalogging.Logger
import unidef.languages.common.*
import unidef.languages.sql.{KeyNullable, KeyPrimary}
import unidef.utils.TypeEncodeException

import java.util.concurrent.TimeUnit
import scala.collection.mutable
object SqlCommon {
  // multiple rows
  case object KeyRecords extends KeywordBoolean

  case object KeySchema extends KeywordString
}

class SqlCommon(naming: NamingConvention = SqlNamingConvention)
    extends TypeDecoder[String]
    with TypeEncoder[String] {

  case object KeyOid extends KeywordBoolean {
    def get: TyInteger =
      TyIntegerImpl(Some(BitSize.B32), Some(false)).setValue(KeyOid, true)
  }

  def convertReal(ty: TyReal): String = ty match {
    case x: TyDecimal if x.getPrecision.isDefined && x.getScale.isDefined =>
      s"decimal(${x.getPrecision.get}, ${x.getScale.get})"
    case _: TyDecimal => s"decimal"
    case x: TyFloat if x.getBitSize.contains(BitSize.B32) => "real"
    case x: TyFloat if x.getBitSize.contains(BitSize.B64) => "float" // "double precision"
  }

  def convertInt(ty: TyInteger): String = ty.getBitSize match {
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
    case t: TyOptional if t.getContent.isDefined => encode(t.getContent.get).map(s => s"$s = NULL")
    case t: TyReal => Some(convertReal(t))
    case t: TyInteger with Extendable if t.getValue(KeyOid).contains(true) => Some("oid")
    case t: TyInteger => Some(convertInt(t))
    case t: TyTimeStamp if t.getValue(KeyHasTimeZone).contains(true) =>
      Some("timestamp with time zone")
    // case TimeStampType(_, false) => "timestamp without time zone"
    case TyTimeStamp() => Some("timestamp")
    case _: TyString => Some("text")
    case _: TyStruct => Some("jsonb")
    case x @ TyEnum(_) if x.getValue(KeySimpleEnum).contains(false) => Some("jsonb")
    case x @ TyEnum(_) if x.getValue(KeyName).isDefined =>
      x.getValue(KeyName)
    case TyEnum(_) => Some("text")
    case TyJsonObject => Some("jsonb")
    case t: TyJsonAny if !t.getValue(KeyIsBinary).contains(false) => Some("jsonb")
    case t: TyJsonAny => Some("json")
    case _: TyUnit => Some("void")
    case _: TyBoolean => Some("boolean")
    case _: TyByteArray => Some("bytea")
    case _: TyInet => Some("inet")
    case _: TyUuid => Some("uuid")
    case _: TyRecord => Some("record")
    case t: TyList if t.getContent.isDefined => encode(t.getContent.get).map(x => s"${x}[]")
    case _ => None
  }

  override def decode(ty: String): Option[TyNode] = {
    ty match {
      case s"$ty[]" => decode(ty).map(x => TyListImpl(Some(x)))
      case "bigint" | "bigserial" => Some(TyIntegerImpl(Some(BitSize.B64), Some(true)))
      case "integer" | "int" | "serial" => Some(TyIntegerImpl(Some(BitSize.B32), Some(true)))
      case "smallint" => Some(TyIntegerImpl(Some(BitSize.B16), Some(true)))
      case "double precision" | "float" => Some(TyFloatImpl(Some(BitSize.B64)))
      case "real" => Some(TyFloatImpl(Some(BitSize.B32)))
      case "decimal" | "numeric" => Some(TyDecimalImpl(None, None))
      case "timestamp" | "timestamp without time zone" =>
        Some(
          TyTimeStamp()
            .setValue(KeyTimeUnit, TimeUnit.MILLISECONDS)
            .setValue(KeyHasTimeZone, false)
        )
      case "timestamp with time zone" =>
        Some(
          TyTimeStamp()
            .setValue(KeyTimeUnit, TimeUnit.MILLISECONDS)
            .setValue(KeyHasTimeZone, true)
        )
      case "text" | "varchar" => Some(TyStringImpl())
      case "jsonb" => Some(TyJsonAny().setValue(KeyIsBinary, true))
      case "json" => Some(TyJsonAny().setValue(KeyIsBinary, false))
      case "void" => Some(TyUnitImpl())
      case "oid" => Some(KeyOid.get)
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
    if (node.getValue(KeyPrimary).contains(true))
      attributes ++= " PRIMARY KEY"
    if (!node.getValue(KeyNullable).contains(true))
      attributes ++= " NOT NULL"
    // TODO auto incr
    SqlField(
      naming.toFieldName(node.name),
      convertType(node.value),
      attributes.toString
    )
  }

}

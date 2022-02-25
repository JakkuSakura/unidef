package unidef.languages.sql

import com.typesafe.scalalogging.Logger
import unidef.languages.common.*
import unidef.languages.sql.{KeyNullable, KeyPrimary}

import java.util.concurrent.TimeUnit
import scala.collection.mutable

case object SqlCommon {
  // multiple rows
  case object KeyRecords extends KeywordBoolean

  case object KeySchema extends KeywordString

  case object KeyOid extends KeywordBoolean {
    def get: TyInteger =
      TyInteger(BitSize.B32, signed = false).setValue(KeyOid, true)
  }

  def convertReal(ty: TyReal): String = ty match {
    case TyDecimal(Some(precision), Some(scale)) => s"decimal($precision, $scale)"
    case TyDecimal(None, None) => s"decimal"
    case TyFloat(BitSize.B32) => "real"
    case TyFloat(BitSize.B64) => "double precision"

  }

  def convertInt(ty: TyInteger): String = ty match {
    case ty if ty.getValue(KeyOid).contains(true) => "oid"
    case _ =>
      ty.bitSize match {
        case BitSize.B16 => "smallint"
        case BitSize.B32 => "integer"
        case BitSize.B64 => "bigint"
        case x => s"integer($x)"

      }
  }

  def convertType(ty: TyNode): String = ty match {
    case t: TyReal => convertReal(t)
    case t: TyInteger => convertInt(t)
    case t: TyTimeStamp if t.getValue(KeyHasTimeZone).contains(true) =>
      "timestamp with time zone"
    // case TimeStampType(_, false) => "timestamp without time zone"
    case TyTimeStamp() => "timestamp"
    case TyString => "text"
    case TyStruct() => "jsonb"
    case x @ TyEnum(_) if x.getValue(KeySimpleEnum).contains(false) => "jsonb"
    case x @ TyEnum(_) if x.getValue(KeyName).isDefined =>
      x.getValue(KeyName).get
    case TyEnum(_) => "text"
    case TyJsonObject => "jsonb"
    case t: TyJsonAny if !t.getValue(KeyIsBinary).contains(false) => "jsonb"
    case t: TyJsonAny => "json"
    case TyUnit => "void"
    case TyNamed(name) => name
    case TyBoolean => "boolean"
    case TyByteArray => "bytea"
    case TyInet => "inet"
    case TyUuid => "uuid"
    case TyRecord => "record"
  }

  def convertTypeFromSql(ty: String): Option[TyNode] = {
    if (ty.endsWith("[]")) {
      return convertTypeFromSql(ty.dropRight(2)).map(TyList.apply)
    }
    ty match {
      case "bigint" | "bigserial" => Some(TyInteger(BitSize.B64))
      case "integer" | "int" | "serial" => Some(TyInteger(BitSize.B32))
      case "smallint" => Some(TyInteger(BitSize.B16))
      case "double precision" => Some(TyFloat(BitSize.B64))
      case "real" | "float" => Some(TyFloat(BitSize.B32))
      case "decimal" | "numeric" => Some(TyDecimal(None, None))
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
      case "text" | "varchar" => Some(TyString)
      case "jsonb" => Some(TyJsonAny().setValue(KeyIsBinary, true))
      case "json" => Some(TyJsonAny().setValue(KeyIsBinary, false))
      case "void" => Some(TyUnit)
      case "oid" => Some(KeyOid.get)
      case "boolean" => Some(TyBoolean)
      case "bytea" => Some(TyByteArray)
      case "inet" => Some(TyInet)
      case "uuid" => Some(TyUuid)
      case "record" => Some(TyRecord)
      case _ => None
    }
  }

  def convertToSqlField(node: TyField, naming: NamingConvention = SqlNamingConvention): SqlField = {
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

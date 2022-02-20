package unidef.languages.sql

import FieldType.{Nullable, PrimaryKey}
import unidef.languages.common._

import java.util.concurrent.TimeUnit
import scala.collection.mutable

case object Oid extends KeywordBoolean {
  def get: TyInteger =
    TyInteger(BitSize.B32, signed = false).setValue(Oid, true)
}

case object SqlCommon {
  case object Records extends KeywordBoolean
  case object Schema extends KeywordString
  case object SimpleEnum extends KeywordBoolean
  def convertReal(ty: TyReal): String = ty match {
    case TyDecimal(precision, scale) => s"decimal($precision, $scale)"
    case TyFloat(BitSize.B32)        => "real"
    case TyFloat(BitSize.B64)        => "double precision"

  }

  def convertInt(ty: TyInteger): String = ty match {
    case ty if ty.getValue(Oid).contains(true) => "oid"
    case _ =>
      ty.bitSize match {
        case BitSize.B16 => "smallint"
        case BitSize.B32 => "integer"
        case BitSize.B64 => "bigint"
        case x           => s"integer($x)"

      }
  }
  def convertType(ty: TyNode): String = ty match {
    case t: TyReal    => convertReal(t)
    case t: TyInteger => convertInt(t)
    case t: TyTimeStamp if t.getValue(HasTimeZone).contains(true) =>
      "timestamp with time zone"
    //case TimeStampType(_, false) => "timestamp without time zone"
    case TyTimeStamp()                                               => "timestamp"
    case TyString                                                    => "text"
    case TyStruct(_)                                                 => "jsonb"
    case x @ TyEnum("", _) if x.getValue(SimpleEnum).contains(false) => "jsonb"
    case TyEnum("", _)                                               => "text"
    case TyEnum(name, _)                                             => name
    case TyJsonObject                                                => "jsonb"
    case TyUnit                                                      => "void"
    case TyNamed(name)                                               => name
    case TyBoolean                                                   => "boolean"
    case TyByteArray                                                 => "bytea"
    case TyInet                                                      => "inet"
    case TyUuid                                                      => "uuid"
    case TyRecord                                                    => "record"
  }

  def convertTypeFromSql(ty: String): TyNode = ty match {
    case "bigint" | "bigserial"       => TyInteger(BitSize.B64)
    case "integer" | "int" | "serial" => TyInteger(BitSize.B32)
    case "smallint"                   => TyInteger(BitSize.B16)
    case "double precision"           => TyFloat(BitSize.B64)
    case "real" | "float"             => TyFloat(BitSize.B32)
    case "decimal" | "numeric"        => TyDecimal(None, None)
    case "timestamp" | "timestamp without time zone" =>
      TyTimeStamp()
        .setValue(HasTimeUnit, TimeUnit.MILLISECONDS)
        .setValue(HasTimeZone, false)
    case "timestamp with time zone" =>
      TyTimeStamp()
        .setValue(HasTimeUnit, TimeUnit.MILLISECONDS)
        .setValue(HasTimeZone, true)
    case "text" | "varchar" => TyString
    case "jsonb"            => TyJsonAny
    case "void"             => TyUnit
    case "oid"              => Oid.get
    case "boolean"          => TyBoolean
    case "bytea"            => TyByteArray
    case "inet"             => TyInet
    case "uuid"             => TyUuid
    case "record"           => TyRecord
    case others             => TyNamed(others)
  }

  def convertToSqlField(node: TyField): SqlField = {
    val attributes = new mutable.StringBuilder()
    if (node.getValue(PrimaryKey).contains(true))
      attributes ++= " PRIMARY KEY"
    if (!node.getValue(Nullable).contains(true))
      attributes ++= " NOT NULL"
    // TODO auto incr
    SqlField(node.name, convertType(node.value), attributes.toString)
  }
}

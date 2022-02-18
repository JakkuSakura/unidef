package unidef.languages.sql

import FieldType.{Nullable, PrimaryKey}
import unidef.languages.common.{
  BitSize,
  KeywordBoolean,
  KeywordString,
  TyDecimal,
  TyEnum,
  TyField,
  TyFloat,
  TyInteger,
  TyJsonAny,
  TyJsonObject,
  TyNamed,
  TyNode,
  TyReal,
  TyString,
  TyStruct,
  TyTimeStamp,
  TyUnit
}

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
    case t: TyReal            => convertReal(t)
    case t: TyInteger         => convertInt(t)
    case TyTimeStamp(_, true) => "timestamp with time zone"
    //case TimeStampType(_, false) => "timestamp without time zone"
    case TyTimeStamp(_, false)                                       => "timestamp"
    case TyString                                                    => "text"
    case TyStruct(_, _, _)                                           => "jsonb"
    case x @ TyEnum("", _) if x.getValue(SimpleEnum).contains(false) => "jsonb"
    case TyEnum("", _)                                               => "text"
    case TyEnum(name, _)                                             => name
    case TyJsonObject                                                => "jsonb"
    case TyUnit                                                      => "void"
    case TyNamed(name)                                               => name
  }

  def convertTypeFromSql(ty: String): TyNode = ty match {
    case "bigint"           => TyInteger(BitSize.B64)
    case "integer"          => TyInteger(BitSize.B32)
    case "smallint"         => TyInteger(BitSize.B16)
    case "double precision" => TyFloat(BitSize.B64)
    case "real"             => TyFloat(BitSize.B32)
    case "decimal"          => ??? // TyDecimal()
    case "timestamp" | "timestamp without time zone" =>
      TyTimeStamp(TimeUnit.MILLISECONDS, timezone = false)
    case "timestamp with time zone" =>
      TyTimeStamp(TimeUnit.MILLISECONDS, timezone = true)
    case "text" | "varchar" => TyString
    case "jsonb"            => TyJsonAny
    case "void"             => TyUnit
    case "oid"              => Oid.get
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

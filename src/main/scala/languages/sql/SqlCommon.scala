package com.jeekrs.unidef
package languages.sql

import languages.common._

import languages.sql.FieldType.{Nullable, PrimaryKey}

case object SqlCommon {
  case object Records extends KeywordBoolean
  case object Schema extends KeywordString
  def convertReal(ty: TyReal): String = ty match {
    case TyDecimal(precision, scale) => s"decimal($precision, $scale)"
    case TyFloat(BitSize.B32)        => "real"
    case TyFloat(BitSize.B64)        => "double precision"
  }
  def convertInt(ty: TyInteger): String = ty.bitSize match {
    case BitSize.B16 => "smallint"
    case BitSize.B32 => "integer"
    case BitSize.B64 => "bigint"
    case x           => s"integer($x)"
  }
  def convertType(ty: TyNode): String = ty match {
    case t: TyReal            => convertReal(t)
    case t: TyInteger         => convertInt(t)
    case TyTimeStamp(_, true) => "timestamp with time zone"
    //case TimeStampType(_, false) => "timestamp without time zone"
    case TyTimeStamp(_, false) => "timestamp"
    case TyString              => "text"
    case TyStruct(_, _, _)     => "jsonb"
    case TyEnum(_, true)       => "text"
    case TyEnum(_, false)      => "jsonb"
    case TyJsonObject          => "jsonb"
  }
  def convertToSqlField(node: TyField): SqlField = {
    val attributes = new StringBuilder()
    if (node.getValue(PrimaryKey).contains(true))
      attributes ++= " PRIMARY KEY"
    if (!node.getValue(Nullable).contains(true))
      attributes ++= " NOT NULL"
    // TODO auto incr
    SqlField(node.name, convertType(node.value), attributes.toString)
  }
}

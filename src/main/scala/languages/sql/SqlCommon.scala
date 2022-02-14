package com.jeekrs.unidef
package languages.sql

import languages.common.{
  BitSize,
  DecimalType,
  EnumType,
  FieldType,
  FloatType,
  IntegerType,
  JsonObjectType,
  RealType,
  StringType,
  StructType,
  TimeStampType,
  TyNode
}
import languages.sql.FieldType.{Nullable, PrimaryKey}
import utils.{ExtKeyBoolean, ExtKeyString}

case object SqlCommon {
  case object Records extends ExtKeyBoolean
  case object Schema extends ExtKeyString
  def convertReal(ty: RealType): String = ty match {
    case DecimalType(precision, scale) => s"decimal($precision, $scale)"
    case FloatType(BitSize.B32)        => "real"
    case FloatType(BitSize.B64)        => "double precision"
  }
  def convertInt(ty: IntegerType): String = ty.bitSize match {
    case BitSize.B16 => "smallint"
    case BitSize.B32 => "integer"
    case BitSize.B64 => "bigint"
    case x           => s"integer($x)"
  }
  def convertType(ty: TyNode): String = ty match {
    case t: RealType            => convertReal(t)
    case t: IntegerType         => convertInt(t)
    case TimeStampType(_, true) => "timestamp with time zone"
    //case TimeStampType(_, false) => "timestamp without time zone"
    case TimeStampType(_, false) => "timestamp"
    case StringType              => "text"
    case StructType(_, _, _)     => "jsonb"
    case EnumType(_, true)       => "text"
    case EnumType(_, false)      => "jsonb"
    case JsonObjectType          => "jsonb"
  }
  def convertToSqlField(node: FieldType): SqlField = {
    val attributes = new StringBuilder()
    if (node.getValue(PrimaryKey).contains(true))
      attributes ++= " PRIMARY KEY"
    if (!node.getValue(Nullable).contains(true))
      attributes ++= " NOT NULL"
    // TODO auto incr
    SqlField(node.name, convertType(node.value), attributes.toString)
  }
}

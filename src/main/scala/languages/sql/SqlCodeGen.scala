package com.jeekrs.unidef
package languages.sql

import languages.common._

import com.jeekrs.unidef.languages.sql.FieldType.{Nullable, PrimaryKey}
import org.apache.velocity.VelocityContext

case class SqlField(name: String, ty: String, attributes: String)
class SqlCodeGen {

  def generateCode(node: AstNode): String = {
    node match {
      case n: ClassDeclNode    => generateTableDdl(n)
      case n: FunctionDeclNode => generateFunctionDdl(n)
    }
  }

  def generateTableDdl(node: ClassDeclNode): String = {
    val context = new VelocityContext()
    context.put("fields", node.fields.map(convertToSqlField))
    CodeGen.render("""
        |CREATE TABLE IF NOT EXIST $name (
        |#foreach($field in $fields)
        |   $field.name $field.ty$field.attributes, 
        |#end
        |);
        |""".stripMargin, context)
  }
  def convertReal(ty: RealType): String = ty match {
    case DecimalType(precision, scale) => s"decimal($precision, $scale)"
    case FloatType(BitSize.B32)        => "real"
    case FloatType(BitSize.B64)        => "double precision"
  }
  def convertInt(ty: IntegerType): String = ty.bitSize match {
    case BitSize.B16 => "smallint"
    case BitSize.B32 => "integer"
    case BitSize.B64 => "bigint"
  }
  def convertType(ty: TyNode): String = ty match {
    case t: RealType            => convertReal(t)
    case t: IntegerType         => convertInt(t)
    case TimeStampType(_, true) => "timestamp"
    case TimeStampType(_, true) => "timestamp without time zone"
    case StringType             => "text"
    case StructType(_, _, _)    => "jsonb"
    case EnumType(_, true)      => "text"
    case EnumType(_, false)     => "jsonb"

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
  def generateFunctionDdl(n: FunctionDeclNode): String = ???
}

package com.jeekrs.unidef
package languages.sql

import languages.common._
import languages.sql.FieldType.{Nullable, PrimaryKey}
import scala.jdk.CollectionConverters._
import org.apache.velocity.VelocityContext

case class SqlField(name: String, ty: String, attributes: String)
case object SqlCodeGen {

  def generateCode(node: AstNode): String = {
    node match {
      case n: ClassDeclNode    => generateTableDdl(n)
      case n: FunctionDeclNode => generateFunctionDdl(n)
    }
  }

  def generateTableDdl(node: ClassDeclNode): String = {
    val context = new VelocityContext()
    context.put("name", node.name.asInstanceOf[LiteralString].value)
    context.put("fields", node.fields.map(convertToSqlField).asJava)
    CodeGen.render(
      """
        |CREATE TABLE IF NOT EXIST $name (
        |#foreach($field in $fields)
        |   $field.name() $field.ty()$field.attributes()#if($foreach.hasNext),#end
        |#end
        |);
        |""".stripMargin,
      context
    )
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
  private val TEMPLATE_GENERATE_FUNCTION_DDL =
    """
                           |CREATE OR REPLACE FUNCTION $name (
                           |#foreach($arg in $args)
                           |  $arg.name() $arg.ty()#if($foreach.hasNext),#end 
                           |#end
                           |)
                           |#if($return_table)
                           |RETURNS TABLE (
                           |#foreach($arg in $return_table)
                           |  $arg.name() $arg.ty()#if($foreach.hasNext),#end
                           |#end
                           |)
                           |#else
                           |RETURNS void
                           |#end
                           |LANGUAGE $language
                           |AS $$
                           |$body
                           |$$;
                           |""".stripMargin
  def generateFunctionDdl(node: FunctionDeclNode): String = {
    val context = new VelocityContext()
    context.put("name", node.name.asInstanceOf[LiteralString].value)
    context.put("args", node.arguments.map(convertToSqlField).asJava)
    context.put("language", node.body.asInstanceOf[RawCodeNode].lang.get)
    context.put("body", node.body.asInstanceOf[RawCodeNode].raw)

    node.returnType match {
      case ClassDeclNode(_, fields, _, _) =>
        context.put("return_table", fields.map(convertToSqlField).asJava)
      case _ =>
    }

    CodeGen.render(TEMPLATE_GENERATE_FUNCTION_DDL, context)
  }
}

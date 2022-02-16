package com.jeekrs.unidef
package languages.sql

import languages.common._
import languages.sql.FieldType.{AutoIncr, Nullable, PrimaryKey}
import languages.sql.SqlCommon.{Records, Schema, convertToSqlField, convertType}
import utils.CodeGen

import scala.jdk.CollectionConverters._

case class SqlField(name: String, ty: String, attributes: String)
case object SqlCodeGen extends KeywordProvider {
  override def keysOnFuncDecl: Seq[Keyword] = Seq(Records, Schema)
  override def keysOnField: Seq[Keyword] = Seq(PrimaryKey, AutoIncr, Nullable)
  private val TEMPLATE_GENERATE_FUNCTION_CALL =
    """
    |#macro(generate_params)#foreach($param in $params)
    |    $param => ${esc.d}$foreach.count#if($foreach.hasNext), #end
    |#end#end
    |#if($records)
    |SELECT * FROM $schema${db_func_name}(
    |#generate_params()
    |);
    |#else
    |SELECT $schema$db_func_name(
    |#generate_params()
    |) AS _value;
    |#end
    |""".stripMargin
  def generateCallFunc(func: AstFunctionDecl): String = {
    val context = CodeGen.createContext

    context.put("name", func.literalName.get)
    context.put("params", func.parameters.map(_.name).asJava)
    context.put("db_func_name", func.literalName.get)
    context.put("schema", func.getValue(Schema).fold("")(x => s"$x."))
    context.put("records", func.getValue(Records).contains(true))

    CodeGen.render(TEMPLATE_GENERATE_FUNCTION_CALL, context)
  }
  private val TEMPLATE_GENERATE_TABLE_DDL: String =
    """
    |CREATE TABLE IF NOT EXIST $schema$name (
    |#foreach($field in $fields)
    |   $field.name() $field.ty()$field.attributes()#if($foreach.hasNext),#end
    |#end
    |);
    |""".stripMargin
  def generateTableDdl(node: AstClassDecl): String = {
    val context = CodeGen.createContext
    context.put("name", node.literalName.get)
    context.put("fields", node.fields.map(convertToSqlField).asJava)
    context.put("schema", node.getValue(Schema).fold("")(x => s"$x."))
    CodeGen.render(TEMPLATE_GENERATE_TABLE_DDL, context)
  }
  private val TEMPLATE_GENERATE_FUNCTION_DDL =
    """
     |CREATE OR REPLACE FUNCTION $schema$name (
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
     |RETURNS $return_type
     |#end
     |LANGUAGE $language
     |AS $$
     |$body
     |$$;
     |""".stripMargin
  def generateFunctionDdl(node: AstFunctionDecl): String = {
    val context = CodeGen.createContext
    context.put("name", node.literalName.get)
    context.put("args", node.parameters.map(convertToSqlField).asJava)
    context.put("language", node.body.asInstanceOf[AstRawCode].lang.get)
    context.put("body", node.body.asInstanceOf[AstRawCode].raw)
    context.put("schema", node.getValue(Schema).fold("")(x => s"$x."))
    node.returnType match {
      case AstClassDecl(_, fields, _, _) =>
        context.put("return_table", fields.map(convertToSqlField).asJava)
      case a => context.put("return_type", convertType(a.inferType))
    }

    CodeGen.render(TEMPLATE_GENERATE_FUNCTION_DDL, context)
  }
}

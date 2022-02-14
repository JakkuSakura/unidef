package com.jeekrs.unidef
package languages.sql

import languages.common._
import languages.sql.FieldType.{AutoIncr, Nullable, PrimaryKey}
import languages.sql.SqlCommon.{Records, convertToSqlField}
import utils.ExtKey

import org.apache.velocity.VelocityContext

import scala.jdk.CollectionConverters._

case class SqlField(name: String, ty: String, attributes: String)
case object SqlCodeGen extends GetExtKeys {
  override def keysOnDecl: List[ExtKey] = List(Records)
  override def keysOnField: List[ExtKey] = List(PrimaryKey, AutoIncr, Nullable)

  def generateTableDdl(node: ClassDeclNode, schema: Option[String]): String = {
    val context = new VelocityContext()
    context.put("name", node.literalName.get)
    context.put("fields", node.fields.map(convertToSqlField).asJava)
    context.put("schema", schema.fold("")(x => s"$x."))
    CodeGen.render(
      """
        |CREATE TABLE IF NOT EXIST $schema$name (
        |#foreach($field in $fields)
        |   $field.name() $field.ty()$field.attributes()#if($foreach.hasNext),#end
        |#end
        |);
        |""".stripMargin,
      context
    )
  }
  private val TEMPLATE_GENERATE_FUNCTION_DDL =
    """
                           |CREATE OR REPLACE FUNCTION $schema$name (
                           |#foreach($arg in $args)
                           |  $arg.name() $arg.ty()#if($foreach.hasNext),#end 
                           |#end
                           |)
                           |#if($records)
                           |RETURNS RECORD
                           |#elseif($return_table)
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
  def generateFunctionDdl(node: FunctionDeclNode,
                          schema: Option[String]): String = {
    val context = new VelocityContext()
    context.put("name", node.literalName.get)
    context.put("args", node.parameters.map(convertToSqlField).asJava)
    context.put("language", node.body.asInstanceOf[RawCodeNode].lang.get)
    context.put("body", node.body.asInstanceOf[RawCodeNode].raw)
    context.put("schema", schema.fold("")(x => s"$x."))

    node.getValue(Records) match {
      case Some(true) => context.put("records", true)
      case _ =>
        node.returnType match {
          case ClassDeclNode(_, fields, _, _) =>
            context.put("return_table", fields.map(convertToSqlField).asJava)
          case _ =>
        }
    }

    CodeGen.render(TEMPLATE_GENERATE_FUNCTION_DDL, context)
  }
}

package unidef.languages.sql

import unidef.languages.common._

import unidef.languages.sql.{KeyAutoIncr, KeyNullable, KeyPrimary}
import unidef.languages.sql.SqlCommon.{KeyRecords, KeySchema, convertToSqlField, convertType}
import unidef.utils.CodeGen

import scala.jdk.CollectionConverters._

class SqlCodeGen(naming: NamingConvention = SqlNamingConvention) extends KeywordProvider {
  override def keysOnFuncDecl: Seq[Keyword] =
    Seq(KeyRecords, KeySchema, KeyBody)

  override def keysOnField: Seq[Keyword] = Seq(KeyPrimary, KeyAutoIncr, KeyNullable)

  private val TEMPLATE_GENERATE_FUNCTION_CALL =
    """
      |#if($percentage)
      |#macro(generate_params)#foreach($param in $params)
      |    $param => %s#if($foreach.hasNext), #end
      |#end#end
      |#else
      |#macro(generate_params)#foreach($param in $params)
      |    $param => ${esc.d}$foreach.count#if($foreach.hasNext), #end
      |#end#end
      |#end
      |#if($table)
      |SELECT * FROM $schema${db_func_name}(
      |#generate_params()
      |);
      |#else
      |SELECT $schema$db_func_name(
      |#generate_params()
      |) AS _value;
      |#end
      |""".stripMargin

  def generateCallFunc(func: AstFunctionDecl, percentage: Boolean = false): String = {
    val context = CodeGen.createContext

    context.put("params", func.parameters.map(_.name).map(naming.toFunctionParameterName).asJava)
    context.put("db_func_name", naming.toFunctionName(func.getName.get))
    context.put("schema", func.getValue(KeySchema).fold("")(x => s"$x."))

    val returnType = func.returnType
    returnType match {
      case TyRecord | TyStruct() =>
        context.put("table", true)
      case _ =>
        context.put("table", false)

    }

    context.put("percentage", percentage)
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

  def generateTableDdl(node: TyClass with HasName with HasFields): String = {
    val context = CodeGen.createContext
    context.put("name", naming.toFunctionName(node.getName.get))
    context.put("fields", node.getFields.get.map(convertToSqlField(_, naming)).asJava)
    context.put("schema", node.getValue(KeySchema).fold("")(x => s"$x."))
    CodeGen.render(TEMPLATE_GENERATE_TABLE_DDL, context)
  }

  private val TEMPLATE_GENERATE_FUNCTION_DDL =
    """
      |CREATE OR REPLACE FUNCTION $schema$name (
      |#foreach($arg in $args)
      |  $arg.name() $arg.ty()#if($foreach.hasNext),#end
      |#end
      |)
      |#if(!$return_type)
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
    context.put("name", naming.toFunctionName(node.getName.get))
    context.put(
      "args",
      node.parameterType
        .asInstanceOf[TyTuple]
        .values
        .map(_.asInstanceOf[TyField])
        .map(convertToSqlField(_, naming))
        .asJava
    )
    context.put(
      "language",
      node
        .getValue(KeyBody)
        .get
        .asInstanceOf[AstRawCode]
        .getValue(KeyLanguage)
        .get
    )
    context.put("body", node.getValue(KeyBody).get.asInstanceOf[AstRawCode].raw)
    context.put("schema", node.getValue(KeySchema).fold("")(x => s"$x."))
    node.returnType match {
      case x: TyStruct if x.getFields.isDefined =>
        context.put(
          "return_table",
          x.getFields.get.map(convertToSqlField(_, naming)).asJava
        )
      case TyStruct() =>
        context.put("return_type", "record")
      case a => context.put("return_type", convertType(a))
    }

    CodeGen.render(TEMPLATE_GENERATE_FUNCTION_DDL, context)
  }

  protected val TEMPLATE_GENERATE_FUNCTION_CONSTANT: String =
    """
      |CREATE OR REPLACE FUNCTION $name (
      |)
      |RETURNS $return_type
      |LANGUAGE SQL
      |AS $$
      |SELECT $value;
      |$$;
      |""".stripMargin

  def generateRawFunction(name: String, ret: TyNode, value: String): String = {
    val context = CodeGen.createContext
    context.put("name", name)
    context.put("return_type", convertType(ret))
    context.put("value", value)
    CodeGen.render(TEMPLATE_GENERATE_FUNCTION_CONSTANT, context)
  }
}

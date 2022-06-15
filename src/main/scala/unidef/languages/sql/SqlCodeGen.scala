package unidef.languages.sql

import unidef.common.{Keyword, KeywordProvider, NamingConvention}
import unidef.common.ty.*
import unidef.common.ast.*
import unidef.languages.sql.SqlCommon.*
import unidef.languages.sql.{KeyAutoIncr, KeyNullable, KeyPrimary}
import unidef.utils.TextTool

class SqlCodeGen(
    naming: NamingConvention = SqlNamingConvention,
    sqlCommon: SqlCommon = SqlCommon()
) extends KeywordProvider {
  override def keysOnFuncDecl: Seq[Keyword] =
    Seq(KeyRecords, KeySchema, KeyBody)

  override def keysOnField: Seq[Keyword] = Seq(KeyPrimary, KeyAutoIncr, KeyNullable)
  def renderFunctionCall(
      schema: String,
      db_func_name: String,
      args: List[String],
      is_table: Boolean,
      quote: String
  ): String = {
    if (is_table) {
      s"SELECT * FROM $schema$db_func_name(\n"
        + args.map(x => s"    $x => $quote").mkString(",\n")
        + ");"
    } else {
      s"SELECT $schema$db_func_name(\n"
        + args.map(x => s"    $x => $quote").mkString(",\n")
        + ") AS AS _value;"
    }
  }
  def generateCallFunc(func: AstFunctionDecl, percentage: Boolean = false): String = {

    val params = func.parameters.map(_.name.get).map(naming.toFunctionParameterName)
    val db_func_name = naming.toFunctionName(func.name)
    val schema = func.getValue(KeySchema).fold("")(x => s"$x.")
    val returnType = func.returnType
    val is_table = returnType match {
      case _: TyRecord | _: TyStruct =>
        true
      case _ =>
        false
    }
    val quote = if (percentage) "%" else "$"

    renderFunctionCall(schema, db_func_name, params, is_table, quote)
  }
  def renderTableDdl(schema: String, name: String, fields: List[SqlField]): String = {
    s"CREATE TABLE IF NOT EXIST $schema$name (\n"
      + fields.map(f => s"    ${f.name} ${f.ty}${f.attributes}").mkString(",\n")
      + "\n);"
  }

  def generateTableDdl(node: AstClassDecl): String = {
    val name = naming.toFunctionName(node.name)
    val fields = node.getFields.map(sqlCommon.convertToSqlField)
    val schema = node.schema.fold("")(x => s"$x.")
    renderTableDdl(schema, name, fields)
  }
  def generateTableDdl(node: TyStruct): String = {
    val name = naming.toFunctionName(node.name.get)
    val fields = node.fields.get.map(sqlCommon.convertToSqlField)
    val schema = node.schema.fold("")(x => s"$x.")
    renderTableDdl(schema, name, fields)
  }
  def renderFunctionDdl(
      schema: String,
      name: String,
      params: List[SqlField],
      returnType: String,
      returnTable: List[SqlField],
      language: String,
      body: String
  ): String =
    s"""CREATE OR REPLACE FUNCTION $schema$name(\n"""
      + params.map(f => s"    ${f.name} ${f.ty}${f.attributes}").mkString(",\n")
      + ")\n"
      + (if (returnType.isEmpty)
           "RETURNS TABLE (\n" + returnTable
             .map(x => s"  ${x.name} ${x.ty}")
             .mkString("\n") + "\n)\n"
         else s"RETURNS $returnType\n")
      + s"LANGUAGE $language\n"
      + s"AS $$\n"
      + TextTool.indent_hard(body, 2)
      + "\n$$;"

  def generateFunctionDdl(node: AstFunctionDecl): String = {
    val name = naming.toFunctionName(node.name)
    val args = node.parameters
      .map(sqlCommon.convertToSqlField)

    val language =
      node
        .getValue(KeyBody)
        .get
        .asInstanceOf[AstRawCode]
        .language
        .get

    val body = node.getValue(KeyBody).get.asInstanceOf[AstRawCode].code
    val schema = node.getValue(KeySchema).fold("")(x => s"$x.")
    var returnTable: List[SqlField] = Nil
    var returnType = ""
    node.returnType match {
      case x: TyStruct if x.fields.isDefined =>
        returnTable = x.fields.get.map(sqlCommon.convertToSqlField)
      case _: TyStruct =>
        returnType = "record"
      case a => returnType = sqlCommon.convertType(a)
    }
    renderFunctionDdl(schema, name, args, returnType, returnTable, language, body)
  }
  def renderFuncionConstant(schema: String, name: String, ty: String, value: String): String = {
    s"CREATE OR REPLACE FUNCTION $schema$name()\n"
      + s"RETURNS $ty\n"
      + s"LANGUAGE SQL\n"
      + "AS $$\n"
      + s"SELECT $value;\n"
      + "$$;"

  }

  def generateRawFunction(name: String, ret: TyNode, value: String): String = {
    renderFuncionConstant(
      "",
      name, //      naming.toFunctionName(name),
      sqlCommon.convertType(ret),
      value
    )
  }
}

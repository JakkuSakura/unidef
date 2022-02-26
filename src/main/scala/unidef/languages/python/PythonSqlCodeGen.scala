package unidef.languages.python

import unidef.languages.common.*
import unidef.languages.sql.{KeyNullable, KeyPrimary}
import unidef.languages.sql.SqlCommon.{KeyRecords, KeySchema}
import unidef.languages.sql.{SqlCodeGen, SqlCommon, SqlField, SqlNamingConvention}
import unidef.utils.{CodeGen, ParseCodeException}

import scala.collection.mutable
import scala.jdk.CollectionConverters.*

private case class PythonField(name: String, orig_name: String, ty: String)
class PythonSqlCodeGen(
    pythonCommon: PythonCommon = PythonCommon(PythonNamingConvention),
    sqlCodeGen: SqlCodeGen = SqlCodeGen(SqlNamingConvention)
) extends KeywordProvider {
  override def keysOnFuncDecl: Seq[Keyword] = List(KeyRecords, KeySchema)
  private def convertToPythonField(
      node: TyField,
      importManager: Option[ImportManager]
  ): PythonField =
    PythonField(
      pythonCommon.naming.toFunctionParameterName(node.name),
      node.name,
      pythonCommon
        .convertType(node.value, importManager)
        .getOrElse(throw ParseCodeException(s"Failed to convert type: ${node.value}"))
    )

  protected val TEMPLATE_DATABASE_CODEGEN: String =
    """
      |@beartype.beartype
      |async def $name(
      |#foreach($param in $params)
      |    $param.name(): $param.ty(),
      |#end
      |) -> Result[$return, int]:
      |    result = await database.invoke('$db_func_name', {
      |        #foreach($param in $params)
      |            '$param.orig_name()': $param.name(),
      |        #end
      |    })
      |    if isinstance(result, Err):
      |       err = result.value
      |       logger.error("Error when executing $name: " + str(err))
      |       return Err(err)
      |    #if($table && $records)
      |    ret = result.value
      |    #elseif($table && !$records)
      |    ret = result.value[0]
      |    #elseif(!$table && $records)
      |    ret = [x["$name"] for x in result.value]
      |    #else
      |    ret = result.value[0]["$name"]
      |    #end
      |    return Ok(cast($return, ret))
      |""".stripMargin

  def generateFuncWrapper(
      func: AstFunctionDecl,
      percentage: Boolean = false,
      importManager: Option[ImportManager] = None
  ): String = {
    val context = CodeGen.createContext
    context.put("name", func.getName.get)
    context.put(
      "params",
      func.parameters.map(convertToPythonField(_, importManager = importManager)).asJava
    )
    context.put(
      "db_func_name",
      func.getValue(KeySchema).map(_ + ".").getOrElse("") + func.getName.get
    )
    context.put("callfunc", sqlCodeGen.generateCallFunc(func, percentage))

    val returnType = func.returnType
    returnType match {
      case TyRecord | TyStruct() =>
        context.put("table", true)
      case _ =>
        context.put("table", false)

    }
    if (func.getValue(KeyRecords).contains(true)) {
      context.put("records", true)
      context.put("return", pythonCommon.convertType(TyList(returnType)).get)
    } else {
      context.put("records", false)
      context.put("return", pythonCommon.convertType(returnType).get)
    }

    context.put("schema", func.getValue(KeySchema).fold("")(x => s"$x."))

    CodeGen.render(TEMPLATE_DATABASE_CODEGEN, context)
  }

}

package unidef.languages.python

import unidef.languages.common._
import unidef.languages.python.PythonCommon.convertType
import unidef.languages.sql.SqlCodeGen
import unidef.languages.sql.SqlCommon.{Records, Schema}
import unidef.utils.CodeGen

import scala.jdk.CollectionConverters._

private case class PythonField(name: String, ty: String)
class PythonSqlCodeGen extends KeywordProvider {
  override def keysOnFuncDecl: Seq[Keyword] = List(Records, Schema)
  private def convertToPythonField(
    node: TyField
  )(implicit resolver: TypeResolver): PythonField =
    PythonField(
      PythonNamingConvention.toFunctionParameterName(node.name),
      convertType(node.value)
    )

  protected val template: String =
    """
      |@beartype.beartype
      |async def $name(
      |#foreach($param in $params)
      |    $param.name(): $param.ty(),
      |#end
      |) -> Result[$return, int]:
      |    result = await database._execute('''##
      |        #indent($callfunc, 8)
      |        ''',
      |        [
      |        #foreach($param in $params)
      |            $param.name(),
      |        #end
      |        ]
      |    )
      |    if isinstance(result, Err):
      |       err = result.value
      |       logger.error("Database when executing $name: " + str(err))
      |       return Err(err)
      |    #if($table && $records)
      |    ret = result.value
      |    #elseif($table && !$records)
      |    ret = result.value[0]
      |    #elseif(!$table && $records)
      |    ret = [x["_value"] for x in result.value]
      |    #else
      |    ret = result.value[0]["_value"]
      |    #end
      |    ret2 = cast($return, ret)
      |    return Ok(ret2)
      |""".stripMargin

  def generateFuncWrapper(func: AstFunctionDecl, percentage: Boolean = false)(
    implicit resolver: TypeResolver
  ): String = {
    val context = CodeGen.createContext
    context.put("name", func.literalName.get.split("\\.").last)
    context.put("params", func.parameters.map(convertToPythonField).asJava)
    context.put("db_func_name", func.literalName.get)
    context.put("callfunc", SqlCodeGen.generateCallFunc(func, percentage))

    val returnType = func.returnType
    returnType match {
      case TyRecord | TyStruct(_) =>
        context.put("table", true)
      case _ =>
        context.put("table", false)

    }
    if (func.getValue(Records).contains(true)) {
      context.put("records", true)
      context.put("return", convertType(TyList(returnType)))
    } else {
      context.put("records", false)
      context.put("return", convertType(returnType))
    }

    context.put("schema", func.getValue(Schema).fold("")(x => s"$x."))

    CodeGen.render(template, context)
  }
}
object PythonSqlCodeGen extends PythonSqlCodeGen

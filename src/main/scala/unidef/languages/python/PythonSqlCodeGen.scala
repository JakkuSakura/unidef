package unidef.languages.python

import PythonCommon.convertType
import unidef.languages.common._
import unidef.languages.sql.SqlCodeGen
import unidef.languages.sql.SqlCommon.{Records, Schema}
import unidef.utils.CodeGen

import scala.jdk.CollectionConverters._

private case class PythonField(name: String, ty: String)
class PythonSqlCodeGen extends KeywordProvider {
  override def keysOnFuncDecl: Seq[Keyword] = List(Records, Schema)
  private def convertToPythonField(node: TyField): PythonField =
    PythonField(node.name, convertType(node.value))

  protected val template: String =
    """
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
      |    if result.error is not None:
      |       logger.error("Database when executing $name: " + str(result.error))
      |       return Err(result.error)
      |    #if($records)
      |    return Ok(result.data)
      |    #else
      |    return Ok(result.data[0]["_value"])
      |    #end
      |""".stripMargin

  def generateFuncWrapper(func: AstFunctionDecl,
                          percentage: Boolean = false): String = {
    val context = CodeGen.createContext
    context.put("name", func.literalName.get.split("\\.").last)
    context.put("params", func.parameters.map(convertToPythonField).asJava)
    context.put("db_func_name", func.literalName.get)
    context.put("callfunc", SqlCodeGen.generateCallFunc(func, percentage))
    val returnType = func.returnType
    context.put(
      "records",
      func.getValue(Records).contains(true) || returnType == TyRecord
    )
    if (func.getValue(Records).contains(true)) {
      context.put("return", convertType(TyList(returnType)))
    } else {
      context.put("return", convertType(returnType))
    }

    context.put("schema", func.getValue(Schema).fold("")(x => s"$x."))

    CodeGen.render(template, context)
  }
}
object PythonSqlCodeGen extends PythonSqlCodeGen

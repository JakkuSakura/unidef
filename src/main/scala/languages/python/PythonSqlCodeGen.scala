package com.jeekrs.unidef
package languages.python

import languages.common._
import languages.python.PythonCommon.convertType
import languages.sql.SqlCodeGen
import languages.sql.SqlCommon.{Records, Schema}
import utils.CodeGen

import org.apache.velocity.VelocityContext

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

  def generateFuncWrapper(func: AstFunctionDecl): String = {
    val context = CodeGen.createContext
    context.put("name", func.literalName.get)
    context.put("params", func.parameters.map(convertToPythonField).asJava)
    context.put("db_func_name", func.literalName.get)
    context.put("callfunc", SqlCodeGen.generateCallFunc(func))
    val returnType = func.returnType.inferType
    context.put("records", func.getValue(Records).contains(true))
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

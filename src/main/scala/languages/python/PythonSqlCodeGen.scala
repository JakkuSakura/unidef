package com.jeekrs.unidef
package languages.python

import languages.common.{CodeGen, FieldType, FunctionDeclNode, LiteralString}
import languages.python.PythonCommon.convertType

import org.apache.velocity.VelocityContext
import scala.jdk.CollectionConverters._

case class PythonField(name: String, ty: String)
object PythonSqlCodeGen {
  def convertToPythonField(node: FieldType): PythonField =
    PythonField(node.name, convertType(node.value))
  private val TEMPLATE_GENERATE_FUNCTION_WRAPPER =
    """
      |async def $name(
      |#foreach($arg in $args)
      |    $arg.name(): $arg.ty(),
      |#end
      |):
      |    return await database.void("$db_func_name",
      |    #foreach($arg in $args)
      |        $arg.name()=$arg.name(),
      |    #end
      |    ) 
      |""".stripMargin
  def generateFuncWrapper(func: FunctionDeclNode): String = {
    val context = new VelocityContext()
    context.put("name", func.name.asInstanceOf[LiteralString].value)
    context.put("args", func.arguments.map(convertToPythonField).asJava)
    context.put("db_func_name", func.name.asInstanceOf[LiteralString].value)

    CodeGen.render(TEMPLATE_GENERATE_FUNCTION_WRAPPER.stripMargin, context)
  }
}

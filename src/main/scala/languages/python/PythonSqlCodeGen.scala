package com.jeekrs.unidef
package languages.python

import languages.common._
import languages.python.PythonCommon.convertType

import org.apache.velocity.VelocityContext

import scala.jdk.CollectionConverters._

private case class PythonField(name: String, ty: String)
object PythonSqlCodeGen {
  private def convertToPythonField(node: FieldType): PythonField =
    PythonField(node.name, convertType(node.value))
  //#foreach($ann in $annotations)
  //@$ann
  //#end

  private val TEMPLATE_GENERATE_FUNCTION_WRAPPER =
    """
      |async def $name(
      |#foreach($arg in $args)
      |    $arg.name(): $arg.ty(),
      |#end
      |) -> $return:
      |    result = await database.$method("$db_func_name",
      |    #foreach($arg in $args)
      |        $arg.name()=$arg.name(),
      |    #end
      |    )
      |    if result.error is None:
      |       raise Exception("Failed to execute database method")
      |    return result.data$post_op
      |""".stripMargin
  def generateFuncWrapper(func: FunctionDeclNode): String = {
    val context = new VelocityContext()
    context.put("name", func.name.asInstanceOf[LiteralString].value)
    context.put("args", func.arguments.map(convertToPythonField).asJava)
    context.put("db_func_name", func.name.asInstanceOf[LiteralString].value)
    context.put("return", convertType(func.returnType.inferType))
    context.put("method", func.returnType match {
      case UnitNode               => "void"
      case _: ClassDeclNode       => "data_table"
      case TypedNode(ListType(_)) => "record_list"
      case TypedNode(SetType(_))  => "record_set"
    })
    context.put("post_op", func.returnType match {
      case _: ClassDeclNode => ".to_dict()"
      case _                => ""
    })
    //context.put(
    //  "annotations",
    //  func
    //    .getValue(Annotations)
    //    .getOrElse(List())
    //    .map(_.value.asInstanceOf[RawCodeNode].raw)
    //    .asJava
    //)

    CodeGen.render(TEMPLATE_GENERATE_FUNCTION_WRAPPER.stripMargin, context)
  }
}

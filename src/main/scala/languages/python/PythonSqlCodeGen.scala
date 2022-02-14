package com.jeekrs.unidef
package languages.python

import languages.common._
import languages.python.PythonCommon.convertType
import languages.sql.SqlCommon.Records
import utils.ExtKey

import org.apache.velocity.VelocityContext

import scala.jdk.CollectionConverters._

private case class PythonField(name: String, ty: String)
case object PythonSqlCodeGen extends GetExtKeys {
  override def keysOnDecl: List[ExtKey] = List(Records)
  private def convertToPythonField(node: FieldType): PythonField =
    PythonField(node.name, convertType(node.value))

  private val TEMPLATE_GENERATE_FUNCTION_WRAPPER =
    """
      |async def $name(
      |#foreach($param in $params)
      |    $param.name(): $param.ty(),
      |#end
      |) -> $return:
      |    result = await database.$method("$db_func_name",
      |    #foreach($param in $params)
      |        $param.name()=$param.name(),
      |    #end
      |    )
      |    if result.error is not None:
      |       raise Exception("Failed to execute database method")
      |    return result.data$post_op
      |""".stripMargin
  def generateFuncWrapper(func: FunctionDeclNode): String = {
    val context = new VelocityContext()
    context.put("name", func.literalName.get)
    context.put("params", func.parameters.map(convertToPythonField).asJava)
    context.put("db_func_name", func.literalName.get)
    val returnType = func.returnType.inferType

    if (func.getValue(Records).contains(true)) {
      context.put("return", convertType(ListType(returnType)))
    } else {
      context.put("return", convertType(returnType))
    }

    context.put("method", func.returnType match {
      case UnitNode                                   => "void"
      case TypedNode(ListType(_))                     => "record_list"
      case _ if func.getValue(Records).contains(true) => "record_list"
      case _: ClassDeclNode                           => "data_table"
      case TypedNode(SetType(_))                      => "record_set"
      case TypedNode(_)                               => "void"
    })
    context.put("post_op", func.returnType match {
      case _: ClassDeclNode => ".to_dict()"
      case _                => ""
    })

    CodeGen.render(TEMPLATE_GENERATE_FUNCTION_WRAPPER.stripMargin, context)
  }
}

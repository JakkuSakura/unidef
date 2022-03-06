package unidef.languages.scala

import unidef.languages.common.*
import unidef.utils.{CodeGen, TypeEncodeException}

import scala.jdk.CollectionConverters.*

class ScalaCodeGen(naming: NamingConvention) {
  val TEMPLATE_METHOD: String =
    """
      |${override}def $name($params): $return = {
      |  $text.indent($body, 2)
      |}
    """.stripMargin
  // TODO func decl without definition
  def generateMethod(method: AstFunctionDecl): String = {
    val context = CodeGen.createContext
    context.put("name", naming.toMethodName(method.getName.get))
    context.put(
      "params",
      method.parameters.map(x => x.name + ": " + ScalaCommon().encode(x).get).mkString(", ")
    )
    context.put("body", method.getBody.get.asInstanceOf[AstRawCode].raw)
    context.put(
      "override",
      if (method.getValue(KeyOverride).getOrElse(false)) {
        "override "
      } else {
        ""
      }
    )
    context.put("return", ScalaCommon().encode(method.returnType).get)
    CodeGen.render(TEMPLATE_METHOD, context)
  }

  val TEMPLATE_CLASS: String =
    """
      |$cls $name#if($hasParams)($params)#end
      |#if($derive) extends
      |#foreach($d in $derive)
      |  $d #if($foreach.hasNext)with#end
      |#end#end
      |{
      |#foreach($m in $methods)
      |  $text.indent($m, 2)
      |#end
      |}
    """.stripMargin
  def generateClass(trt: AstClassDecl): String = {
    val context = CodeGen.createContext
    val cls = trt.getValue(KeyClassType).getOrElse("case class")
    context.put("cls", trt.getValue(KeyClassType).getOrElse("case class"))
    context.put("name", naming.toClassName(trt.getName.get))
    context.put(
      "params",
      trt.fields
        .map(x =>
          x.name + ": " + ScalaCommon()
            .encode(x.value)
            .getOrElse(throw TypeEncodeException("Scala", x))
        )
        .mkString(", ")
    )
    context.put("hasParams", !cls.contains("object"))
    context.put("derive", trt.derived.map(_.name).map(naming.toClassName).asJava)
    context.put(
      "methods",
      trt.methods.map {
        case x: AstFunctionDecl => generateMethod(x)
        case x: AstRawCode => x.raw
      }.asJava
    )
    CodeGen.render(TEMPLATE_CLASS, context)
  }
}

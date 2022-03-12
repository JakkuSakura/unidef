package unidef.languages.scala

import unidef.languages.common.*
import unidef.utils.{CodeGen, TextTool, TypeEncodeException}

import scala.jdk.CollectionConverters.*

class ScalaCodeGen(naming: NamingConvention) {
  val TEMPLATE_METHOD: String =
    """
      |${override}def $name$params: $return#if($body) = {
      |  $text.indent($body, 2)
      |}#end
    """.stripMargin

  def generateMethod(method: AstFunctionDecl): String = {
    val context = CodeGen.createContext
    context.put("name", naming.toMethodName(method.getName.get))
    if (method.parameters.isEmpty && method.getName.get.startsWith("get"))
      context.put("params", "")
    else
      context.put(
        "params",
        "(" + method.parameters
          .map(x => x.name + ": " + ScalaCommon().encode(x).get)
          .mkString(", ") + ")"
      )

    context.put("body", method.getBody.map(_.asInstanceOf[AstRawCode].raw).getOrElse(""))
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
      |$cls $name#if($hasParams)($params)#end #if($derive)extends #foreach($d in $derive)$d #if($foreach.hasNext)with #end#end#end{
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
    context.put(
      "hasParams",
      !cls.contains("object") && !(cls.contains("trait") && trt.fields.isEmpty)
    )
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
  val TEMPLATE_ENUM: String =
    """
      |sealed trait $name
      |object $name {
      |  #foreach($x in $variants)
      |  case object $x extends $name
      |  #end
      |}
    """.stripMargin
  def generateScala2Enum(enm: TyEnum): String = {
    val context = CodeGen.createContext
    context.put("name", TextTool.toPascalCase(enm.getName.get))
    context.put(
      "variants",
      enm.variants.map(x => x.names.head).map(TextTool.toScreamingSnakeCase).asJava
    )
    CodeGen.render(TEMPLATE_ENUM, context)
  }
}

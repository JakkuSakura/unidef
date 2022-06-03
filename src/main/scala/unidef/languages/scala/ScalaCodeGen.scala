package unidef.languages.scala

import unidef.common.NamingConvention
import unidef.common.ty.{TyEnum, TyField}
import unidef.common.ast.{AstClassDecl, AstFunctionDecl, AstRawCode, KeyClassType, KeyOverride}

import unidef.utils.{CodeGen, TextTool, TypeEncodeException}

import scala.jdk.CollectionConverters.*

class ScalaCodeGen(naming: NamingConvention) {
  def renderMethod(
      override_a: String,
      name: String,
      params: String,
      ret: String,
      body: Option[String]
  ): String = {
    // TODO: replace velocity template engine
    ???
  }
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
          .map(x => x.name + ": " + ScalaCommon().encodeOrThrow(x.value, "param"))
          .mkString(", ") + ")"
      )

    context.put("body", method.getBody.map(_.asInstanceOf[AstRawCode].getCode.get).getOrElse(""))
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
    def mapParam(x: TyField): String = {
      val modifier = if (cls == "case class") {
        ""
      } else if (x.mutability.contains(true)) {
        "var "
      } else {
        "val "
      }
      modifier + x.name + ": " + ScalaCommon()
        .encode(x.value)
        .getOrElse(throw TypeEncodeException("Scala", x))
    }

    context.put("cls", cls)
    context.put("name", naming.toClassName(trt.getName.get))
    context.put(
      "params",
      trt.fields.map(mapParam).mkString(", ")
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
        case x: AstRawCode => x.getCode.get
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
  def generateRaw(code: AstRawCode): String = {
    code.getCode.get
  }
}

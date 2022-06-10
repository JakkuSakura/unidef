package unidef.languages.scala

import unidef.common.NamingConvention
import unidef.common.ty.{TyEnum, TyField}
import unidef.common.ast.{AstClassDecl, AstFunctionDecl, AstRawCode, AstValDef, KeyClassType, KeyOverride}
import unidef.utils.{TextTool, TypeEncodeException}

import scala.jdk.CollectionConverters.*

class ScalaCodeGen(naming: NamingConvention) {
  val common = ScalaCommon()
  def renderMethod(
      override_a: String,
      name: String,
      params: String,
      ret: Option[String],
      body: Option[String]
  ): String = {
    s"${override_a}def $name$params" +
      ret.map(r => s": $r").getOrElse("") +
      body.map(b => s" = {\n${TextTool.indent_hard(b, 2)}\n}").getOrElse("")
  }

  def generateMethod(method: AstFunctionDecl): String = {
    val name = naming.toMethodName(method.getName.get)
    val params = if (method.parameters.isEmpty && method.getName.get.startsWith("get"))
      ""
    else
      "(" + method.parameters
          .map(x => x.name + ": " + common.encodeOrThrow(x.value, "param"))
          .mkString(", ") + ")"


    val body = method.getBody.map(_.asInstanceOf[AstRawCode].getCode.get)
    val override_a = if (method.getValue(KeyOverride).getOrElse(false)) {
        "override "
      } else {
        ""
      }

    val ret = common.encode(method.returnType)
    renderMethod(override_a, name, params, ret, body)
  }
  def renderClass(cls: String, name: String, params: Option[List[String]], derive: List[String], methods: List[String]) = {
    val params_a = params.map(x => x.mkString("(", ", ", ")")).getOrElse("")
    val derive_a = if (derive.isEmpty) {
      ""
    } else {
      s" extends ${derive.mkString(" with ")}"
    }
    val body_a = if(methods.isEmpty) "" else "{\n" + TextTool.indent_hard(methods.mkString("\n"), 2) + "\n}"
    s"$cls $name$params_a$derive_a ${body_a}"
  }

  def generateClass(trt: AstClassDecl): String = {
    val cls = trt.getValue(KeyClassType).getOrElse("case class")
    def mapParam(x: AstValDef): String = {
      val modifier = if (cls == "case class") {
        ""
      } else if (x.getMutability.contains(true)) {
        "var "
      } else {
        "val "
      }
      modifier + x.getName.get + ": " + common
        .encode(x.getTy.get)
        .getOrElse(throw TypeEncodeException("Scala", x.getTy.get))
    }

    val name = naming.toClassName(trt.getName.get)
    val params =
      trt.fields.map(mapParam).mkString(", ")
    val hasParams = !cls.contains("object") && !(cls.contains("trait") && trt.fields.isEmpty)

    val derive = trt.derived.map(_.name).map(naming.toClassName)
    val methods = trt.methods.map {
        case x: AstFunctionDecl => generateMethod(x)
        case x: AstRawCode => x.getCode.get
      }
    renderClass(cls, name, if (hasParams) Some(List(params)) else None, derive, methods)
  }
  def renderEnum(name: String, variants: List[String]): String = {
     s"sealed trait $name\n"
    + variants.map(x => s"case object $x extends $name").mkString("\n")
  }

  def generateScala2Enum(enm: TyEnum): String = {
    val name = TextTool.toPascalCase(enm.getName.get)
    val variants =  enm.variants.map(x => x.names.head).map(TextTool.toScreamingSnakeCase)
    renderEnum(name, variants)
  }
  def generateRaw(code: AstRawCode): String = {
    code.getCode.get
  }
}

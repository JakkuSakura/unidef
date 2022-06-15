package unidef.languages.scala

import unidef.common.NamingConvention
import unidef.common.ty.*
import unidef.common.ast.*
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
    val params =
      if (method.parameters.isEmpty && method.getName.get.startsWith("get"))
        ""
      else
        "(" + method.parameters
          .map(x => x.getName.get + ": " + common.encodeOrThrow(x.getValue, "param"))
          .mkString(", ") + ")"

    val body = method.getBody.map(_.asInstanceOf[AstRawCode].getCode)
    val override_a = if (method.getValue(KeyOverride).getOrElse(false)) {
      "override "
    } else {
      ""
    }

    val ret = common.encode(method.returnType)
    renderMethod(override_a, name, params, ret, body)
  }

  def renderClass(
      cls: String,
      name: String,
      params: List[String],
      fields: List[String],
      derive: List[String],
      methods: List[String]
  ) = {
    val params_a = params.mkString("(", ", ", ")")
    val derive_a = if (derive.isEmpty) {
      ""
    } else {
      s" extends ${derive.mkString(" with ")}"
    }
    val body_a =
      if (fields.isEmpty && methods.isEmpty) ""
      else
        "{\n" +
          (fields ++ methods).map(TextTool.indent_hard(_, 2)).mkString("\n") +
          "\n}"

    s"$cls $name$params_a$derive_a $body_a"
  }

  def generateClass(c: AstClassDecl): String = {
    val cls = c.getValue(KeyClassType).getOrElse("case class")

    def mapParam(x: AstValDef): String = {
      val modifier = if (cls == "case class") {
        ""
      } else if (x.getMutability.contains(true)) {
        "var "
      } else {
        "val "
      }
      val default = x.getValue
        .map { case x: AstRawCode =>
          " = " + x.getCode
        }
        .getOrElse("")
      modifier + x.getName + ": " + common
        .encode(x.getTy)
        .getOrElse(throw TypeEncodeException("Scala", x.getTy))
        + default
    }

    val name = naming.toClassName(c.getName.get)
    val params = c.parameters.map(mapParam)
    val fields = c.fields.map(mapParam)
    val derive = c.derived.map(_.name).map(naming.toClassName)
    val methods = c.methods.map {
      case x: AstFunctionDecl => generateMethod(x)
      case x: AstRawCode => x.getCode
    }
    renderClass(cls, name, params, fields, derive, methods)
  }

  def renderEnum(name: String, variants: List[String]): String = {
    s"sealed trait $name\n"
      + variants.map(x => s"case object $x extends $name").mkString("\n")
  }

  def generateScala2Enum(enm: TyEnum): String = {
    val name = TextTool.toPascalCase(enm.getName.get)
    val variants = enm.variants.map(x => x.names.head).map(TextTool.toScreamingSnakeCase)
    renderEnum(name, variants)
  }

  def generateRaw(code: AstRawCode): String = {
    code.getCode
  }

  def generateBuilder(builderName: String, target: String, fields: List[TyField]): AstClassDecl = {
    def expandField(field: TyField): String = {
      val fieldName = naming.toFieldName(field.getName.get)

      fieldName + (field.getValue match {
        case _: TyOptional => ""
        case _ => ".get"
      })
    }
    val buildMethod = AstFunctionDecl(
      name = "build",
      returnType = TyNamed(target),
      parameters = Nil
    ).setValue(
      KeyBody,
      AstRawCodeImpl(s"$target(${fields.map(expandField).mkString(", ")})", None)
    )
    def ensureOptional(x: TyNode): TyNode = {
      x match {
        case x: TyOptional => x
        case x => TyOptionalImpl(x)
      }
    }
    def unwrapOptional(x: TyNode): TyNode = {
      x match {
        case x: TyOptional => x.getContent
        case x => x
      }
    }

    def setFieldMethod(x: TyField): AstFunctionDecl = {
      val fieldName = naming.toFieldName(x.getName.get)
      AstFunctionDecl(
        name = fieldName,
        returnType = TyNamed(builderName),
        parameters = List(
          TyFieldBuilder().name(fieldName).value(unwrapOptional(x.getValue)).build()
        )
      ).setValue(
        KeyBody,
        AstRawCodeImpl(s"this.${fieldName} = Some(${fieldName})\nthis", None)
      )
    }
    AstClassDecl(
      name = builderName,
      parameters = Nil,
      fields = fields.map(x =>
        val fieldName = naming.toFieldName(x.getName.get)
        AstValDefImpl(
          fieldName,
          ensureOptional(x.getValue),
          mutability = Some(true),
          value = Some(AstRawCodeImpl("None", None))
        )
      ),
      methods = fields.map(setFieldMethod) :+ buildMethod
    ).setValue(KeyClassType, "class")
  }

}

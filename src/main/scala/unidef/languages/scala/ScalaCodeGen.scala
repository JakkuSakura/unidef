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
    val name = naming.toMethodName(method.name)
    val params =
      if (method.parameters.isEmpty && method.name.startsWith("get"))
        ""
      else
        "(" + method.parameters
          .map(x => x.name.get + ": " + common.encodeOrThrow(x.value, "param"))
          .mkString(", ") + ")"

    val body = method.getBody.map(_.asInstanceOf[AstRawCode].code)
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
    val cls = c.classType.getOrElse("case class")

    def mapParam(x: AstValDef): String = {
      val modifier = if (cls == "case class") {
        ""
      } else if (x.mutability.contains(true)) {
        "var "
      } else {
        "val "
      }
      val default = x.value
        .map { case x: AstRawCode =>
          " = " + x.code
        }
        .getOrElse("")
      modifier + x.name + ": " + common
        .encode(x.ty)
        .getOrElse(throw TypeEncodeException("Scala", x.ty))
        + default
    }

    val name = naming.toClassName(c.name)
    val params = c.parameters.get.map(mapParam)
    val fields = c.fields.get.map(mapParam)
    val derive = c.derived.getOrElse(Nil).map(_.name).map(naming.toClassName)
    val methods = c.methods.getOrElse(Nil).map {
      case x: AstFunctionDecl => generateMethod(x)
      case x: AstRawCode => x.code
    }
    renderClass(cls, name, params, fields, derive, methods)
  }

  def renderEnum(name: String, variants: List[String]): String = {
    s"sealed trait $name\n"
      + variants.map(x => s"case object $x extends $name").mkString("\n")
  }

  def generateScala2Enum(enm: TyEnum): String = {
    val name = TextTool.toPascalCase(enm.name.get)
    val variants = enm.variants.map(x => x.names.head).map(TextTool.toScreamingSnakeCase)
    renderEnum(name, variants)
  }

  def generateRaw(code: AstRawCode): String = {
    code.code
  }

  def generateBuilder(builderName: String, target: String, fields: List[TyField]): AstClassDecl = {
    def expandField(field: TyField): String = {
      val fieldName = naming.toFieldName(field.name.get)

      fieldName + (field.value match {
        case _: TyOptional => ""
        case _ => ".get"
      })
    }
    val buildMethod = AstFunctionDecl(
      name = "build",
      returnType = TyNamedImpl(target),
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
        case x: TyOptional => x.content
        case x => x
      }
    }

    def setFieldMethod(x: TyField): List[AstFunctionDecl] = {
      val fieldName = naming.toFieldName(x.name.get)
      List(
        AstFunctionDecl(
          name = fieldName,
          returnType = TyNamedImpl(builderName),
          parameters = List(
            TyFieldBuilder().name(fieldName).value(unwrapOptional(x.value)).build()
          )
        ).setValue(
          KeyBody,
          AstRawCodeImpl(s"this.${fieldName} = Some(${fieldName})\nthis", None)
        )
      ) :::
        (if (x.value.isInstanceOf[TyOptional])
           List(
             AstFunctionDecl(
               name = fieldName,
               returnType = TyNamedImpl(builderName),
               parameters = List(
                 TyFieldBuilder().name(fieldName).value(x.value).build()
               )
             ).setValue(
               KeyBody,
               AstRawCodeImpl(s"this.${fieldName} = ${fieldName}\nthis", None)
             )
           )
         else Nil)
    }
    AstClassDeclBuilder()
      .name(builderName)
      .parameters(Nil)
      .fields(fields.map(x =>
        val fieldName = naming.toFieldName(x.name.get)
        AstValDefImpl(
          fieldName,
          ensureOptional(x.value),
          mutability = Some(true),
          value = Some(AstRawCodeImpl("None", None))
        )
      ))
      .methods(fields.flatMap(setFieldMethod) :+ buildMethod)
      .classType("class")
      .build()
  }

}

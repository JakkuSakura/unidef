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
    val params = c.parameters.map(mapParam)
    val fields = c.fields.map(mapParam)
    val derive = c.derived.map(_.name).map(naming.toClassName)
    val methods = c.methods.map {
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

  def generateBuilder(
      builderName: String,
      target: String,
      fields: List[AstValDef]
  ): AstClassDecl = {
    def expandField(field: AstValDef): String = {
      val fieldName = naming.toFieldName(field.name)
      println("expandField " + fieldName + " " + field.ty)
      fieldName + (field.ty match {
        case _: TyOptional => ""
        case _: TyList => ".toList"
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
    def getDefaultValue(x: TyNode): String = {
      x match {
        case _: TyList => "mutable.ArrayBuffer.empty"
        case _ => "None"
      }
    }
    def ensureOptional(x: TyNode): TyNode = {
      x match {
        case x: TyOptional => x
        case x: TyList =>
          val content = common.encodeOrThrow(x.content, "scala")
          TyNamedImpl(s"mutable.ArrayBuffer[${content}]")
        case x => TyOptionalImpl(x)
      }
    }
    def unwrapOptional(x: TyNode): TyNode = {
      x match {
        case x: TyOptional => x.content
        case x => x
      }
    }
    def setFieldUnwrapped(x: AstValDef): List[AstFunctionDecl] = {
      val fieldName = naming.toFieldName(x.name)
      (x.ty match {
        case list: TyList =>
          List(
            AstFunctionDecl(
              name = fieldName,
              returnType = TyNamedImpl(builderName),
              parameters = List(
                TyFieldBuilder().name(fieldName).value(unwrapOptional(x.ty)).build()
              )
            ).setValue(
              KeyBody,
              AstRawCodeImpl(s"this.${fieldName} ++= ${fieldName}\nthis", None)
            )
          )
        case _ =>
          List(
            AstFunctionDecl(
              name = fieldName,
              returnType = TyNamedImpl(builderName),
              parameters = List(
                TyFieldBuilder().name(fieldName).value(unwrapOptional(x.ty)).build()
              )
            ).setValue(
              KeyBody,
              AstRawCodeImpl(s"this.${fieldName} = Some(${fieldName})\nthis", None)
            )
          )
      })
    }
    def setFieldWholeReplace(x: AstValDef): Option[AstFunctionDecl] = {
      val fieldName = naming.toFieldName(x.name)

      (x.ty match {
        case o: TyOptional =>
          Some(
            AstFunctionDecl(
              name = fieldName,
              returnType = TyNamedImpl(builderName),
              parameters = List(
                TyFieldBuilder().name(fieldName).value(o).build()
              )
            ).setValue(
              KeyBody,
              AstRawCodeImpl(s"this.${fieldName} = ${fieldName}\nthis", None)
            )
          )
        case _ => None
      })
    }
    def setFieldAppend(x: AstValDef): List[AstFunctionDecl] = {
      val fieldName = naming.toFieldName(x.name)

      (x.ty match {
        case list: TyList =>
          val fieldNameWithoutS = fieldName.stripSuffix("s")
          List(
            AstFunctionDecl(
              name = fieldNameWithoutS,
              returnType = TyNamedImpl(builderName),
              parameters = List(
                TyFieldBuilder().name(fieldNameWithoutS).value(list.content).build()
              )
            ).setValue(
              KeyBody,
              AstRawCodeImpl(s"this.${fieldName} += ${fieldNameWithoutS}\nthis", None)
            )
          )
        case _ => Nil
      })
    }
    AstClassDeclBuilder()
      .name(builderName)
      .fields(fields.map(x =>
        val fieldName = naming.toFieldName(x.name)
        AstValDefImpl(
          fieldName,
          ensureOptional(x.ty),
          mutability = Some(true),
          value = Some(AstRawCodeImpl(getDefaultValue(x.ty), None))
        )
      ))
      .classType("class")
      .methods(fields.flatMap(setFieldUnwrapped))
      .methods(fields.flatMap(setFieldWholeReplace))
      .methods(fields.flatMap(setFieldAppend))
      .method(buildMethod)
      .build()
  }

}

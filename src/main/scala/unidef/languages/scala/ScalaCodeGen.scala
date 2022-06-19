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
    // TODO: support multiple param lists
    val parameters = Asts.flattenParameters(method.parameters)
    val params =
      if (parameters.isEmpty && method.name.startsWith("get"))
        ""
      else
        "(" + parameters
          .map(x => x.name + ": " + common.encodeOrThrow(x.ty, "param"))
          .mkString(", ") + ")"

    val body = method.body.map(_.asInstanceOf[AstRawCode].code)
    val override_a = if (method.overwrite.getOrElse(false)) {
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
    val params = Asts.flattenParameters(c.parameters).map(mapParam)
    val fields = c.fields.map(mapParam)
    val derive = c.derives.map(_.asInstanceOf[AstIdent]).map(_.name).map(naming.toClassName)
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
        case _: TyOption => ""
        case _: TyList => ".toList"
        case _ => ".get"
      })
    }
    val buildMethod = AstFunctionDeclBuilder()
      .parameters(Asts.parameters(Nil))
      .name("build")
      .returnType(Types.named(target))
      .body(
        AstRawCodeImpl(s"$target(${fields.map(expandField).mkString(", ")})", None)
      )
      .build()
    def getDefaultValue(x: TyNode): String = {
      x match {
        case _: TyList => "mutable.ArrayBuffer.empty"
        case _ => "None"
      }
    }
    def ensureOptional(x: TyNode): TyNode = {
      x match {
        case x: TyOption => x
        case x: TyList =>
          val content = common.encodeOrThrow(x.value, "scala")
          Types.named(s"mutable.ArrayBuffer[${content}]")
        case x => TyOptionImpl(x)
      }
    }
    def unwrapOptional(x: TyNode): TyNode = {
      x match {
        case x: TyOption => x.value
        case x => x
      }
    }
    def setFieldUnwrapped(x: AstValDef): List[AstFunctionDecl] = {
      val fieldName = naming.toFieldName(x.name)
      val builder = AstFunctionDeclBuilder()
        .name(fieldName)
        .returnType(Types.named(builderName))
        .parameters(
          Asts.parameters(
            List(
              AstValDefBuilder().name(fieldName).ty(unwrapOptional(x.ty)).build()
            )
          )
        )

      (x.ty match {
        case list: TyList =>
          List(
            builder
              .body(AstRawCodeImpl(s"this.${fieldName} ++= ${fieldName}\nthis", None))
              .build()
          )
        case _ =>
          List(
            builder
              .body(
                AstRawCodeImpl(s"this.${fieldName} = Some(${fieldName})\nthis", None)
              )
              .build()
          )
      })
    }
    def setFieldWholeReplace(x: AstValDef): Option[AstFunctionDecl] = {
      val fieldName = naming.toFieldName(x.name)

      (x.ty match {
        case o: TyOption =>
          Some(
            AstFunctionDeclBuilder()
              .name(fieldName)
              .returnType(Types.named(builderName))
              .parameters(
                Asts.parameters(
                List(
                  AstValDefBuilder().name(fieldName).ty(o).build()
                )
                )
              )
              .body(
                AstRawCodeImpl(s"this.${fieldName} = ${fieldName}\nthis", None)
              )
              .build()
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
            AstFunctionDeclBuilder()
              .name(fieldNameWithoutS)
              .returnType(Types.named(builderName))
              .parameters(
                Asts.parameters(
                  List(
                    AstValDefBuilder().name(fieldNameWithoutS).ty(list.value).build()
                  )
                )
              )
              .body(
                AstRawCodeImpl(s"this.${fieldName} += ${fieldNameWithoutS}\nthis", None)
              )
              .build()
          )

        case _ => Nil

      })
    }
    AstClassDeclBuilder()
      .name(builderName)
      .parameters(Asts.parameters(Nil))
      .fields(fields.map(x =>
        val fieldName = naming.toFieldName(x.name)
        AstValDefBuilder()
          .name(fieldName)
          .ty(ensureOptional(x.ty))
          .mutability(true)
          .value(AstRawCodeImpl(getDefaultValue(x.ty), None))
          .build()
      ))
      .classType("class")
      .methods(fields.flatMap(setFieldUnwrapped))
      .methods(fields.flatMap(setFieldWholeReplace))
      .methods(fields.flatMap(setFieldAppend))
      .method(buildMethod)
      .build()
  }

}

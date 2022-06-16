package unidef.languages.schema

import com.typesafe.scalalogging.Logger
import io.circe.yaml.parser
import io.circe.{Json, JsonNumber, JsonObject}
import unidef.common.{NoopNamingConvention, ast}
import unidef.languages.javascript.{JsonSchemaCommon, JsonSchemaParser}
import unidef.languages.scala.*
import unidef.utils.FileUtils.readFile
import unidef.utils.{ParseCodeException, TextTool, TypeDecodeException, TypeEncodeException}
import unidef.common.ty.*
import unidef.common.ast.*

import java.io.PrintWriter
import scala.collection.mutable

case class TyNodeCodeGen() {
  val logger: Logger = Logger[this.type]

  def collectFields(ty: Type): Set[AstValDef] = {
    ty.fields.map(_.build()).toSet
  }
  def collectFields(types: List[Type]): Set[AstValDef] = {
    types.flatMap(collectFields).toSet
  }
  def scalaField(
      name: String,
      derive: String,
      methods: List[String],
      classType: Option[String]
  ): AstClassDecl = {
    AstClassDeclBuilder()
      .name(name)
      .methods(methods.map(x => AstRawCodeImpl(x, None)))
      .derived(
        List(AstClassIdent(derive))
      )
      .classType(classType)
      .build()
  }
  def tryWrapValue(x: AstValDef): TyNode = x.ty
  def generateScalaHasTrait(field: AstValDef): AstClassDecl = {
    val traitName = "Has" + TextTool.toPascalCase(field.name)
    val scalaCommon = ScalaCommon()
    val valueType =
      scalaCommon.encodeOrThrow(tryWrapValue(field), "Scala")

    scalaField(
      traitName,
      "TyNode",
      List(s"def ${TextTool.toCamelCase(field.name)}: ${valueType}"),
      Some("trait")
    )
  }
  def generateScalaCompoundTrait(ty: Type): AstClassDecl = {
    val scalaCommon = ScalaCommon()
    val fields = ty.fields.map(_.build()).toList

    AstClassDeclBuilder()
      .name("Ty" + TextTool.toPascalCase(ty.name))
      .methods(
        fields
          .map(field =>
            val valueType =
              scalaCommon.encodeOrThrow(tryWrapValue(field), "Scala")
            AstRawCodeImpl(s"def ${TextTool.toCamelCase(field.name)}: ${valueType}", None)
          )
          .toList
      )
      .derived(
        List(AstClassIdent("TyNode"))
          :::
            ty.equivalent
              .flatMap {
                case x: TyNamed => Some("Ty" + TextTool.toPascalCase(x.ref))
                case _ => None // TODO: support other IS
              }
              .map(x => AstClassIdent(x))
              .toList
            :::
            fields
              .map(x => x.name -> x.value)
              .map((k, v) => "Has" + TextTool.toPascalCase(k))
              .map(x => AstClassIdent(x))
              .toList
      )
      .classType("trait")
      .build()
  }
  def generateScalaBuilder(ty: Type): AstClassDecl = {
    val fields = ty.fields.map(_.build()).toList
    val codegen = ScalaCodeGen(ScalaNamingConvention)
    codegen.generateBuilder(
      "Ty" + TextTool.toPascalCase(ty.name) + "Builder",
      "Ty" + TextTool.toPascalCase(ty.name) + "Impl",
      fields
    )
  }
  def generateScalaCaseClass(ty: Type): AstClassDecl = {
    val fields = ty.fields.map(_.build()).toList

    AstClassDeclBuilder()
      .name(
        "Ty" + TextTool.toPascalCase(ty.name) + "Impl"
      )
      .parameters(fields)
      .derived(
        List(AstClassIdent("Ty" + TextTool.toPascalCase(ty.name)))
      )
      .classType("case class")
      .build()
  }
}
object TyNodeCodeGen {
  def getTypes: Map[String, Type] =
    Seq(
      Type("string"),
      Type("field")
        .field("name", TyStringImpl())
        .field("value", TyNode, required = true)
        .field("mutability", TyBooleanImpl()),
      Type("list")
        .field("value", TyNode, required = true),
      Type("variant")
        .field("names", TyListImpl(TyStringImpl()), required = true)
        .field("code", TyIntegerBuilder().build()),
      Type("enum")
        .field("variants", TyListImpl(TyNamedImpl("TyVariant")), required = true)
        .field("simple_enum", TyBooleanImpl())
        .field("name", TyStringImpl())
        .field("value", TyNode, required = true)
        .field("schema", TyStringImpl()),
      Type("tuple")
        .field("values", TyListImpl(TyNode), required = true),
      Type("optional")
        .field("value", TyNode, required = true),
      Type("result")
        .field("ok", TyNode, required = true)
        .field("err", TyNode, required = true),
      Type("numeric"),
      Type("integer")
        .field("bit_size", TyNamedImpl("BitSize"))
        .field("sized", TyBooleanImpl())
        .is(TyNamedImpl("numeric")),
      Type("real")
        .is(TyNamedImpl("numeric")),
      Type("decimal")
        .field("precision", TyIntegerImpl(None, None))
        .field("scale", TyIntegerImpl(None, None))
        .is(TyNamedImpl("real")),
      Type("float")
        .field("bit_size", TyNamedImpl("BitSize"))
        .is(TyNamedImpl("real")),
      Type("class"),
      Type("struct")
        .field("name", TyStringImpl())
        .field("fields", TyListImpl(TyNamedImpl("TyField")))
        .field("derives", TyListImpl(TyStringImpl()))
        .field("attributes", TyListImpl(TyStringImpl()))
        .field("dataframe", TyBooleanImpl())
        .field("schema", TyStringImpl())
        .is(TyNamedImpl("class"))
        .setCommentable(true),
      Type("object"),
      Type("map")
        .field("key", TyNode, required = true)
        .field("value", TyNode, required = true),
      Type("set")
        .field("value", TyNode, required = true),
      Type("set")
        .field("value", TyNode, required = true)
        .is(TyIntegerImpl(Some(BitSize.B8), Some(false))),
      Type("byte_array")
        .is(TyListImpl(TyIntegerImpl(Some(BitSize.B8), Some(false)))),
      Type("boolean"),
      Type("record"),
      Type("null"),
      Type("char"),
      Type("any"),
      Type("unit"),
      Type("nothing"),
      Type("undefined"),
      Type("inet"),
      Type("uuid"),
      Type("union")
        .field("values", TyListImpl(TyNode), required = true),
      Type("date_time")
        .field("timezone", TyNamedImpl("java.util.TimeZone")),
      Type("time_stamp")
        .field("has_time_zone", TyBooleanImpl())
        .field("time_unit", TyNamedImpl("java.util.concurrent.TimeUnit")),
      Type("named")
        .field("ref", TyStringImpl(), required = true),
      Type("type_var")
        .field("name", TyStringImpl()),
      Type("key_value")
        .field("key", TyNode, required = true)
        .field("value", TyNode, required = true),
      Type("json"),
      Type("json_any")
        .field("is_binary", TyBooleanImpl(), required = true),
      Type("json_object")
        .field("is_binary", TyBooleanImpl(), required = true)
    )
      .map(x => x.name -> x)
      .toMap

  def main(args: Array[String]): Unit = {
    val types = getTypes.values.toList

    val parser = TyNodeCodeGen()

    println("Parsed types")
    println(types.mkString("\n"))

    val fields = parser.collectFields(types)
    println("Parsed fields")
    println(fields.mkString("\n"))

    val hasTraits = fields.map(parser.generateScalaHasTrait)
    println("Generated has traits")
    println(hasTraits.mkString("\n"))
    val caseClasses = types.map(parser.generateScalaCaseClass)
    println("Generated case classes")
    println(caseClasses.mkString("\n"))
    val compoundTraits = types.map(parser.generateScalaCompoundTrait)
    println("Generated compound traits")
    println(compoundTraits.mkString("\n"))
    val builders = types.map(parser.generateScalaBuilder)
    val scalaCodegen = ScalaCodeGen(NoopNamingConvention)
    val scalaCode =
      (
        hasTraits.map(scalaCodegen.generateClass).toList
          ::: caseClasses.map(scalaCodegen.generateClass)
          ::: compoundTraits.map(scalaCodegen.generateClass)
          ::: builders.map(scalaCodegen.generateClass)
      ).mkString("\n")

    println(scalaCode)
    val writer = new PrintWriter("target/TyNodeGen.scala")
    writer.println("""
                     |package unidef.common.ty
                     |import scala.collection.mutable
                     |
                     |""".trim.stripMargin)
    writer.write(scalaCode)
    writer.close()

  }
}

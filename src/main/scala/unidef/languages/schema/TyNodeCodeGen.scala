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
      .parameters(Asts.parameters(Nil))
      .methods(methods.map(x => AstRawCodeImpl(x, None)))
      .derive(
        AstIdentImpl(derive)
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
      .parameters(Asts.parameters(Nil))
      .methods(
        fields
          .map(field =>
            val valueType =
              scalaCommon.encodeOrThrow(tryWrapValue(field), "Scala")
            AstRawCodeImpl(s"def ${TextTool.toCamelCase(field.name)}: ${valueType}", None)
          )
          .toList
      )
      .derive(AstIdentImpl("TyNode"))
      . derives(
        ty.equivalent
          .flatMap {
            case x: TyNamed => Some("Ty" + TextTool.toPascalCase(x.ref))
            case _ => None // TODO: support other IS
          }
          .map(x => AstIdentImpl(x))
          .toList
      )
      . derives(
        fields
          .map(x => x.name -> x.value)
          .map((k, v) => "Has" + TextTool.toPascalCase(k))
          .map(x => AstIdentImpl(x))
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
      .parameters(Asts.parameters(fields))
      .derive(
        AstIdentImpl("Ty" + TextTool.toPascalCase(ty.name))
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
        .field("name", Types.string())
        .field("value", TyNode, required = true)
        .field("mutability", Types.bool()),
      Type("list")
        .field("value", TyNode, required = true),
      Type("variant")
        .field("names", Types.list(Types.string()), required = true)
        .field("code", TyIntegerBuilder().build()),
      Type("enum")
        .field("variants", Types.list(Types.named("TyVariant")), required = true)
        .field("simple_enum", Types.bool())
        .field("name", Types.string())
        .field("value", TyNode, required = true)
        .field("schema", Types.string()),
      Type("tuple")
        .field("values", Types.list(TyNode), required = true),
      Type("option")
        .field("value", TyNode, required = true),
      Type("result")
        .field("ok", TyNode, required = true)
        .field("err", TyNode, required = true),
      Type("numeric"),
      Type("integer")
        .field("bit_size", Types.named("BitSize"))
        .field("signed", Types.bool())
        .is(Types.named("numeric")),
      Type("oid"), //        .is(Types.named("i32")),
      Type("real")
        .is(Types.named("numeric")),
      Type("decimal")
        .field("precision", TyIntegerImpl(None, None))
        .field("scale", TyIntegerImpl(None, None))
        .is(Types.named("real")),
      Type("float")
        .field("bit_size", Types.named("BitSize"))
        .is(Types.named("real")),
      Type("class"),
      Type("struct")
        .field("name", Types.string())
        .field("fields", Types.list(Types.named("TyField")))
        .field("derives", Types.list(Types.string()))
        .field("attributes", Types.list(Types.string()))
        .field("dataframe", Types.bool())
        .field("schema", Types.string())
        .is(Types.named("class"))
        .setCommentable(true),
      Type("object"),
      Type("map")
        .field("key", TyNode, required = true)
        .field("value", TyNode, required = true),
      Type("set")
        .field("value", TyNode, required = true),
      Type("byte_array")
        .is(Types.list(TyIntegerImpl(Some(BitSize.B8), Some(false)))),
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
        .field("values", Types.list(TyNode), required = true),
      Type("date_time")
        .field("timezone", Types.named("java.util.TimeZone")),
      Type("time_stamp")
        .field("has_time_zone", Types.bool())
        .field("time_unit", Types.named("java.util.concurrent.TimeUnit")),
      Type("named")
        .field("ref", Types.string(), required = true)
        .field("preserve_case", Types.bool()),
      Type("type_var")
        .field("name", Types.string()),
      Type("key_value")
        .field("key", TyNode, required = true)
        .field("value", TyNode, required = true),
      Type("json"),
      Type("json_any")
        .field("is_binary", Types.bool(), required = true),
      Type("json_object")
        .field("is_binary", Types.bool(), required = true),
      Type("unknown"),
      Type("reference")
        .field("referee", TyNode, required = true)
        .field("lifetime", Types.named("LifeTime")),
      Type("pointer")
        .field("pointed", TyNode, required = true)
        .field("mutable", Types.bool())
        .field("smart", Types.bool()),
      Type("seq")
        .field("value", TyNode, required = true),
      Type("char")
        .field("bit_size", Types.named("BitSize"))
        .field("charset", Types.string()),
      Type("byte")
        .field("signed", Types.bool()),
      Type("select")
        .field("value", TyNode, required = true)
        .field("symbol", Types.string(), required = true),
      Type("ident")
        .field("name", Types.string()),
      Type("apply")
        .field("applicant", Types.string(), required = true)
        .field("arguments", Types.list(Types.bool()), required = true)
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

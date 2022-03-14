package unidef.languages.schema

import com.typesafe.scalalogging.Logger
import io.circe.yaml.parser
import io.circe.{Json, JsonNumber, JsonObject}
import unidef.languages.common.*
import unidef.languages.javascript.{JsonSchemaCommon, JsonSchemaParser}
import unidef.languages.scala.{ScalaCodeGen, ScalaCommon}
import unidef.utils.FileUtils.readFile
import unidef.utils.{ParseCodeException, TextTool, TypeDecodeException, TypeEncodeException}

import java.io.PrintWriter
import scala.collection.mutable

case class ScalaSchemaParser() {
  val logger: Logger = Logger[this.type]
  val common = JsonSchemaCommon(true)

  def collectFields(ty: TypeBuilder, extra: Seq[String]): Set[TyField] = {
    ty.fields
      .map((k, v) => TyField(k, v))
      .toSet
  }
  def collectFields(types: Seq[TypeBuilder], extra: Seq[String]): Set[TyField] = {
    types
      .flatMap(x => collectFields(x, extra))
      .toSet
  }
  def scalaField(name: String, derive: String, methods: Seq[String]): AstClassDecl = {
    AstClassDecl(
      AstLiteralString(name),
      Nil,
      methods.map(AstRawCode.apply),
      Seq(AstClassIdent(derive))
    )
  }
  def generateScalaKeyObject(field: TyField): AstClassDecl = {
    val traitName = "Key" + field.name.capitalize
    val cls = field.value match {
      case _: TyInteger =>
        scalaField(traitName, "KeywordInt", Nil)
      case TyString => scalaField(traitName, "KeywordString", Nil)
      case TyBoolean => scalaField(traitName, "KeywordBoolean", Nil)
      case _ =>
        val scalaCommon = ScalaCommon()
        val valueType =
          scalaCommon.encodeOrThrow(field.value, "scala")
        scalaField(traitName, "Keyword", Seq(s"override type V = ${valueType}"))
    }
    cls.setValue(KeyClassType, "case object")
  }

  def generateScalaHasTrait(field: TyField): AstClassDecl = {
    val traitName = "Has" + TextTool.toPascalCase(field.name)
    val scalaCommon = ScalaCommon()
    val valueType =
      scalaCommon.encodeOrThrow(field.value, "Scala")

    scalaField(
      traitName,
      "TyNode",
      Seq(s"def get${TextTool.toPascalCase(field.name)}: Option[${valueType}]")
    ).setValue(KeyClassType, "trait")
  }
  def generateScalaCompoundTrait(ty: TypeBuilder, extra: Seq[String]): AstClassDecl = {
    val scalaCommon = ScalaCommon()
    val fields = collectFields(ty, extra)

    AstClassDecl(
      AstLiteralString("Ty" + TextTool.toPascalCase(ty.name)),
      Nil,
      fields.toSeq
        .map(x => x.name -> x.value)
        .map((k, v) =>
          AstFunctionDecl(
            AstLiteralString("get" + TextTool.toPascalCase(k)),
            Nil,
            TyOptional(v)
          )
        ),
      Seq(AstClassIdent("TyNode"))
        ++
          fields
            .map(x => x.name -> x.value)
            .map((k, v) => "Has" + TextTool.toPascalCase(k))
            .map(x => AstClassIdent(x))
            .toSeq
    ).setValue(KeyClassType, "trait")
  }

  def generateScalaCaseClass(ty: TypeBuilder, extra: Seq[String]): AstClassDecl = {
    val scalaCommon = ScalaCommon()
    val fields = collectFields(ty, extra)

    AstClassDecl(
      AstLiteralString("Ty" + TextTool.toPascalCase(ty.name) + "Impl"),
      fields.map(x => TyField(x.name, TyOptional(x.value))).toSeq,
      fields.toSeq
        .map(x => x.name -> x.value)
        .map((k, v) =>
          AstFunctionDecl(
            AstLiteralString("get" + TextTool.toPascalCase(k)),
            Nil,
            TyOptional(v)
          ).setValue(KeyBody, AstRawCode(s"${k}"))
            .setValue(KeyOverride, true)
        ),
      Seq(AstClassIdent("Extendable"), AstClassIdent("Ty" + TextTool.toPascalCase(ty.name)))
    )
  }
}
object ScalaSchemaParser {
  def getTypes: Map[String, TypeBuilder] =
    Map(
      "list" -> TypeBuilder("list")
        .field("content", TyNode),
      "string" -> TypeBuilder("string"),
      "enum" -> TypeBuilder("enum")
        .field("variants", TyList(TyString)),
      "tuple" -> TypeBuilder("tuple")
        .field("values", TyList(TyNode)),
      "option" -> TypeBuilder("optional")
        .field("content", TyNode),
      "result" -> TypeBuilder("result")
        .field("ok", TyNode)
        .field("err", TyNode),
      "numeric" -> TypeBuilder("numeric"),
      "integer" -> TypeBuilder("integer")
        .field("bit_size", TyNamed("bit_size"))
        .field("sized", TyBoolean)
        .is(TyNamed("numeric")),
      "real" -> TypeBuilder("real")
        .is(TyNamed("numeric")),
      "decimal" -> TypeBuilder("decimal")
        .field("precision", TyInteger())
        .field("scale", TyInteger())
        .is(TyNamed("real")),
      "float" -> TypeBuilder("float")
        .field("bis_size", TyNamed("bit_size"))
        .is(TyNamed("real")),
      "class" -> TypeBuilder("class"),
      "struct" -> TypeBuilder("struct")
        .field("fields", TyList(TyNode))
        .field("derives", TyList(TyString))
        .field("attributes", TyList(TyString))
        .is(TyNamed("class")),
      "object" -> TypeBuilder("object"),
      "dict" -> TypeBuilder("map")
        .field("key", TyNode)
        .field("value", TyNode),
      "set" -> TypeBuilder("set")
        .field("content", TyNode),
      "byte" -> TypeBuilder("set")
        .field("content", TyNode)
        .is(TyInteger(BitSize.B8, false)),
      "byte_array" -> TypeBuilder("byte_array")
        .is(TyList(TyInteger(BitSize.B8, false)))
    )

  def main(args: Array[String]): Unit = {
    val types = getTypes.values

    val parser = ScalaSchemaParser()
    val extra = mutable.ArrayBuffer[String]()

    println("Parsed types")
    println(types.mkString("\n"))

    val fields = parser.collectFields(types.toSeq, extra.toSeq)
    println("Parsed fields")
    println(fields.mkString("\n"))
    val keyObjects = fields.map(parser.generateScalaKeyObject)
    println("Generated key objects")
    println(keyObjects.mkString("\n"))
    val hasTraits = fields.map(parser.generateScalaHasTrait)
    println("Generated has traits")
    println(hasTraits.mkString("\n"))
    val caseClasses = types.map(parser.generateScalaCaseClass(_, extra.toSeq))
    println("Generated case classes")
    println(caseClasses.mkString("\n"))
    val compoundTraits = types.map(parser.generateScalaCompoundTrait(_, extra.toSeq))
    println("Generated compound traits")
    println(compoundTraits.mkString("\n"))

    val scalaCodegen = ScalaCodeGen(NoopNamingConvention)
    val scalaCode =
      (keyObjects.map(scalaCodegen.generateClass)
        ++ hasTraits.map(scalaCodegen.generateClass)
        ++ caseClasses.map(scalaCodegen.generateClass)
        ++ compoundTraits.map(scalaCodegen.generateClass)).mkString("\n")

    println(scalaCode)
    val writer = new PrintWriter("target/TyNodeGen.scala")
    writer.println("""
                     |package unidef.languages.common
                     |
                     |""".trim.stripMargin)
    writer.write(scalaCode)
    writer.close()

  }
}

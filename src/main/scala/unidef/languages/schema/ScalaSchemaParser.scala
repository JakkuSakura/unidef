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

  def collectFields(ty: Type, extra: Seq[String]): Set[TyField] = {
    ty.fields.toSet
  }
  def collectFields(types: Seq[Type], extra: Seq[String]): Set[TyField] = {
    types
      .flatMap(x => collectFields(x, extra))
      .toSet
  }
  def scalaField(name: String, derive: String, methods: Seq[String]): AstClassDecl = {
    AstClassDecl(
      AstLiteralString(name),
      Nil,
      methods.map(AstRawCode.apply).toList,
      List(AstClassIdent(derive))
    )
  }
  def generateScalaKeyObject(field: TyField): AstClassDecl = {
    val traitName = "Key" + TextTool.toPascalCase(field.name)
    val cls = field.value match {
      case _: TyInteger =>
        scalaField(traitName, "KeywordInt", Nil)
      case _: TyString => scalaField(traitName, "KeywordString", Nil)
      case _: TyBoolean => scalaField(traitName, "KeywordBoolean", Nil)
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
  def generateScalaCompoundTrait(ty: Type, extra: Seq[String]): AstClassDecl = {
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
            TyOptionalImpl(Some(v))
          )
        ).toList,
      List(AstClassIdent("TyNode"))
        ++
          ty.equivalent
            .flatMap {
              case TyNamed(x) => Some("Ty" + TextTool.toPascalCase(x))
              case _ => None // TODO: support other IS
            }
            .map(x => AstClassIdent(x))
            .toList
          ++
          fields
            .map(x => x.name -> x.value)
            .map((k, v) => "Has" + TextTool.toPascalCase(k))
            .map(x => AstClassIdent(x))
            .toList
    ).setValue(KeyClassType, "trait")
  }

  def generateScalaCaseClass(ty: Type, extra: Seq[String]): AstClassDecl = {
    val fields = collectFields(ty, extra)

    AstClassDecl(
      AstLiteralString("Ty" + TextTool.toPascalCase(ty.name) + "Impl"),
      fields.map(x => TyField(x.name, TyOptionalImpl(Some(x.value)))).toList,
      fields.toSeq
        .map(x => x.name -> x.value)
        .map((k, v) =>
          AstFunctionDecl(
            AstLiteralString("get" + TextTool.toPascalCase(k)),
            Nil,
            TyOptionalImpl(Some(v))
          ).setValue(KeyBody, AstRawCode(s"${k}"))
            .setValue(KeyOverride, true)
        ).toList,
      List(AstClassIdent("Extendable"), AstClassIdent("Ty" + TextTool.toPascalCase(ty.name)))
    )
  }
}
object ScalaSchemaParser {
  def getTypes: Map[String, Type] =
    Seq(
      Type("string"),
      Type("field")
        .field("name", TyStringImpl())
        .field("value", TyNode),
//      Type("list")
//        .field("content", TyNode),
//      Type("enum")
//        .field("variants", TyListImpl(Some(TyStringImpl()))),
      Type("tuple")
        .field("values", TyListImpl(Some(TyNode))),
      Type("optional")
        .field("content", TyNode),
      Type("result")
        .field("ok", TyNode)
        .field("err", TyNode),
      Type("numeric"),
      Type("integer")
        .field("bit_size", TyNamed("bit_size"))
        .field("sized", TyBooleanImpl())
        .is(TyNamed("numeric")),
      Type("real")
        .is(TyNamed("numeric")),
      Type("decimal")
        .field("precision", TyIntegerImpl(None, None))
        .field("scale", TyIntegerImpl(None, None))
        .is(TyNamed("real")),
      Type("float")
        .field("bit_size", TyNamed("bit_size"))
        .is(TyNamed("real")),
      Type("class"),
      Type("struct")
        .field("name", TyStringImpl())
        .field("fields", TyListImpl(Some(TyNamed("TyField"))))
        .field("derives", TyListImpl(Some(TyStringImpl())))
        .field("attributes", TyListImpl(Some(TyStringImpl())))
        .is(TyNamed("class")),
      Type("object"),
      Type("map")
        .field("key", TyNode)
        .field("value", TyNode),
      Type("set")
        .field("content", TyNode),
      Type("set")
        .field("content", TyNode)
        .is(TyIntegerImpl(Some(BitSize.B8), Some(false))),
      Type("byte_array")
        .is(TyListImpl(Some(TyIntegerImpl(Some(BitSize.B8), Some(false))))),
      Type("boolean"),
      Type("record"),
      Type("null"),
      Type("char"),
      Type("any"),
      Type("unit"),
      Type("nothing"),
      Type("undefined"),
      Type("inet"),
      Type("uuid")
    ).map(x => x.name -> x).toMap

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

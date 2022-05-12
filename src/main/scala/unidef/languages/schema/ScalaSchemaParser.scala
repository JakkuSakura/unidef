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

  def collectFields(ty: Type, extra: List[String]): Set[TyField] = {
    ty.fields.toSet
  }
  def collectFields(types: List[Type], extra: List[String]): Set[TyField] = {
    types
      .flatMap(x => collectFields(x, extra))
      .toSet
  }
  def scalaField(name: String, derive: String, methods: List[String]): AstClassDecl = {
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
        scalaField(traitName, "Keyword", List(s"override type V = ${valueType}"))
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
      List(s"def get${TextTool.toPascalCase(field.name)}: Option[${valueType}]")
    ).setValue(KeyClassType, "trait")
  }
  def generateScalaCompoundTrait(ty: Type, extra: List[String]): AstClassDecl = {
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
        )
        .toList,
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

  def generateScalaCaseClass(ty: Type, extra: List[String]): AstClassDecl = {
    val fields = collectFields(ty, extra) ++ (if (ty.commentable) List(TyField("comment", TyStringImpl(), Some(true))) else Nil)

    AstClassDecl(
      AstLiteralString("Ty" + TextTool.toPascalCase(ty.name) + "Impl"),
      fields.map(x => TyField(x.name, TyOptionalImpl(Some(x.value)))).toList,
      fields
        .map(x =>
          AstFunctionDecl(
            AstLiteralString("get" + TextTool.toPascalCase(x.name)),
            Nil,
            TyOptionalImpl(Some(x.value))
          ).setValue(KeyBody, AstRawCode(x.name))
            .setValue(KeyOverride, true)
        )
        .toList
      ++ (if (ty.commentable) List( AstFunctionDecl(
        AstLiteralString("setComment"),
        List(TyField("comment", TyStringImpl())),
        TyNamed("this.type")
      ).setValue(KeyBody, AstRawCode(s"this.comment = comment"))
        .setValue(KeyOverride, true)) else Nil),
      List(AstClassIdent("Ty" + TextTool.toPascalCase(ty.name)))
        ++ (if (ty.commentable) List(AstClassIdent("TyCommentable")) else Nil)
    ).setValue(KeyClassType, "class")
  }

  def generateScalaTypeToExpr(ty: Seq[Type]): AstRawCode = {
    def names(t: Type): String = t.fields.map(_.name).mkString(", ")
    def spliced_names(t: Type): String = t.fields
      .map(x => '$' + s"{ exprOption(${x.name}) }")
      .mkString(", ")
    def typeName(t: Type) = "Ty" + TextTool.toPascalCase(t.name) + "Impl"
    val cases = ty.map { t =>
      s"case ${typeName(t)}(${names(t)}) => '{ ${typeName(t)}(${spliced_names(t)}) }"
    }
    AstRawCode(s"""
      |object TyNode extends quoted.ToExpr[TyNode] {
      |  def apply(ty: TyNode)(using quotes: Quotes): Expr[TyNode] = {
      |    import quotes.reflect.*
      |    ty match {
      |      ${TextTool.indent(cases.mkString("\n"), 6)}
      |    }
      |  }
      |}
      |""".stripMargin)
  }
}
object ScalaSchemaParser {
  def getTypes: Map[String, Type] =
    Seq(
      Type("string"),
      Type("field")
        .field("name", TyStringImpl())
        .field("value", TyNode),
      Type("list")
        .field("content", TyNode),
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
        .field("dataframe", TyBooleanImpl())
        .field("schema", TyStringImpl())
        .is(TyNamed("class"))
        .setCommentable(true),
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
    val types = getTypes.values.toList

    val parser = ScalaSchemaParser()
    val extra = mutable.ArrayBuffer[String]()

    println("Parsed types")
    println(types.mkString("\n"))

    val fields = parser.collectFields(types, extra.toList)
    println("Parsed fields")
    println(fields.mkString("\n"))
    val keyObjects = fields.map(parser.generateScalaKeyObject)
    println("Generated key objects")
    println(keyObjects.mkString("\n"))
    val hasTraits = fields.map(parser.generateScalaHasTrait)
    println("Generated has traits")
    println(hasTraits.mkString("\n"))
    val caseClasses = types.map(parser.generateScalaCaseClass(_, extra.toList))
    println("Generated case classes")
    println(caseClasses.mkString("\n"))
    val compoundTraits = types.map(parser.generateScalaCompoundTrait(_, extra.toList))
    println("Generated compound traits")
    println(compoundTraits.mkString("\n"))
    val scalaTypeToExpr = parser.generateScalaTypeToExpr(types)
    val scalaCodegen = ScalaCodeGen(NoopNamingConvention)
    val scalaCode =
      (
//        keyObjects.map(scalaCodegen.generateClass)
//        ++
        hasTraits.map(scalaCodegen.generateClass)
          ++ caseClasses.map(scalaCodegen.generateClass)
          ++ compoundTraits.map(scalaCodegen.generateClass)
//        + scalaCodegen.generateRaw(scalaTypeToExpr)
      ).mkString("\n")

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

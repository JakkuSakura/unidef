package unidef.languages.schema

import com.typesafe.scalalogging.Logger
import io.circe.yaml.parser
import io.circe.{Json, JsonNumber, JsonObject}
import unidef.common.ty.*
import unidef.common.{NoopNamingConvention, ast}
import unidef.common.ast.*
import unidef.languages.javascript.{JsonSchemaCommon, JsonSchemaParser}
import unidef.languages.scala.{ScalaCodeGen, ScalaCommon}
import unidef.utils.FileUtils.readFile
import unidef.utils.{ParseCodeException, TextTool, TypeDecodeException, TypeEncodeException}

import java.io.PrintWriter
import scala.collection.mutable
def toAstClassName(s: String): String = "Ast" + TextTool.toPascalCase(s)

case class AstNodeCodeGen() {
  val logger: Logger = Logger[this.type]
  val common = JsonSchemaCommon(true)
  val scalaCommon = ScalaCommon()
  def collectFields(ty: Ast): Set[TyField] = {
    ty.fields.map(_.build()).toSet
  }
  def collectFields(types: List[Ast]): Set[TyField] = {
    types
      .flatMap(collectFields)
      .toSet
  }
  def scalaField(name: String, derive: String, methods: List[String]): AstClassDecl = {
    AstClassDecl(
      name,
      Nil,
      Nil,
      methods.map(x => AstRawCodeImpl(x, None)),
      List(AstClassIdent(derive))
    )
  }

  def generateScalaHasTrait(field: TyField): AstClassDecl = {
    val traitName = "Has" + TextTool.toPascalCase(field.name.get)
    val scalaCommon = ScalaCommon()
    val valueType =
      scalaCommon.encodeOrThrow(tryWrapValue(field), "Scala")

    scalaField(
      traitName,
      "AstNode",
      List(s"def ${TextTool.toCamelCase(field.name.get)}: ${valueType}")
    ).setValue(KeyClassType, "trait")
  }
  def generateScalaCompoundTrait(ty: Ast): AstClassDecl = {
    val fields = ty.fields.map(_.build()).toList

    AstClassDecl(
      toAstClassName(ty.name),
      Nil,
      Nil,
      fields
        .map(field =>
          val valueType =
            scalaCommon.encodeOrThrow(tryWrapValue(field), "Scala")

          AstRawCodeImpl(s"def ${TextTool.toCamelCase(field.name.get)}: ${valueType}", None)
        )
        .toList,
      List(AstClassIdent("AstNode"))
        :::
          fields
            .map(x => "Has" + TextTool.toPascalCase(x.name.get))
            .map(x => AstClassIdent(x))
            .toList
    ).setValue(KeyClassType, "trait")
  }
  def tryWrapValue(x: TyField): TyNode = if (x.defaultNone.get) TyOptionalImpl(x.value) else x.value
  def generateScalaCaseClass(ty: Ast): AstClassDecl = {
    val fields =
      ty.fields.map(_.build()).toList
    AstClassDecl(
      toAstClassName(ty.name) + "Impl",
      fields.map(x => AstValDefImpl(TextTool.toCamelCase(x.name.get), tryWrapValue(x), None, None)),
      Nil,
      Nil,
      List(AstClassIdent(toAstClassName(ty.name)))
    ).setValue(KeyClassType, "class")
  }
}
object AstNodeCodeGen {
  def getAsts: Map[String, Ast] = {
    val astNode = TyNamed("AstNode")
    Seq(
      Ast("unit"),
      Ast("null"),
      Ast("undefined"),
      Ast("block")
        .field("nodes", TyListImpl(astNode)),
      Ast("statement")
        .field("expr", astNode, required = true),
      Ast("if")
        .field("test", astNode)
        .field("consequent", astNode)
        .field("alternative", astNode),
      Ast("flow_control")
        .field("flow", TyNamed("FlowControl"))
        .field("value", astNode),
      Ast("literal")
        .field(
          "literal_value",
          TyStringImpl(),
          required = true
        ) // TODO: make subtypes of literal values?
        .field("ty", TyNamed("TyNode"), required = true),
      Ast("select")
        .field("qualifier", astNode, required = true)
        .field("symbol", TyStringImpl(), required = true),
      Ast("await").field("expr", astNode, required = true),
      Ast("raw_code")
        .field("code", TyStringImpl(), required = true)
        .field("language", TyStringImpl()),
      Ast("apply")
        .field("applicable", astNode, required = true)
        .field("arguments", TyListImpl(astNode), required = true),
      Ast("val_def")
        .field("name", TyStringImpl(), required = true)
        .field("ty", TyNamed("TyNode"), required = true)
        .field("value", astNode)
        .field("mutability", TyBooleanImpl()),
      Ast("decls")
        .field("decls", TyListImpl(astNode), required = true)
    ).map(x => x.name -> x).toMap
  }

  def main(args: Array[String]): Unit = {
    val types = getAsts.values.toList

    val parser = AstNodeCodeGen()

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
    val scalaCodegen = ScalaCodeGen(NoopNamingConvention)
    val scalaCode =
      (
        hasTraits.map(scalaCodegen.generateClass).toList
          ::: caseClasses.map(scalaCodegen.generateClass)
          ::: compoundTraits.map(scalaCodegen.generateClass)
      ).mkString("\n")

    println(scalaCode)
    val writer = new PrintWriter("target/AstNodeGen.scala")
    writer.println("""
                     |package unidef.common.ast
                     |import unidef.common.ty.*
                     |
                     |""".trim.stripMargin)
    writer.write(scalaCode)
    writer.close()

  }
}

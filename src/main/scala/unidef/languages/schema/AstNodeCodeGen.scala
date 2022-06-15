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

  def collectFields(ty: Ast, extra: List[String]): Set[TyField] = {
    // FIXME: it doesn't preserve orders
    ty.fields.toSet
  }
  def collectFields(types: List[Ast], extra: List[String]): Set[TyField] = {
    types
      .flatMap(x => collectFields(x, extra))
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
    val traitName = "Has" + TextTool.toPascalCase(field.name)
    val scalaCommon = ScalaCommon()
    val valueType =
      scalaCommon.encodeOrThrow(field.value, "Scala")

    scalaField(
      traitName,
      "AstNode",
      if (field.defaultNone.get) List(s"def get${TextTool.toPascalCase(field.name)}: Option[${valueType}]")
      else List(s"def get${TextTool.toPascalCase(field.name)}: ${valueType}")
    ).setValue(KeyClassType, "trait")
  }
  def generateScalaCompoundTrait(ty: Ast, extra: List[String]): AstClassDecl = {
    val fields = ty.fields.toList

    AstClassDecl(
      toAstClassName(ty.name),
      Nil,
      Nil,
      fields
        .map(x =>
          AstFunctionDecl(
            "get" + TextTool.toPascalCase(x.name),
            Nil,
            tryWrapValue(x)
          )
        )
        .toList,
      List(AstClassIdent("AstNode"))
        :::
        ty.equivalent
          .flatMap {
            case TyNamed(x) => Some(toAstClassName(x))
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
    ).setValue(KeyClassType, "trait")
  }
  def tryWrapValue(x: TyField): TyNode = if (x.defaultNone.get) TyOptionalImpl(x.value) else x.value
  def generateScalaCaseClass(ty: Ast, extra: List[String]): AstClassDecl = {
    val fields = ty.fields.toList ::: (if (ty.commentable) List(TyField("comment", TyStringImpl(), Some(true))) else Nil)

    AstClassDecl(
      toAstClassName(ty.name) + "Impl",
      fields.map(x => AstValDefImpl(x.name, tryWrapValue(x), None, None)),
      Nil,
      fields
        .map(x =>
          AstFunctionDecl(
            "get" + TextTool.toPascalCase(x.name),
            Nil,
            tryWrapValue(x)
          ).setValue(KeyBody, AstRawCodeImpl(x.name, None))
            .setValue(KeyOverride, true)
        )
        ::: (if (ty.commentable) List( AstFunctionDecl(
        "setComment",
        List(TyField("comment", TyStringImpl())),
        TyNamed("this.type")
      ).setValue(KeyBody, AstRawCodeImpl(s"this.comment = Some(comment)\n this", None))
        .setValue(KeyOverride, true)) else Nil),
      List(AstClassIdent(toAstClassName(ty.name)))
        ::: (if (ty.commentable) List(AstClassIdent("TyCommentable")) else Nil)
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
        .field("literal_value", TyStringImpl(), required = true) // TODO: make subtypes of literal values?
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
        .field("decls", TyListImpl(astNode), required = true),

    ).map(x => x.name -> x).toMap
  }

  def main(args: Array[String]): Unit = {
    val types = getAsts.values.toList

    val parser = AstNodeCodeGen()
    val extra = mutable.ArrayBuffer[String]()

    println("Parsed types")
    println(types.mkString("\n"))

    val fields = parser.collectFields(types, extra.toList)
    println("Parsed fields")
    println(fields.mkString("\n"))
    val hasTraits = fields.map(parser.generateScalaHasTrait)
    println("Generated has traits")
    println(hasTraits.mkString("\n"))
    val caseClasses = types.map(parser.generateScalaCaseClass(_, extra.toList))
    println("Generated case classes")
    println(caseClasses.mkString("\n"))
    val compoundTraits = types.map(parser.generateScalaCompoundTrait(_, extra.toList))
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

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
      methods.map(x => AstRawCodeImpl(Some(x), None)),
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
      List(s"def get${TextTool.toPascalCase(field.name)}: Option[${valueType}]")
    ).setValue(KeyClassType, "trait")
  }
  def generateScalaCompoundTrait(ty: Ast, extra: List[String]): AstClassDecl = {
    val fields = ty.fields.toList

    AstClassDecl(
      toAstClassName(ty.name),
      Nil,
      fields
        .map(x => x.name -> x.value)
        .map((k, v) =>
          AstFunctionDecl(
            AstLiteralString("get" + TextTool.toPascalCase(k)),
            Nil,
            TyOptionalImpl(Some(v))
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

  def generateScalaCaseClass(ty: Ast, extra: List[String]): AstClassDecl = {
    val fields = ty.fields.toList ::: (if (ty.commentable) List(TyField("comment", TyStringImpl(), Some(true))) else Nil)

    AstClassDecl(
      toAstClassName(ty.name) + "Impl",
      fields.map(x => AstValDefImpl(Some(x.name), Some(TyOptionalImpl(Some(x.value))), None, None)),
      fields
        .map(x =>
          AstFunctionDecl(
            AstLiteralString("get" + TextTool.toPascalCase(x.name)),
            Nil,
            TyOptionalImpl(Some(x.value))
          ).setValue(KeyBody, AstRawCodeImpl(Some(x.name), None))
            .setValue(KeyOverride, true)
        )
        ::: (if (ty.commentable) List( AstFunctionDecl(
        AstLiteralString("setComment"),
        List(TyField("comment", TyStringImpl())),
        TyNamed("this.type")
      ).setValue(KeyBody, AstRawCodeImpl(Some(s"this.comment = Some(comment)\n this"), None))
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
        .field("nodes", TyListImpl(Some(astNode))),
      Ast("statement")
        .field("expr", astNode),
      Ast("if")
        .field("test", astNode)
        .field("consequent", astNode)
        .field("alternative", astNode),
      Ast("flow_control")
        .field("flow", TyNamed("FlowControl"))
        .field("value", astNode),
      Ast("literal")
        .field("literal_value", TyStringImpl()) // TODO: make subtypes of literal values?
        .field("ty", TyNamed("TyNode")),
      Ast("select")
        .field("qualifier", astNode)
        .field("symbol", TyStringImpl()),
      Ast("await").field("expr", astNode),
      Ast("raw_code")
        .field("code", TyStringImpl())
        .field("language", TyStringImpl()),
      Ast("apply")
        .field("applicable", astNode)
        .field("arguments", TyListImpl(Some(astNode))),
      Ast("val_def")
        .field("name", TyStringImpl())
        .field("ty", TyNamed("TyNode"))
        .field("value", astNode)
        .field("mutability", TyBooleanImpl()),
      Ast("decls")
        .field("decls", TyListImpl(Some(astNode))),

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

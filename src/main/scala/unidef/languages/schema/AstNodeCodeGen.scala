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
  def collectFields(ty: Ast): Set[AstValDef] = {
    ty.fields.map(_.build()).toSet
  }
  def collectFields(types: List[Ast]): Set[AstValDef] = {
    types
      .flatMap(collectFields)
      .toSet
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
      .derive(AstIdentImpl(derive))
      .classType(classType)
      .build()

  }

  def generateScalaHasTrait(field: TyField): AstClassDecl = {
    val traitName = "Has" + TextTool.toPascalCase(field.name.get)
    val scalaCommon = ScalaCommon()
    val valueType =
      scalaCommon.encodeOrThrow(field.value, "Scala")

    scalaField(
      traitName,
      "AstNode",
      List(s"def ${TextTool.toCamelCase(field.name.get)}: ${valueType}"),
      Some("trait")
    )
  }
  def generateScalaCompoundTrait(ty: Ast): AstClassDecl = {
    val fields = ty.fields.map(_.build()).toList

    AstClassDeclBuilder()
      .name(toAstClassName(ty.name))
      .parameters(Asts.parameters(Nil))
      .methods(
        fields
          .map(field =>
            val valueType =
              scalaCommon.encodeOrThrow(field.ty, "Scala")

            AstRawCodeImpl(s"def ${TextTool.toCamelCase(field.name)}: ${valueType}", None)
          )
      )
      .derive(AstIdentImpl("AstNode"))
      .derives(
        fields
          .map(x => "Has" + TextTool.toPascalCase(x.name))
          .map(x => AstIdentImpl(x))
          .toList
      )
      .classType("trait")
      .build()
  }

  def generateScalaCaseClass(ty: Ast): AstClassDecl = {
    val fields =
      ty.fields.map(_.build()).toList
    AstClassDeclBuilder()
      .name(toAstClassName(ty.name) + "Impl")
      .parameters(Asts.parameters(fields))
      .derive(
        AstIdentImpl(toAstClassName(ty.name))
      )
      .classType("case class")
      .build()
  }
  def generateScalaBuilder(ty: Ast): AstClassDecl = {
    val fields = ty.fields
      .map(x =>
        x.name(TextTool.toCamelCase(x.name.get))
          .value(x.value)
          .build()
      )
      .toList
    val codegen = ScalaCodeGen(NoopNamingConvention)
    codegen.generateBuilder(
      "Ast" + TextTool.toPascalCase(ty.name) + "Builder",
      "Ast" + TextTool.toPascalCase(ty.name) + "Impl",
      fields
    )
  }
}
object AstNodeCodeGen {
  def getAsts: Map[String, Ast] = {
    val astNode = Types.named("AstNode")
    Seq(
      Ast("block")
        .field("stmts", Types.list(astNode), required = true),
      Ast("statement")
        .field("expr", astNode, required = true),
      Ast("if")
        .field("test", astNode, required = true)
        .field("consequent", astNode)
        .field("alternative", astNode),
      Ast("flow_control")
        .field("flow", Types.named("FlowControl"))
        .field("value", astNode),
      Ast("ident")
        .field("name", Types.string(), required = true),
      Ast("literal_string")
        .field("literal_string", Types.string(), required = true),
      Ast("literal_int")
        .field("literal_int", Types.i32(), required = true),
      Ast("literal_unit"),
      Ast("literal_none"),
      Ast("literal_null"),
      Ast("literal_undefined"),
      Ast("select")
        .field("qualifier", astNode, required = true)
        .field("symbol", Types.string(), required = true),
      Ast("await").field("expr", astNode, required = true),
      Ast("raw_code")
        .field("code", Types.string(), required = true)
        .field("language", Types.string()),
      Ast("parameter_list")
        .field(
          "parameter_list_content",
          Types.list(Types.named("AstValDef")),
          required = true
        ) // AstParameter later
      ,
      Ast("parameter_lists")
        .field(
          "parameter_lists_content",
          Types.list(Types.named("AstParameterList")),
          required = true
        ),
      Ast("argument")
        .field("name", Types.string(), required = true)
        .field("value", astNode),
      Ast("argument_list")
        .field(
          "argument_list_content",
          Types.list(Types.named("AstArgument")),
          required = true
        ),
      Ast("argument_lists")
        .field(
          "argument_lists_content",
          Types.list(Types.named("AstArgumentList")),
          required = true
        ),
      Ast("apply")
        .field("applicant", astNode, required = true)
        .field("arguments", Types.named("AstArgumentLists"), required = true),
      Ast("val_def")
        .field("name", Types.string(), required = true)
        .field("ty", TyNode, required = true)
        .field("value", astNode)
        .field("mutability", Types.bool())
        .field("auto_incr", Types.bool())
        .field("primary_key", Types.bool()),
      Ast("decls")
        .field("decls", Types.list(astNode), required = true),
      Ast("type")
        .field("ty", TyNode, required = true),
      Ast("class_decl")
        .field("name", Types.string(), required = true)
        .field("parameters", Types.named("AstParameterLists"), required = true)
        .field("fields", Types.list(Types.named("AstValDef")), required = true)
        .field("methods", Types.list(astNode), required = true)
        .field("derives", Types.list(astNode), required = true)
        .field("schema", Types.string())
        .field("dataframe", Types.bool())
        .field("class_type", Types.string())
        .field("access", Types.named("AccessModifier")),
      Ast("function_decl")
        .field("name", Types.string(), required = true)
        .field("parameters", Types.named("AstParameterLists"), required = true)
        .field("return_type", TyNode, required = true)
        .field("dataframe", Types.bool())
        .field("records", Types.bool())
        .field("comment", Types.string())
        .field("body", astNode)
        .field("schema", Types.string())
        .field("language", Types.string())
        .field("overwrite", Types.bool()),
      Ast("program")
        .field("stmts", Types.list(astNode), required = true),
      Ast("class_identifier")
        .field("class_id", Types.string(), required = true),
      Ast("variable_identifier")
        .field("variable_identifier", Types.string(), required = true),
      Ast("directive")
        .field("directive", Types.string(), required = true)
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
    val hasTraits = fields.map(getField).map(parser.generateScalaHasTrait)
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
    val writer = new PrintWriter("target/AstNodeGen.scala")
    writer.println("""
                     |package unidef.common.ast
                     |import unidef.common.ty.*
                     |import scala.collection.mutable
                     |
                     |""".trim.stripMargin)
    writer.write(scalaCode)
    writer.close()

  }
}

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

case class YamlSchemaParser() {
  val logger: Logger = Logger[this.type]
  val common = JsonSchemaCommon(true)

  def parseTypeApply(json: Json): AstTypeApply = {
    json.foldWith(new Json.Folder[AstTypeApply] {
      override def onNull: AstTypeApply = ???
      override def onBoolean(value: Boolean): AstTypeApply =
        AstTypeApply(AstTypeRef("bool"), Map("value" -> AstLiteralBoolean(value)))

      override def onNumber(value: JsonNumber): AstTypeApply =
        AstTypeApply(AstTypeRef("int"), Map("value" -> AstLiteralInteger(value.toInt.get)))

      override def onString(value: String): AstTypeApply = {
        if (common.decode(value).isDefined) {
          AstTypeApply(AstTypeRef(value))
        } else {
          AstTypeApply(AstTypeRef("string"), Map("value" -> AstLiteralString(value)))
        }
      }
      override def onArray(value: Vector[Json]): AstTypeApply = AstTypeApply(
        AstTypeRef("array"),
        Map("root" -> AstLiteralArray(value.map(parseTypeApply)))
      )
      override def onObject(value: JsonObject): AstTypeApply = {
        val tyCur = json.hcursor.downField("type")
        val ty =
          tyCur.focus
            .flatMap(_.asString)
            .getOrElse(
              throw ParseCodeException(tyCur.history.mkString(".") + " of object is not found")
            )
        logger.info(s"onObject $value")
        val v = AstTypeApply(
          AstTypeRef(ty),
          value.toIterable
            .filterNot((x, _) => x == "type")
            .map(
              _ -> parseTypeApply(_)
            )
            .toMap
        )
        logger.info(s"onObject result $v")
        v
      }
    })

  }
  def parseDecl(json: Json): AstDecl = {
    val obj = json.asObject.getOrElse(
      throw ParseCodeException(json.hcursor.history.mkString(" ") + " is not object")
    )
    val type_ = json.hcursor
      .downField("type")
      .focus
      .flatMap(_.asString)
      .getOrElse(
        throw ParseCodeException(json.hcursor.history.mkString(".") + " of string is not found")
      )

    val name = json.hcursor
      .downField("name")
      .focus
      .flatMap(_.asString)
      .getOrElse(
        throw ParseCodeException(
          json.hcursor.downField("name").history.mkString(".") + " of string is not found"
        )
      )
    type_ match {
      case "enum" =>
        val values = json.hcursor
          .downField("variants")
          .focus
          .flatMap(_.as[Array[String]].toOption)
          .getOrElse(
            throw ParseCodeException(
              json.hcursor.downField("variants").history.mkString(".") + " of array is not found"
            )
          )
          .map(x => TyVariant(Seq(x)))

        AstEnumDecl(TyEnum(values).setValue(KeyName, name))

      case "type" =>
        AstTypeDecl(
          name,
          obj.toIterable
            .filterNot((x, _) => x == "name")
            .map((k, v) => k -> parseTypeApply(v))
            .toMap
        )
      case "builtin" =>
        AstBuiltin(name)
    }
  }

  def parseFile(content: String): Seq[AstDecl] = {
    val typeRegistry = TypeRegistry()

    parser
      .parseDocuments(content)
      .flatMap {
        case Right(j) if j.isNull => None
        case Right(o) if o.isObject => Some(o.asObject.get)
        case Right(o) =>
          throw ParseCodeException("Invalid doc. Object only: " + o, null)
        case Left(err) => throw err
      }
      .map(x => parseDecl(Json.fromJsonObject(x)))
      .toArray
  }
  def decodeType(tyApp: AstTypeApply): TyNode = tyApp.ty.name match {
    case "list" =>
      TyList(
        decodeType(
          tyApp.args
            .getOrElse("content", throw ParseCodeException("Could not find content in " + tyApp))
            .asInstanceOf[AstTypeApply]
        )
      )
    case "option" =>
      TyOptional(
        decodeType(
          tyApp.args
            .getOrElse("content", throw ParseCodeException("Could not find content in " + tyApp))
            .asInstanceOf[AstTypeApply]
        )
      )
    case name =>
      common.decodeOrThrow(name, "Yaml schema")
  }
  def collectFields(ty: AstTypeDecl, extra: Seq[String]): Set[TyField] = {
    ty.params
      .map((k, v) =>
        TyField(
          k,
          if (
            v.ty.name == "string" && extra
              .contains(v.args("value").asInstanceOf[AstLiteralString].value)
          )
            TyNamed(v.args("value").asInstanceOf[AstLiteralString].value)
          else decodeType(v)
        )
      )
      .filterNot(x => Seq("is").contains(x.name))
      .toSet
  }
  def collectFields(types: Seq[AstTypeDecl], extra: Seq[String]): Set[TyField] = {
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
        scalaField(traitName, "KeywordInteger", Nil)
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
  def generateScalaCompoundTrait(ty: AstTypeDecl, extra: Seq[String]): AstClassDecl = {
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

  def generateScalaCaseClass(ty: AstTypeDecl, extra: Seq[String]): AstClassDecl = {
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

object YamlSchemaParser {
  def main(args: Array[String]): Unit = {
    val file = readFile("examples/types.yaml")
    val parser = YamlSchemaParser()
    val types = mutable.ArrayBuffer[AstTypeDecl]()
    val enums = mutable.ArrayBuffer[AstEnumDecl]()
    val extra = mutable.ArrayBuffer[String]()
    for (d <- parser.parseFile(file))
      d match {
        case AstBuiltin(name) =>
        case x @ AstEnumDecl(e) =>
          enums += x
          extra += e.getName.get
        case x @ AstTypeDecl(name, params) =>
          types += x
          if (!Seq("list", "int", "string", "bool", "option").contains(name))
            extra += name
        case _ => ???
      }

    println("Parsed types")
    println(types.mkString("\n"))

    println("Generated enums")
    println(enums.mkString("\n"))
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
      (enums.map(_.e).map(scalaCodegen.generateScala2Enum)
        ++ keyObjects.map(scalaCodegen.generateClass)
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

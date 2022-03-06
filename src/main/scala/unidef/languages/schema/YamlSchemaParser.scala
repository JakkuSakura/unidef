package unidef.languages.schema

import com.typesafe.scalalogging.Logger
import io.circe.yaml.parser
import io.circe.{Json, JsonNumber, JsonObject}
import unidef.languages.common.*
import unidef.languages.javascript.{JsonSchemaCommon, JsonSchemaParser}
import unidef.languages.scala.{ScalaCommon, ScalaCodeGen}
import unidef.utils.FileUtils.readFile
import unidef.utils.{ParseCodeException, TypeDecodeException, TypeEncodeException}

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
  def parseTypeDecl(json: Json): AstTypeDecl = {
    val obj = json.asObject.getOrElse(
      throw ParseCodeException(json.hcursor.history.mkString(" ") + " is not object")
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
    AstTypeDecl(
      name,
      obj.toIterable
        .filterNot((x, _) => x == "name")
        .map((k, v) => k -> parseTypeApply(v))
        .toMap
    )
  }

  def parseFile(content: String): Seq[AstTypeDecl] = {
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
      .map(x => parseTypeDecl(Json.fromJsonObject(x)))
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
      common
        .decode(name)
        .getOrElse(throw TypeDecodeException("Yaml schema type", name))

  }
  def collectFields(ty: AstTypeDecl): Set[TyField] = {
    ty.params
      .map((k, v) => TyField(k, decodeType(v)))
      .filterNot(x => Seq("is").contains(x.name))
      .toSet
  }
  def collectFields(types: Seq[AstTypeDecl]): Set[TyField] = {
    types
      .flatMap(collectFields)
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
          scalaCommon.encode(field.value).getOrElse(throw TypeEncodeException("Scala", field.value))
        scalaField(traitName, "Keyword", Seq(s"override type V = ${valueType}"))
    }
    cls.setValue(KeyClassType, "case object")
  }
  def generateScalaHasTrait(field: TyField): AstClassDecl = {
    val traitName = "Has" + field.name.capitalize
    val scalaCommon = ScalaCommon()
    val valueType =
      scalaCommon.encode(field.value).getOrElse(throw TypeEncodeException("Scala", field.value))

    scalaField(
      traitName,
      "TyNode",
      Seq(s"def get${field.name.capitalize}: ${valueType}")
    )
  }
  def generateScalaCaseClass(ty: AstTypeDecl): AstClassDecl = {
    val scalaCommon = ScalaCommon()
    val fields = collectFields(ty)

    AstClassDecl(
      AstLiteralString("Ty" + ty.name.capitalize),
      fields.toSeq,
      fields.toSeq
        .map(x => x.name -> x.value)
        .map((k, v) =>
          AstFunctionDecl(
            AstLiteralString(k),
            Nil,
            TyOptional(v)
          ).setValue(KeyBody, AstRawCode(k))
            .setValue(KeyOverride, true)
        ),
      fields
        .map(x => x.name -> x.value)
        .map((k, v) => "Has" + k.capitalize)
        .map(x => AstClassIdent(x))
        .toSeq
    )
  }
}

object YamlSchemaParser {
  def main(args: Array[String]): Unit = {
    val file = readFile("examples/types.yaml")
    val parser = YamlSchemaParser()
    val types = parser.parseFile(file)
    println("Parsed types")
    println(types.mkString("\n"))
    val fields = parser.collectFields(types)
    println("Parsed fields")
    println(fields.mkString("\n"))
    val keyObjects = fields.map(parser.generateScalaKeyObject)
    println("Generated key objects")
    println(keyObjects.mkString("\n"))
    val hasTraits = fields.map(parser.generateScalaHasTrait)
    println("Generated has traits")
    println(hasTraits.mkString("\n"))
    val caseClasses = types.map(parser.generateScalaCaseClass)
    println("Generated case classes")
    println(caseClasses.mkString("\n"))
    val scalaCodegen = ScalaCodeGen(NoopNamingConvention)
    println(
      (keyObjects.map(scalaCodegen.generateClass)
        ++ hasTraits.map(scalaCodegen.generateClass)
        ++ caseClasses.map(scalaCodegen.generateClass)).mkString("\n")
    )
  }
}

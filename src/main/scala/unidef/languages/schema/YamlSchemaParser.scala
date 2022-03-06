package unidef.languages.schema

import com.typesafe.scalalogging.Logger
import io.circe.yaml.parser
import io.circe.{Json, JsonNumber, JsonObject}
import unidef.languages.common.*
import unidef.languages.javascript.{JsonSchemaCommon, JsonSchemaParser}
import unidef.languages.scala.{AstTrait, ScalaCommon}
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
  def collectFields(types: Seq[AstTypeDecl]): Set[TyField] = {
    types
      .flatMap(
        _.params.map((k, v) => TyField(k, decodeType(v)))
      )
      .filterNot(x => Seq("is").contains(x.name))
      .toSet
  }

  def generateScalaTrait(field: TyField): AstTrait = {
    val traitName = "Key" + field.name.capitalize
    field.value match {
      case _: TyInteger => AstTrait(traitName, Seq("KeywordInteger"), Nil, Nil)
      case TyString => AstTrait(traitName, Seq("KeywordString"), Nil, Nil)
      case TyBoolean => AstTrait(traitName, Seq("KeywordBoolean"), Nil, Nil)
      case _ =>
        val scalaCommon = ScalaCommon()
        val valueType =
          scalaCommon.encode(field.value).getOrElse(throw TypeEncodeException("Scala", field.value))
        AstTrait(
          traitName,
          Seq("Keyword"),
          Seq(AstRawCode(s"override type V = ${valueType}")),
          Nil
        )
    }

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
    val traits = fields.map(parser.generateScalaTrait)
    println("Generated traits")
    println(traits.mkString("\n"))
  }
}

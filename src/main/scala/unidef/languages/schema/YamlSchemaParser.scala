package unidef.languages.schema

import com.typesafe.scalalogging.Logger
import io.circe.yaml.parser
import io.circe.{Json, JsonNumber, JsonObject}
import unidef.languages.common.*
import unidef.languages.javascript.JsonSchemaParser
import unidef.utils.FileUtils.readFile
import unidef.utils.ParseCodeException

import scala.collection.mutable

case class YamlSchemaParser() {
  val logger: Logger = Logger[this.type]

  def parseTypeApply(json: Json): AstTypeApply = {
    json.foldWith(new Json.Folder[AstTypeApply] {
      override def onNull: AstTypeApply = ???
      override def onBoolean(value: Boolean): AstTypeApply =
        AstTypeApply(AstTypeRef("bool"), Map("value" -> AstLiteralBoolean(value)))

      override def onNumber(value: JsonNumber): AstTypeApply =
        AstTypeApply(AstTypeRef("int"), Map("value" -> AstLiteralInteger(value.toInt.get)))

      override def onString(value: String): AstTypeApply = {
        AstTypeApply(AstTypeRef(json.asString.get))
        // TODO? AstTypeApply(AstTypeRef("str"), mutable.Map("value" -> AstLiteral(value)))
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

        AstTypeApply(
          AstTypeRef(ty),
          value.toIterable
            .map(
              _ -> parseTypeApply(_)
            )
            .toMap
        )

      }
    })

  }
  def parseTypeDecl(json: Json)(using types: TypeRegistry): AstTypeDecl = {
    val obj = json.asObject.getOrElse(
      throw ParseCodeException(json.hcursor.history.mkString(" ") + " is not object")
    )

    AstTypeDecl(obj.toIterable.map { case (k, v) =>
      k -> parseTypeApply(v)
    }.toMap)
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
      .map(x => parseTypeDecl(Json.fromJsonObject(x))(using typeRegistry))
      .toArray
  }

}

object YamlSchemaParser {
  def main(args: Array[String]): Unit = {
    val file = readFile("examples/types.yaml")
    println(YamlSchemaParser().parseFile(file).mkString("\n"))
  }
}

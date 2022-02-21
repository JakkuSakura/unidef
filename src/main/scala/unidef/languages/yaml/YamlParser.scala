package unidef.languages.yaml

import com.typesafe.scalalogging.Logger
import io.circe.yaml.parser
import io.circe.{Json, ParsingFailure}
import unidef.languages.common._
import unidef.languages.javascript.JsonSchemaParser

import scala.collection.mutable

// TODO: achieve json-schema like effect
case class YamlParser(jsParser: JsonSchemaParser) {
  val logger: Logger = Logger[this.type]
  @throws[ParsingFailure]
  def parseMarkdown(content: String): Seq[AstNode] = {
    var isYaml = false
    val sb = new mutable.StringBuilder()
    for (line <- content.split("\n")) {
      if (isYaml) {
        sb ++= line
        sb ++= "\n"
      } else if (line.startsWith("```yaml")) {
        isYaml = true
      } else if (line.startsWith("```")) {
        isYaml = false
        sb ++= "---\n"
      }
    }

    parseFile(sb.toString())
  }

  @throws[ParsingFailure]
  def parseFile(content: String): Seq[AstNode] = {
    parser
      .parseDocuments(content)
      .flatMap {
        case Right(j) if j.isNull   => None
        case Right(o) if o.isObject => Some(o.asObject.get)
        case Right(o) =>
          throw new ParsingFailure("Invalid doc. Object only: " + o, null)
        case Left(err) => throw err
      }
      .map(x => jsParser.parse(Json.fromJsonObject(x)))
      .map(AstTyped)
  }

}

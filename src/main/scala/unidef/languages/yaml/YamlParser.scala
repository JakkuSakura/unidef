package unidef.languages.yaml

import com.typesafe.scalalogging.Logger
import io.circe.yaml.parser
import io.circe.Json
import unidef.common.ty.{TyNode, TyStructImpl}
import unidef.common.ast
import unidef.common.ast.{AstNode, AstTyped}

import unidef.languages.javascript.JsonSchemaParser
import unidef.utils.ParseCodeException

import scala.collection.mutable

case class YamlParser(jsParser: JsonSchemaParser) {
  val logger: Logger = Logger[this.type]

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

  def parseFile(content: String): Seq[AstNode] = {
    parser
      .parseDocuments(content)
      .flatMap {
        case Right(j) if j.isNull => None
        case Right(o) if o.isObject => Some(o.asObject.get)
        case Right(o) =>
          throw ParseCodeException("Invalid doc. Object only: " + o, null)
        case Left(err) => throw err
      }
      .map { x =>
        val ty = jsParser.parse(Json.fromJsonObject(x))
        ty match {
          case y: TyStructImpl =>
            val name = x("name").flatMap(_.asString)
            TyStructImpl(name, y.fields, y.derives, y.attributes, y.dataframe, y.schema, "")
          case _ => ty
        }
      }
      .map {
        case a: AstNode => a
        case t: TyNode => ast.AstTyped(t)
      }
      .toArray
  }

}

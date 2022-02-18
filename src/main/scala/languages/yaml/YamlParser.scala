package com.jeekrs.unidef
package languages.yaml

import languages.common._
import utils.JsonUtils.{getList, getString}

import io.circe.yaml.parser
import io.circe.{Json, JsonNumber, JsonObject, ParsingFailure}

import scala.collection.mutable

sealed trait YamlType
object YamlType {
  case object Model extends YamlType
  case object Enum extends YamlType
  case object Function extends YamlType

  def parse(name: String): Option[YamlType] = name match {
    case "model"    => Some(YamlType.Model)
    case "enum"     => Some(YamlType.Enum)
    case "function" => Some(YamlType.Function)
    case _          => None

  }
}

object YamlParser {

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
      .map(parseDecl)
  }

  @throws[ParsingFailure]
  def parseDecl(content: JsonObject): AstNode = {
    val ty1 = getString(content, "type")
    val ty = YamlType
      .parse(ty1)
      .toRight(ParsingFailure("`type` is not one of YamlType: " + ty1, null))
      .toTry
      .get
    ty match {
      case YamlType.Model    => parseStruct(content)
      case YamlType.Function => parseFunction(content)
      //case YamlType.Enum => ???
      case _ => throw ParsingFailure("Could not handle type " + ty, null)

    }

  }

  @throws[ParsingFailure]
  def parseFunction(content: JsonObject): AstFunctionDecl = {
    val name = getString(content, "name")
    val language = getString(content, "language")
    val body = getString(content, "body")
    val parameters = content("parameters")
      .map(_ => getList(content, "parameters"))
      .getOrElse(Vector())
      .map(_.asObject.toRight(ParsingFailure("is not Object", null)).toTry.get)
      .map(parseFieldType)

    val ret = content("return")
      .map(_.foldWith(new Json.Folder[AstNode] {

        override def onString(value: String): AstNode =
          AstTyped(TypeParser.parse(value).toTry.get)

        override def onArray(value: Vector[Json]): AstNode = {
          parseStruct(
            JsonObject(
              ("name", Json.fromString("unnamed")),
              ("fields", Json.fromValues(value))
            )
          )
        }

        override def onObject(value: JsonObject): AstNode =
          parseStruct(value)

        override def onNull: AstNode = ???

        override def onBoolean(value: Boolean): AstNode = ???

        override def onNumber(value: JsonNumber): AstNode = ???
      }))
      .getOrElse(AstUnit)

    val node = AstFunctionDecl(
      AstLiteralString(name),
      parameters.toList,
      ret,
      AccessModifier.Public,
      AstRawCode(body, Some(language))
    )

    collectExtKeys(content, extKeysForFuncDecl.toList)
      .foreach(node.setValue)

    node
  }

  @throws[ParsingFailure]
  def parseStruct(content: JsonObject): AstClassDecl = {
    val fields_arr = getList(content, "fields")
    val name = getString(content, "name")

    val node = AstClassDecl(
      AstLiteralString(name),
      fields_arr
        .map(
          _.asObject.toRight(ParsingFailure("is not Object", null)).toTry.get
        )
        .map(parseFieldType)
        .toList
    )

    collectExtKeys(content, extKeysForClassDecl.toList).foreach(node.setValue)

    node
  }
  private val extKeysForField = mutable.HashSet[Keyword]()
  private val extKeysForFuncDecl = mutable.HashSet[Keyword]()
  private val extKeysForClassDecl = mutable.HashSet[Keyword]()

  def prepareForExtKeys(obj: KeywordProvider): Unit = {
    extKeysForField ++= obj.keysOnField
    extKeysForFuncDecl ++= obj.keysOnFuncDecl
    extKeysForClassDecl ++= obj.keysOnClassDecl
  }

  @throws[ParsingFailure]
  def parseFieldType(content: JsonObject): TyField = {
    val field = if (content("name").isDefined && content("type").isDefined) {
      val name = getString(content, "name")
      val ty = TypeParser.parse(getString(content, "type")).toTry.get
      TyField(name, ty)
    } else if (content.size == 1) {
      val name = content.keys.head
      val ty = TypeParser.parse(getString(content, name)).toTry.get
      TyField(name, ty)
    } else {
      throw new ParsingFailure(
        "FieldType must be either: has fields `name` and `type`, has the form of `name: type`",
        null
      )
    }

    collectExtKeys(content, extKeysForField.toList).foreach(field.setValue)

    field
  }
  def collectExtKeys(content: JsonObject,
                     keys: Seq[Keyword],
                     ignore: Seq[String] = Nil): Seq[(Keyword, Any)] = {
    content.toMap.iterator
      .filterNot { case (k, v) => ignore.contains(k) }
      .map {
        case (k, v) =>
          keys
            .find(kk => kk.name == k && kk.decoder.isDefined)
            .map((_, v))
            .getOrElse(
              throw ParsingFailure(s"${k} is not needed for field", null)
            )
      }
      .map {
        case (k, v) =>
          k -> v
            .as[k.V](k.decoder.get)
            .toTry
            .get
      }
      .toSeq
  }

}

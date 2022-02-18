package unidef.languages.yaml

import io.circe.yaml.parser
import io.circe.{Json, JsonNumber, JsonObject, ParsingFailure}
import unidef.languages.common
import unidef.languages.common._
import unidef.utils.JsonUtils.{getList, getString}

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

    val node = common.AstFunctionDecl(
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

    val node = common.AstClassDecl(
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
  private val extKeysForField = mutable.HashSet[Keyword](Name, Type, Fields)
  private val extKeysForFuncDecl =
    mutable.HashSet[Keyword](Name, Type, Body, Language, Parameters, Return)
  private val extKeysForClassDecl = mutable.HashSet[Keyword](Name, Type, Fields)

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
    if (content("name").isDefined)
      collectExtKeys(content, extKeysForField.toList).foreach(field.setValue)

    field
  }
  def collectExtKeys(content: JsonObject,
                     keywords: Seq[Keyword],
  ): Seq[(Keyword, Any)] = {
    content.keys.iterator.flatMap { key =>
      keywords
        .find(kw => kw.name == key)
        .fold {
          throw ParsingFailure(s"${key} is not needed in " + content, null)
        } {
          case kw if kw.decoder.isDefined =>
            Some(kw -> content(key).get.as[kw.V](kw.decoder.get))
          case _ => None
        }
    }.toSeq
  }

}

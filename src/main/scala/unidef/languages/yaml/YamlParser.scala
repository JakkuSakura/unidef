package unidef.languages.yaml

import com.typesafe.scalalogging.Logger
import io.circe.yaml.parser
import io.circe.{Json, JsonNumber, JsonObject, ParsingFailure}
import unidef.languages.common
import unidef.languages.common._
import unidef.languages.javascript.JsonSchemaExtended.parseType
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

// TODO: achieve json-schema like effect
object YamlParser {
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
    val parameters = content("parameters")
      .map(_ => getList(content, "parameters"))
      .getOrElse(Vector())
      .map(parseFieldType)

    val ret = content("return")
      .map(_.foldWith(new Json.Folder[TyNode] {

        override def onString(value: String): TyNode =
          parseType(value).toTry.get

        override def onArray(value: Vector[Json]): TyNode = {
          TyStruct(Some(value.map(f => parseFieldType(f))))
        }

        override def onObject(value: JsonObject): TyNode = {
          logger.error("Not supported yet " + value)
          ???
        }

        override def onNull: TyNode = ???

        override def onBoolean(value: Boolean): TyNode = ???

        override def onNumber(value: JsonNumber): TyNode = ???
      }))
      .getOrElse(TyUnit)
    val bodyVal =
      if (content("language").isDefined && content("language").isDefined) {
        val language = getString(content, "language")
        val body = getString(content, "body")
        Some(AstRawCode(body).setValue(Language, language))
      } else {
        None
      }

    val node = common.AstFunctionDecl(
      AstLiteralString(name),
      parameters.toList,
      ret,
      bodyVal
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
  def parseFieldType(js: Json): TyField = {
    js.foldWith(new Json.Folder[TyField] {

      override def onString(value: String): TyField =
        TyField("unnamed", parseType(value).toTry.get)

      override def onArray(value: Vector[Json]): TyField =
        throw ParsingFailure("Field should not be array", null)

      override def onObject(value: JsonObject): TyField = {
        val field = if (value("name").isDefined && value("type").isDefined) {
          val name = getString(value, "name")
          val ty = parseType(getString(value, "type")).toTry.get
          TyField(name, ty)
        } else if (value.size == 1) {
          val name = value.keys.head
          val ty = parseType(getString(value, name)).toTry.get
          TyField(name, ty)
        } else {
          throw new ParsingFailure(
            "FieldType must be either: has fields `name` and `type`, has the form of `name: type`",
            null
          )
        }
        // TODO: handle list and object recursively
        if (value("name").isDefined)
          collectExtKeys(value, extKeysForField.toList).foreach(field.setValue)

        field
      }

      override def onNull: TyField =
        throw ParsingFailure("Field should not be null", null)

      override def onBoolean(value: Boolean): TyField =
        throw ParsingFailure("Field should not be boolean", null)

      override def onNumber(value: JsonNumber): TyField =
        throw ParsingFailure("Field should not be number", null)
    })
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
            Some(kw -> content(key).get.as[kw.V](kw.decoder.get).toTry.get)
          case _ => None
        }
    }.toSeq
  }

}

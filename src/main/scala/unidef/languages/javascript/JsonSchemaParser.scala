package unidef.languages.javascript

import io.circe.Json.Folder
import io.circe._
import unidef.languages.common
import unidef.languages.common._
import unidef.utils.FileUtils.readFile
import unidef.utils.JsonUtils.{getList, getObject, getString, iterateOver}

import scala.collection.mutable

case class JsonSchemaParser(extended: Boolean) {
  private def parseType(s: String) =
    JsonSchemaCommon(extended)
      .parseType(s)
      .getOrElse(throw ParsingFailure("Failed to parse type " + s, null))
  @throws[ParsingFailure]
  def parseFunction(content: JsonObject): TyApplicable = {
    val name = getString(content, "name")
    val parameters = content("parameters")
      .map(_ => getList(content, "parameters"))
      .getOrElse(Vector())
      .map(parseFieldType)

    val ret = content("return")
      .map(
        x =>
          if (x.isString)
            parseType(x.asString.get)
          else
            TyStruct(
              Some(
                iterateOver(x, "name", "type")
                  .map {
                    case (name, json) =>
                      val ty = parse(Json.fromJsonObject(json))
                      TyField(name, ty)
                  }
              )
          )
      )
      .getOrElse(TyUnit)

    val node =
      common.AstFunctionDecl(AstLiteralString(name), parameters.toList, ret)

    if (content("language").isDefined && content("language").isDefined) {
      val language = getString(content, "language")
      val body = getString(content, "body")
      node.setValue(KeyBody, AstRawCode(body).setValue(KeyLanguage, language))
    }

    collectExtKeys(content, extKeysForFuncDecl.toList)
      .foreach(node.setValue)

    node
  }

  @throws[ParsingFailure]
  def parseStruct(value: JsonObject): TyClass = {
    val fields =
      value("properties")
        .orElse(if (extended) value("fields") else None)
        .map(
          x =>
            // check extended
            iterateOver(x, "name", "type")
              .map {
                case (name, json) =>
                  val ty = parse(Json.fromJsonObject(json))
                  TyField(name, ty)
            }
        )

    val node = TyStruct(fields)

    collectExtKeys(value, extKeysForClassDecl.toList).foreach(node.setValue)

    node
  }
  private val extKeysForField =
    mutable.HashSet[Keyword](KeyName, KeyType, KeyFields)
  private val extKeysForFuncDecl =
    mutable
      .HashSet[Keyword](
        KeyName,
        KeyType,
        KeyBody,
        KeyLanguage,
        KeyParameters,
        KeyReturn
      )
  private val extKeysForClassDecl =
    mutable.HashSet[Keyword](KeyName, KeyType, KeyFields, KeyProperties)

  def prepareForExtKeys(obj: KeywordProvider): Unit = {
    extKeysForField ++= obj.keysOnField
    extKeysForFuncDecl ++= obj.keysOnFuncDecl
    extKeysForClassDecl ++= obj.keysOnClassDecl
  }

  @throws[ParsingFailure]
  def parseFieldType(js: Json): TyField = {
    js.foldWith(new Json.Folder[TyField] {

      override def onString(value: String): TyField =
        TyField("unnamed", parseType(value))

      override def onArray(value: Vector[Json]): TyField =
        throw ParsingFailure("Field should not be array", null)

      override def onObject(value: JsonObject): TyField = {
        val field = if (value("name").isDefined && value("type").isDefined) {
          val name = getString(value, "name")
          val ty = parseType(getString(value, "type"))
          TyField(name, ty)
        } else if (value.size == 1) {
          val name = value.keys.head
          val ty = parseType(getString(value, name))
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
          //throw ParsingFailure(s"${key} is not needed in " + content, null)
          None.asInstanceOf[Option[(Keyword, Any)]]
        } {
          case kw if kw.decoder.isDefined =>
            Some(kw -> content(key).get.as[kw.V](kw.decoder.get).toTry.get)
          case _ => None
        }
    }.toSeq
  }

  def parse(json: Json): TyNode = {
    json.foldWith(new Folder[TyNode] {
      override def onNull: TyNode = ???

      override def onBoolean(value: Boolean): TyNode = ???

      override def onNumber(value: JsonNumber): TyNode = ???

      // extension
      override def onString(value: String): TyNode =
        parseType(value)

      override def onArray(value: Vector[Json]): TyNode = ???

      override def onObject(value: JsonObject): TyNode = {
        if (value("type").exists(_.isArray)) {
          // probably map(parseType) is enough
          TyUnion(getList(value, "type").map(parse))
        } else if (value("anyOf").isDefined) {
          TyUnion(getList(value, "anyOf").map(parse))
        } else if (value("enum").exists(_.isArray)) {
          TyEnum(
            getList(value, "enum")
              .map(_.asString.get)
              .map(x => TyVariant(Seq(x)))
          )
        } else {
          getString(value, "type") match {
            case "object" => parseStruct(value)
            case "array" =>
              val items = getObject(value, "items")
              TyList(parse(Json.fromJsonObject(items)))
            case "function" if extended => parseFunction(value)
            case ty                     => parseType(ty)
          }

        }

      }
    })
  }

}
object JsonSchemaParser {

  def main(args: Array[String]): Unit = {
    println(
      JsonSchemaParser(true).parse(parser.parse(readFile(args(0))).toTry.get)
    )
  }
}

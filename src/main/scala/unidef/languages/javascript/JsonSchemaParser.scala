package unidef.languages.javascript

import io.circe.Json.Folder
import io.circe.*
import unidef.languages.common
import unidef.languages.common.*
import unidef.utils.FileUtils.readFile
import unidef.utils.JsonUtils.{getList, getObject, getString, iterateOver}
import unidef.utils.{ParseCodeException, TypeDecodeException}

import scala.collection.mutable
class JsonSchemaParserOption(
    val extendedGrammar: Boolean = true
)
class JsonSchemaParser(options: JsonSchemaParserOption = JsonSchemaParserOption()) {
  val jsonSchemaCommon: JsonSchemaCommon = JsonSchemaCommon(options.extendedGrammar)

  def parseFunction(content: JsonObject): TyApplicable = {
    val name = getString(content, "name")
    val parameters = content("parameters")
      .map(_ => getList(content, "parameters"))
      .getOrElse(Vector())
      .map(parseFieldType)

    val ret = content("return")
      .map(x =>
        if (x.isString)
          jsonSchemaCommon.decodeOrThrow(x.asString.get)
        else
          TyStructImpl(
            None,
            Some(
              iterateOver(x, "name", "type").map { (name, json) =>
                val ty = parse(Json.fromJsonObject(json))
                TyField(name, ty)
              }.toList
            ),
            None,
            None
          )
      )
      .getOrElse(TyUnitImpl())

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

  def parseStruct(value: JsonObject): TyStruct = {
    val fields =
      value("properties")
        .orElse(if (options.extendedGrammar) value("fields") else None)
        .map(x =>
          // check extended
          iterateOver(x, "name", "type")
            .map { (name, json) =>
              val ty = parse(Json.fromJsonObject(json))
              TyField(name, ty)
            }
        )
        // TODO TyNamed or TyStruct?
        .getOrElse(Nil)

    val node = TyStructImpl(None, Some(fields.toList), None, None)
//    collectExtKeys(value, extKeysForClassDecl.toList).foreach(node.setValue)

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
        KeyReturn,
        KeyDataframe
      )
  private val extKeysForClassDecl =
    mutable.HashSet[Keyword](KeyName, KeyType, KeyFields, KeyProperties, KeyDataframe)

  def prepareForExtKeys(obj: KeywordProvider): Unit = {
    extKeysForField ++= obj.keysOnField
    extKeysForFuncDecl ++= obj.keysOnFuncDecl
    extKeysForClassDecl ++= obj.keysOnClassDecl
  }

  def parseFieldType(js: Json): TyField = {
    js.foldWith(new Json.Folder[TyField] {

      override def onString(value: String): TyField =
        TyField("unnamed", jsonSchemaCommon.decodeOrThrow(value))

      override def onArray(value: Vector[Json]): TyField =
        throw ParseCodeException("Field should not be array", null)

      override def onObject(value: JsonObject): TyField = {
        val field = if (value("name").isDefined && value("type").isDefined) {
          val name = getString(value, "name")
          val ty = jsonSchemaCommon.decodeOrThrow(getString(value, "type"))
          TyField(name, ty)
        } else if (value.size == 1) {
          val name = value.keys.head
          val ty = jsonSchemaCommon.decodeOrThrow(getString(value, name))
          TyField(name, ty)
        } else {
          throw ParseCodeException(
            "FieldType must be either: has fields `name` and `type`, has the form of `name: type`. Got " + value
          )
        }
        // TODO: handle list and object recursively
        if (value("name").isDefined)
          collectExtKeys(value, extKeysForField.toList).foreach(field.setValue)

        field
      }

      override def onNull: TyField =
        throw ParseCodeException("Field should not be null")

      override def onBoolean(value: Boolean): TyField =
        throw ParseCodeException("Field should not be boolean")

      override def onNumber(value: JsonNumber): TyField =
        throw ParseCodeException("Field should not be number")
    })
  }
  def collectExtKeys(content: JsonObject, keywords: Seq[Keyword]): Seq[(Keyword, Any)] = {
    content.keys.iterator.flatMap { key =>
      keywords
        .find(kw => kw.name == key)
        .fold {
          // throw UnidefParseException(s"${key} is not needed in " + content, null)
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
        jsonSchemaCommon.decodeOrThrow(value)

      override def onArray(value: Vector[Json]): TyNode = ???

      override def onObject(value: JsonObject): TyNode = {
        if (value("type").exists(_.isArray)) {
          // probably map(parseType) is enough
          TyUnion(getList(value, "type").toList.map(parse))
        } else if (value("anyOf").isDefined) {
          TyUnion(getList(value, "anyOf").toList.map(parse))
        } else if (value("enum").exists(_.isArray)) {
          TyEnum(
            getList(value, "enum")
              .map(_.asString.get)
              .map(x =>
                TyVariant(
                  List(x),
                  if (options.extendedGrammar && value("number").isDefined)
                    Some(value("number").get.asNumber.get.toInt.get)
                  else None
                )
              )
              .toList
          ).trySetValue(
            KeyReturn,
            value("int_enum").map(x =>
              if (options.extendedGrammar && x.asBoolean.get)
                TyIntegerImpl(Some(BitSize.B8), Some(true))
              else TyStringImpl()
            )
          )
        } else {
          getString(value, "type") match {
            case "object" => parseStruct(value)
            case "array" if options.extendedGrammar =>
              TyListImpl(Some(value("items").map(parse).getOrElse(TyAnyImpl())))
            case "array" =>
              val items = getObject(value, "items")
              TyListImpl(Some(parse(Json.fromJsonObject(items))))
            case "function" if options.extendedGrammar => parseFunction(value)
            case ty => jsonSchemaCommon.decodeOrThrow(ty)
          }

        }

      }
    })
  }

}

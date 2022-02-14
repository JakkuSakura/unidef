package com.jeekrs.unidef
package languages.yaml

import languages.common._
import utils.JsonUtils.{getList, getString}
import utils.{ExtKey, TypedValue}

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
  def parseFile(content: String): List[AstNode] = {
    val agg = mutable.ArrayBuffer[AstNode]()
    for (v <- parser.parseDocuments(content)) {
      val content = v.toTry.get
      content.foldWith(new Json.Folder[Unit] {
        override def onObject(value: JsonObject): Unit = agg += parseDecl(value)

        override def onNull: Unit = {}

        override def onBoolean(value: Boolean): Unit = {}

        override def onNumber(value: JsonNumber): Unit = {}

        override def onString(value: String): Unit = {}

        override def onArray(value: Vector[Json]): Unit = {}
      })
    }

    agg.toList

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
  def parseFunction(content: JsonObject): FunctionDeclNode = {
    val name = getString(content, "name")
    val language = getString(content, "language")
    val body = getString(content, "body")
    val parameters = content("parameters")
      .map(_ => getList(content, "parameters"))
      .getOrElse(Vector())
      .map(_.asObject.toRight(ParsingFailure("is not Object", null)).toTry.get)
      .map(parseFieldType)

    val ret = content("return")
      .map(x => {
        x.foldWith(new Json.Folder[AstNode] {

          override def onString(value: String): AstNode =
            TypedNode(TypeParser.parse(value).toTry.get)

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
        })
      })
      .getOrElse(UnitNode)

    val node = FunctionDeclNode(
      LiteralString(name),
      parameters.toList,
      ret,
      AccessModifier.Public,
      RawCodeNode(body, Some(language))
    )

    collectExtKeys(content, extKeysForDecl.toList).foreach(node.setValue)

    node
  }

  @throws[ParsingFailure]
  def parseStruct(content: JsonObject): ClassDeclNode = {
    val fields_arr = getList(content, "fields")
    val name = getString(content, "name")

    ClassDeclNode(
      LiteralString(name),
      fields_arr
        .map(
          _.asObject.toRight(ParsingFailure("is not Object", null)).toTry.get
        )
        .map(parseFieldType)
        .toList
    )

  }
  private val extKeysForField = mutable.HashSet[ExtKey]()
  private val extKeysForDecl = mutable.HashSet[ExtKey]()

  def prepareForExtKeys(obj: GetExtKeys): Unit = {
    extKeysForField ++= obj.keysOnField
    extKeysForDecl ++= obj.keysOnFuncDecl
  }

  @throws[ParsingFailure]
  def parseFieldType(content: JsonObject): FieldType = {
    val field = if (content("name").isDefined && content("type").isDefined) {
      val name = getString(content, "name")
      val ty = TypeParser.parse(getString(content, "type")).toTry.get
      FieldType(name, ty)
    } else if (content.size == 1) {
      val name = content.keys.head
      val ty = TypeParser.parse(getString(content, name)).toTry.get
      FieldType(name, ty)
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
                     keys: List[ExtKey]): List[TypedValue] = {
    val result = mutable.ArrayBuffer[TypedValue]()
    for (k <- keys) {
      content(k.name) match {
        case Some(value) =>
          val v = value
            .as[k.V](
              k.decoder
                .toRight(
                  ParsingFailure(
                    s"${k.name} should not be used as key for field",
                    null
                  )
                )
                .toTry
                .get
            )
            .toTry
            .get
          result += k(v)

        case None =>
      }

    }
    result.toList
  }

}

package com.jeekrs.unidef
package languages.yaml

import languages.common._
import languages.sql.FieldType.{AutoIncr, Nullable, PrimaryKey}
import utils.{ExtKey, GetExtKeys}
import utils.JsonUtils.{getAs, getList, getString}

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
    val parameters = getList(content, "parameters")
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
    content("annotations") match {
      case Some(_) =>
        node.setValue(
          Annotations(
            getAs[List[String]](content, "annotations")
              .map(code => Annotation(RawCodeNode(code)))
          )
        )
      case None =>
    }

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
  private val extKeysForField: mutable.ArrayBuffer[ExtKey] =
    mutable.ArrayBuffer[ExtKey]()

  def prepareForExtKeys(obj: GetExtKeys): Unit =
    extKeysForField ++= obj.getExtKeys

  @throws[ParsingFailure]
  def parseFieldType(content: JsonObject): FieldType = {
    val name = getString(content, "name")
    val ty = TypeParser.parse(getString(content, "type")).toTry.get
    val field = FieldType(name, ty)

    for (key <- content.keys) {
      key match {
        case "name" =>
        case "type" =>
        case key =>
          for (k <- extKeysForField if key == k.name) {
            val value = getAs[k.V](content, key)(
              k.decoder
                .toRight(
                  ParsingFailure(
                    s"$key should not be used as key for field",
                    null
                  )
                )
                .toTry
                .get
            )
            field.setValue(k(value))
          }

      }
    }
    field

  }

}

package com.jeekrs.unidef
package languages.yaml

import languages.common._
import languages.sql.FieldType.{AutoIncr, PrimaryKey}
import utils.Extendable
import utils.JsonUtils.{getBool, getString}

import io.circe.yaml.parser
import io.circe.{JsonObject, ParsingFailure}

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

class YamlParser {
  @throws[ParsingFailure]
  def parseFile(content: String): List[Extendable] = {
    val agg = mutable.ArrayBuffer[Extendable]()
    for (v <- parser.parseDocuments(content))
      agg += parseDecl(
        v.toTry.get.asObject
          .toRight(ParsingFailure("is not object", null))
          .toTry
          .get
      )

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
      case YamlType.Function => parseIrNodeFunction(content)
      //case YamlType.Enum => ???
      case _ => throw ParsingFailure("Could not handle type " + ty, null)

    }

  }

  @throws[ParsingFailure]
  def parseIrNodeFunction(content: JsonObject): FunctionDeclNode =
    ???

  @throws[ParsingFailure]
  def parseStruct(content: JsonObject): ClassDeclNode = {
    val fields = content("fields")
      .toRight(ParsingFailure("Missing `fields`", null))
      .toTry
      .get
    val fields_arr = fields.asArray
      .toRight(ParsingFailure("`fields` is not array", null))
      .toTry
      .get
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

  @throws[ParsingFailure]
  def parseFieldType(content: JsonObject): FieldType = {
    val name = getString(content, "name")
    val ty = TypeParser.parse(getString(content, "type")).toTry.get
    val field = FieldType(name, ty)

    for (key <- content.keys) {

      key match {
        case "name" =>
        case "type" =>
        case "primary" =>
          field.setValue(PrimaryKey(getBool(content, "primary")))
        case "auto_incr" =>
          field.setValue(AutoIncr(getBool(content, "auto_incr")))
        case _ =>
      }
    }
    field

  }

}

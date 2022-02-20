package unidef.languages.javascript

import io.circe.{Json, JsonNumber, JsonObject, parser}
import io.circe.Json.Folder
import unidef.languages.common._
import unidef.languages.javascript.JsonSchemaCommon.parseType
import unidef.utils.FileUtils.readFile
import unidef.utils.JsonUtils.{getList, getObject, getString}

object JsonSchemaParser {
  def parse(json: Json): TyNode = {
    json.foldWith(new Folder[TyNode] {
      override def onNull: TyNode = ???

      override def onBoolean(value: Boolean): TyNode = ???

      override def onNumber(value: JsonNumber): TyNode = ???

      // extension
      override def onString(value: String): TyNode = parseType(value).toTry.get

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
            case "object" if value("properties").isDefined =>
              val properties = getObject(value, "properties")
              TyStruct(Some(properties.toMap.map {
                case ((name, json)) =>
                  val ty = parse(json)
                  TyField(name, ty)
              }.toSeq))
            case "array" =>
              val items = getObject(value, "items")
              TyList(parse(Json.fromJsonObject(items)))
            case ty => parseType(ty).toTry.get
          }

        }

      }
    })
  }

  def main(args: Array[String]): Unit = {
    println(parse(parser.parse(readFile(args(0))).toTry.get))
  }
}

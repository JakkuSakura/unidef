package unidef.utils

import io.circe.{Decoder, Json, JsonObject, ParsingFailure}

object JsonUtils {
  def getProperty(obj: JsonObject, name: String): Either[ParsingFailure, Json] =
    obj(name).toRight(ParsingFailure("Key " + name + " does not exist", null))

  def tryGetName(json: JsonObject, key: String = "name"): String =
    if (json.size == 1) json.keys.toSeq.head else getString(json, key)

  def tryGetValue(json: JsonObject, key: String = "value"): JsonObject =
    if (json.size == 1) JsonObject(key -> json.values.toSeq.head) else json

  def iterateOver(json: Json,
                  keyForName: String,
                  keyForValue: String): Seq[(String, JsonObject)] = {
    if (json.asArray.isDefined) {
      json.asArray.get
        .map(_.asObject.get)
        .map(x => tryGetName(x, keyForName) -> x)
        .toSeq
    } else {
      json.asObject.get.toIterable.map {
        case (k, v) => k -> tryGetValue(v.asObject.get, keyForValue)
      }.toSeq
    }

  }

  def getAs[A](obj: JsonObject, name: String)(implicit d: Decoder[A]): A =
    getProperty(obj, name)
      .flatMap(_.as[A])
      .toTry
      .get

  @throws[ParsingFailure]
  def getBool(obj: JsonObject, name: String): Boolean =
    getProperty(obj, name)
      .flatMap(_.asBoolean.toRight(ParsingFailure(name + " is not bool", null)))
      .toTry
      .get

  @throws[ParsingFailure]
  def getString(obj: JsonObject, name: String): String =
    getProperty(obj, name)
      .flatMap(
        _.asString.toRight(ParsingFailure(name + " is not string", null))
      )
      .toTry
      .get

  @throws[ParsingFailure]
  def getList(obj: JsonObject, name: String): Vector[Json] =
    getProperty(obj, name)
      .flatMap(_.asArray.toRight(ParsingFailure(name + " is not list", null)))
      .toTry
      .get

  @throws[ParsingFailure]
  def getObject(obj: JsonObject, name: String): JsonObject =
    getProperty(obj, name)
      .flatMap(
        _.asObject.toRight(ParsingFailure(name + " is not JsonObject", null))
      )
      .toTry
      .get

}

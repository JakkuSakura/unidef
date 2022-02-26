package unidef.utils

import io.circe.{Decoder, Json, JsonObject}

object JsonUtils {
  def getProperty(obj: JsonObject, name: String): Json =
    obj(name).getOrElse(
      throw ParseCodeException("Key " + name + " does not exist")
    )

  def tryGetName(json: JsonObject, key: String = "name"): String =
    if (json.size == 1) json.keys.toSeq.head else getString(json, key)

  def tryGetValue(
      json: JsonObject,
      keyKey: String,
      keyValue: String,
      valueKey: String = "value"
  ): JsonObject =
    if (json.size == 1)
      JsonObject(
        keyKey -> Json.fromString(keyValue),
        valueKey -> json.values.toSeq.head
      )
    else json

  def iterateOver(
      json: Json,
      keyForName: String,
      keyForValue: String
  ): Seq[(String, JsonObject)] = {
    if (json.asArray.isDefined) {
      json.asArray.get
        .map(_.asObject.get)
        .map(x => {
          val name =
            tryGetName(x, keyForName)
          name -> tryGetValue(x, keyForName, name, keyForValue)
        })
        .toSeq
    } else {
      json.asObject.get.toIterable.map { case (k, v) =>
        k -> tryGetValue(v.asObject.get, keyForName, k, keyForValue)
      }.toSeq
    }

  }

  def getAs[A](obj: JsonObject, name: String)(implicit d: Decoder[A]): A =
    getProperty(obj, name)
      .as[A]
      .getOrElse(throw ParseCodeException(name + " is not bool"))

  def getBool(obj: JsonObject, name: String): Boolean =
    getProperty(obj, name).asBoolean
      .getOrElse(throw ParseCodeException(name + " is not bool"))

  def getString(obj: JsonObject, name: String): String =
    getProperty(obj, name).asString
      .getOrElse(throw ParseCodeException(name + " is not string"))

  def getList(obj: JsonObject, name: String): Vector[Json] =
    getProperty(obj, name).asArray
      .getOrElse(throw ParseCodeException(name + " is not list"))

  def getObject(obj: JsonObject, name: String): JsonObject =
    getProperty(obj, name).asObject
      .getOrElse(throw ParseCodeException(name + " is not JsonObject"))

}

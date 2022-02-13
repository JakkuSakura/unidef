package com.jeekrs.unidef
package utils

import io.circe.{Json, JsonObject, ParsingFailure}

object JsonUtils {
  def getJson(obj: JsonObject, name: String): Either[ParsingFailure, Json] =
    obj(name).toRight(ParsingFailure(name + " does not exist", null))

  @throws[ParsingFailure]
  def getBool(obj: JsonObject, name: String): Boolean =
    getJson(obj, name)
      .flatMap(_.asBoolean.toRight(ParsingFailure(name + " is not bool", null)))
      .toTry
      .get

  @throws[ParsingFailure]
  def getString(obj: JsonObject, name: String): String =
    getJson(obj, name)
      .flatMap(
        _.asString.toRight(ParsingFailure(name + " is not string", null))
      )
      .toTry
      .get

  @throws[ParsingFailure]
  def getObject(obj: JsonObject, name: String): JsonObject =
    getJson(obj, name)
      .flatMap(
        _.asObject.toRight(ParsingFailure(name + " is not JsonObject", null))
      )
      .toTry
      .get

}

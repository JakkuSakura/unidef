package com.jeekrs.unidef
package languages.javascript

import languages.common._
import utils.ExtKeyBoolean

import io.circe.{Json, JsonObject}

// meant for private use
case object Required extends ExtKeyBoolean
case object AdditionalProperties extends ExtKeyBoolean

case object JsonSchemaCodeGen {
  def generateFuncDecl(func: AstFunctionDecl): String = {
    val struct = TyStruct("unnamed", func.parameters)
    struct.setValue(Required, true)
    struct.setValue(AdditionalProperties, false)

    val obj = generateType(struct)
    //  "required": [
    //  ],
    //  "additionalProperties": false
    Json
      .fromJsonObject(obj)
      .spaces2
  }

  def generateType(ty: TyNode): JsonObject =
    ty match {
      case TyString => JsonObject("type" -> Json.fromString("string"))
      case _: TyInteger =>
        JsonObject("type" -> Json.fromString("integer"))
      case _: TyFloat =>
        JsonObject("type" -> Json.fromString("float"))
      case _: TyNumeric =>
        JsonObject("type" -> Json.fromString("numeric"))
      case TyVector(ty) =>
        JsonObject(
          "type" -> Json.fromString("array"),
          "items" -> Json.fromJsonObject(generateType(ty))
        )
      case struct: TyStruct =>
        JsonObject(
          "type" -> Json.fromString("object"),
          "properties" -> Json.fromJsonObject(
            JsonObject.fromIterable(
              struct.fields
                .map(f => f.name -> Json.fromJsonObject(generateType(f.value)))
            )
          )
        )
    }
}

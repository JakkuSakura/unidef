package com.jeekrs.unidef
package languages.javascript

import languages.common._
import utils.ExtKeyBoolean

import io.circe.{Json, JsonObject}

// meant for private use
case object Required extends ExtKeyBoolean
case object AdditionalProperties extends ExtKeyBoolean

case object JsonSchemaCodeGen {
  def generateFuncDecl(func: FunctionDeclNode): String = {
    val struct = StructType("unnamed", func.parameters)
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
      case StringType => JsonObject("type" -> Json.fromString("string"))
      case _: IntegerType =>
        JsonObject("type" -> Json.fromString("integer"))
      case _: FloatType =>
        JsonObject("type" -> Json.fromString("float"))
      case _: NumericType =>
        JsonObject("type" -> Json.fromString("numeric"))
      case VectorType(ty) =>
        JsonObject(
          "type" -> Json.fromString("array"),
          "items" -> Json.fromJsonObject(generateType(ty))
        )
      case struct: StructType =>
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

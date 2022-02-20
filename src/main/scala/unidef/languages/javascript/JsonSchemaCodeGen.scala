package unidef.languages.javascript

import com.typesafe.scalalogging.Logger
import io.circe.{Json, JsonObject}
import unidef.languages.common.{
  AstFunctionDecl,
  HasTimeUnit,
  KeywordBoolean,
  TyBoolean,
  TyDateTime,
  TyEnum,
  TyFloat,
  TyInteger,
  TyJsonObject,
  TyNamed,
  TyNode,
  TyNumeric,
  TyString,
  TyStruct,
  TyTimeStamp,
  TyVector
}

// meant for private use
case object Required extends KeywordBoolean
case object AdditionalProperties extends KeywordBoolean

case object JsonSchemaCodeGen {
  val logger: Logger = Logger[this.type]

  def generateFuncDecl(func: AstFunctionDecl): String = {
    val struct = TyStruct(func.parameters)
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
      case TyBoolean =>
        JsonObject("type" -> Json.fromString("boolean"))
      case _: TyDateTime =>
        JsonObject(
          "type" -> Json.fromString("string"),
          "format" -> Json.fromString("date-time")
        )
      case t: TyTimeStamp =>
        JsonObject(
          "type" -> Json.fromString("integer"),
          "format" -> Json.fromString("timestamp"),
          "unit" -> t
            .getValue(HasTimeUnit)
            .map(_.toString)
            .map(Json.fromString)
            .getOrElse(Json.Null)
        )
      case TyVector(ty) =>
        JsonObject(
          "type" -> Json.fromString("array"),
          "items" -> Json.fromJsonObject(generateType(ty))
        )
      case TyJsonObject =>
        JsonObject("type" -> Json.fromString("object"))
      case TyEnum(_, variants) =>
        JsonObject(
          "enum" -> Json
            .fromValues(variants.map(_.names.head).map(Json.fromString))
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
      case TyNamed(name) =>
        JsonObject(
          "type" -> Json.fromString("object"),
          "name" -> Json.fromString(name)
        )
    }
}

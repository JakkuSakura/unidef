package unidef.languages.javascript

import com.typesafe.scalalogging.Logger
import io.circe.{Json, JsonObject, ParsingFailure}
import unidef.languages.common._

// meant for private use
case object Required extends KeywordBoolean
case object AdditionalProperties extends KeywordBoolean

case object JsonSchemaCodeGen {
  val logger: Logger = Logger[this.type]

  def generateFuncDecl(func: AstFunctionDecl): String = {
    val struct = TyStruct().setValue(KeyFields, func.parameters)
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
  def jsonObjectOf(ty: String, others: (String, Json)*): JsonObject = {
    JsonObject.fromIterable(
      Seq("type" -> Json.fromString(ty)) ++
        others.map(x => x._1 -> x._2)
    )
  }
  def generateType(ty: TyNode): JsonObject =
    ty match {
      case TyString => jsonObjectOf("string")
      case _: TyInteger =>
        jsonObjectOf("integer")
      case _: TyFloat =>
        jsonObjectOf("float")
      case _: TyNumeric =>
        jsonObjectOf("numeric")
      case TyBoolean =>
        jsonObjectOf("boolean")
      case _: TyDateTime =>
        jsonObjectOf("string", "format" -> Json.fromString("datetime"))

      case t: TyTimeStamp =>
        jsonObjectOf(
          "string",
          "format" -> Json.fromString("timestamp"),
          "unit" -> t
            .getValue(KeyTimeUnit)
            .map(_.toString)
            .map(Json.fromString)
            .getOrElse(Json.Null)
        )

      case TyList(ty) =>
        jsonObjectOf("array", "items" -> Json.fromJsonObject(generateType(ty)))

      case TyJsonObject => jsonObjectOf("object")
      case x @ TyEnum(variants) if x.getValue(KeyName).isDefined =>
        JsonObject(
          "enum" -> Json
            .fromValues(variants.map(_.names.head).map(Json.fromString)),
          "name" -> Json.fromString(x.getValue(KeyName).get)
        )
      case x @ TyEnum(variants) =>
        JsonObject(
          "enum" -> Json
            .fromValues(variants.map(_.names.head).map(Json.fromString))
        )

      case x @ TyStruct() if x.getFields.isDefined =>
        jsonObjectOf(
          "object",
          "properties" -> Json.fromJsonObject(
            JsonObject.fromIterable(
              x.getFields.get
                .map(f => f.name -> Json.fromJsonObject(generateType(f.value)))
            )
          )
        )
      case TyStruct() =>
        jsonObjectOf("object")
      case TyNamed(name) =>
        jsonObjectOf("string", "name" -> Json.fromString(name))
      case TyByteArray =>
        jsonObjectOf("string", "format" -> Json.fromString("byte"))
      case TyInet => jsonObjectOf("string", "format" -> Json.fromString("inet"))
      case TyUuid => jsonObjectOf("string", "format" -> Json.fromString("uuid"))
      case _      => throw new ParsingFailure(s"Unsupported type: $ty", null)
    }
}

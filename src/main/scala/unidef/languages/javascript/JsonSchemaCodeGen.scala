package unidef.languages.javascript

import com.typesafe.scalalogging.Logger
import io.circe.{Json, JsonObject}
import unidef.languages.common._
import unidef.utils.UnidefParseException

// meant for private use
case object KeyRequired extends KeywordBoolean
case object KeyAdditionalProperties extends KeywordBoolean
case object KeyIsMethodParameters extends KeywordBoolean
class JsonSchemaCodeGen(naming: NamingConvention = JsonNamingConvention) {
  val logger: Logger = Logger[this.type]

  def generateFuncDecl(func: AstFunctionDecl): String = {
    val struct = TyStruct().setValue(KeyFields, func.parameters)
    struct.setValue(KeyRequired, true)
    struct.setValue(KeyAdditionalProperties, false)
    struct.setValue(KeyIsMethodParameters, true)
    val obj = generateType(struct)
    //  "required": [
    //  ],
    //  "additionalProperties": false
    obj.spaces2
  }
  def jsonObjectOf(ty: String, others: (String, Json)*): Json = {
    Json.fromJsonObject(
      JsonObject.fromIterable(
        Seq("type" -> Json.fromString(ty)) ++
          others.map(x => x._1 -> x._2)
      )
    )
  }
  def generateType(ty: TyNode): Json =
    ty match {
      case TyString => jsonObjectOf("string")
      case _: TyInteger =>
        jsonObjectOf("integer")
      case _: TyFloat =>
        jsonObjectOf("number")
      case _: TyNumeric =>
        jsonObjectOf("number")
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
        jsonObjectOf("array", "items" -> generateType(ty))

      case x @ TyEnum(variants) if x.getValue(KeyName).isDefined =>
        Json.fromJsonObject(
          JsonObject(
            "enum" -> Json
              .fromValues(
                variants.map(_.names.head).map(naming.toEnumKeyName).map(Json.fromString)
              ),
            "name" -> Json.fromString(naming.toClassName(x.getValue(KeyName).get))
          )
        )
      case x @ TyEnum(variants) =>
        Json.fromJsonObject(
          JsonObject(
            "enum" -> Json
              .fromValues(variants.map(_.names.head).map(naming.toEnumKeyName).map(Json.fromString))
          )
        )

      case x @ TyStruct()
          if x.getFields.isDefined && x.getValue(KeyIsMethodParameters).contains(true) =>
        jsonObjectOf(
          "object",
          "properties" -> Json.fromJsonObject(
            JsonObject.fromIterable(
              x.getFields.get
                .map(f =>
                  naming.toFunctionParameterName(f.name) ->
                    generateType(f.value)
                )
            )
          )
        )
      case x @ TyStruct() if x.getFields.isDefined =>
        jsonObjectOf(
          "object",
          "properties" -> Json.fromJsonObject(
            JsonObject.fromIterable(
              x.getFields.get
                .map(f => naming.toFieldName(f.name) -> generateType(f.value))
            )
          )
        )
      case TyJsonObject | TyStruct() => jsonObjectOf("object")
      case TyJsonAny() =>
        Json.fromValues(
          Seq("number", "string", "boolean", "object", "array", "null").map(Json.fromString)
        )
      case TyNamed(name) =>
        jsonObjectOf("string", "name" -> Json.fromString(name))
      case TyByteArray =>
        jsonObjectOf("string", "format" -> Json.fromString("byte"))
      case TyInet => jsonObjectOf("string", "format" -> Json.fromString("inet"))
      case TyUuid => jsonObjectOf("string", "format" -> Json.fromString("uuid"))
      case _ => throw UnidefParseException(s"Unsupported type: $ty")
    }
}

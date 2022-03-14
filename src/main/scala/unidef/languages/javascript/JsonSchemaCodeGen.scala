package unidef.languages.javascript

import com.typesafe.scalalogging.Logger
import io.circe.{Json, JsonObject}
import unidef.languages.common.*
import unidef.utils.{ParseCodeException, TypeEncodeException}

import scala.collection.mutable

// meant for private use
case object KeyRequired extends KeywordBoolean
case object KeyAdditionalProperties extends KeywordBoolean
case object KeyIsMethodParameters extends KeywordBoolean

class JsonSchemaCodeGenOption(
    val naming: NamingConvention = JsonNamingConvention,
    val useListForJsonAny: Boolean = false,
    val useCustomFormat: Boolean = false
)

class JsonSchemaCodeGen(options: JsonSchemaCodeGenOption = JsonSchemaCodeGenOption())
    extends TypeEncoder[Json] {
  val logger: Logger = Logger[this.type]

  def generateFuncDecl(func: AstFunctionDecl): String = {
    val struct = TyStructImpl(None, Some(func.parameters), None, None)
    struct.setValue(KeyRequired, true)
    struct.setValue(KeyAdditionalProperties, false)
    struct.setValue(KeyIsMethodParameters, true)
    val obj = generateType(struct)
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

  override def encode(ty: TyNode): Option[Json] = ty match {
    case _: TyString => Some(jsonObjectOf("string"))
    case _: TyInteger =>
      Some(jsonObjectOf("integer"))
    case _: TyFloat =>
      Some(jsonObjectOf("number"))
    case _: TyNumeric =>
      Some(jsonObjectOf("number"))
    case _: TyBoolean =>
      Some(jsonObjectOf("boolean"))
    case _: TyDateTime =>
      Some(jsonObjectOf("string", "format" -> Json.fromString("datetime")))

    case t: TyTimeStamp =>
      Some(
        jsonObjectOf(
          "string",
          "format" -> Json.fromString("timestamp"),
          "unit" -> t
            .getValue(KeyTimeUnit)
            .map(_.toString)
            .map(Json.fromString)
            .getOrElse(Json.Null)
        )
      )

    case x: TyList =>
      Some(jsonObjectOf("array", "items" -> generateType(x.getContent.get)))

    case x @ TyEnum(variants) if x.getValue(KeyName).isDefined =>
      Some(
        Json.fromJsonObject(
          JsonObject(
            "enum" -> Json
              .fromValues(
                variants.map(_.names.head).map(options.naming.toEnumValueName).map(Json.fromString)
              ),
            "name" -> Json.fromString(options.naming.toClassName(x.getValue(KeyName).get))
          )
        )
      )
    case x @ TyEnum(variants) =>
      Some(
        Json.fromJsonObject(
          JsonObject(
            "enum" -> Json
              .fromValues(
                variants.map(_.names.head).map(options.naming.toEnumValueName).map(Json.fromString)
              )
          )
        )
      )

    case x: TyStruct with Extendable if x.getFields.isDefined =>
      val naming = if (x.getValue(KeyIsMethodParameters).contains(true)) {
        options.naming.toFunctionParameterName
      } else {
        options.naming.toClassName
      }
      val others: mutable.Map[String, Json] = mutable.Map.empty
      others += "properties" -> Json.fromJsonObject(
        JsonObject.fromIterable(
          x.getFields.get.map(f => naming(f.name) -> generateType(f.value))
        )
      )

      if (x.getValue(KeyAdditionalProperties).contains(false)) {
        others += "additionalProperties" -> Json.False
      }
      if (x.getValue(KeyRequired).contains(true)) {
        others += "required" -> Json.fromValues(
          x.getFields.get
            .map(f => naming(f.name))
            .map(Json.fromString)
        )

      }
      Some(
        jsonObjectOf(
          "object",
          others.toSeq: _*
        )
      )
    case TyJsonObject => Some(jsonObjectOf("object"))
    case _: TyStruct => Some(jsonObjectOf("object"))
    case TyJsonAny() if options.useListForJsonAny =>
      Some(
        Json.fromValues(
          Seq("number", "string", "boolean", "object", "array", "null").map(Json.fromString)
        )
      )
    case TyJsonAny() if !options.useListForJsonAny =>
      Some(Json.fromJsonObject(JsonObject.empty))
    case TyNamed(name) =>
      Some(jsonObjectOf("string", "name" -> Json.fromString(name)))
    case _: TyByteArray =>
      Some(
        jsonObjectOf(
          "string",
          (if (options.useCustomFormat) "format" else "$comment") -> Json.fromString("bytes")
        )
      )
    case TyInet => Some(jsonObjectOf("string", "format" -> Json.fromString("hostname")))
    case TyUuid => Some(jsonObjectOf("string", "format" -> Json.fromString("uuid")))
    case _ => None
  }
  def generateType(ty: TyNode): Json = encodeOrThrow(ty, "json schema")

}

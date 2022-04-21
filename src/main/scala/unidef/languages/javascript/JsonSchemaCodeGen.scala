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

  def generateFuncDecl(func: AstFunctionDecl): Json = {
    val struct = TyStructImpl(None, Some(func.parameters), None, None)
    struct.setValue(KeyIsMethodParameters, true)
    generateType(struct)
  }

  def convertToMatrix(struct0: TyStruct): TyStructImpl = {
    val fields = mutable.ArrayBuffer[TyField]()
    val headers = mutable.ArrayBuffer[String]()
    val body_row = mutable.ArrayBuffer[TyNode]()

    struct0.getFields.get.foreach {
      case TyField(name, x: TyList) =>
        headers += name
        body_row += x.getContent.get
      case TyField(name, x) =>
        fields += TyField(name, x)
    }
    fields += TyField("headers", TyConstTupleString(headers.toSeq))
    fields += TyField("body", TyTupleImpl(Some(body_row.toList)))
    // TODO: add header names and types
    TyStructImpl(None, Some(fields.toList), None, None)
  }

  def jsonObjectOf(ty: String, others: (String, Json)*): Json = {
    Json.fromFields(
      Seq("type" -> Json.fromString(ty))
        ++
          others.map(x => x._1 -> x._2)
    )
  }

  override def encode(ty: TyNode): Option[Json] = {
    val coded = ty match {
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
      case x: TyConstTupleString =>
        Some(
          jsonObjectOf(
            "array",
            "items" -> Json.fromValues(
              x.values.map(name =>
                Json.obj(
                  "const" -> Json.fromString(name)
                )
              )
            ),
            "minItems" -> Json.fromInt(x.values.size),
            "maxItems" -> Json.fromInt(x.values.size)
          )
        )
      case x: TyTuple =>
        Some(jsonObjectOf("array", "items" -> Json.fromValues(x.getValues.get.map(generateType))))

      case x: TyList =>
        Some(jsonObjectOf("array", "items" -> generateType(x.getContent.get)))

      case x @ TyEnum(variants) if x.getValue(KeyName).isDefined =>
        Some(
          Json.obj(
            "enum" -> Json
              .fromValues(
                variants
                  .map(_.names.head)
                  .map(options.naming.toEnumValueName)
                  .map(Json.fromString)
              ),
            "name" -> Json.fromString(options.naming.toClassName(x.getValue(KeyName).get))
          )
        )
      case _ @TyEnum(variants) =>
        Some(
          Json.obj(
            "enum" -> Json
              .fromValues(
                variants
                  .map(_.names.head)
                  .map(options.naming.toEnumValueName)
                  .map(Json.fromString)
              )
          )
        )

      case x: TyStruct with Extendable if x.getFields.isDefined =>
        x.setValue(KeyRequired, true)
        x.setValue(KeyAdditionalProperties, false)
        val naming = if (x.getValue(KeyIsMethodParameters).contains(true)) {
          options.naming.toFunctionParameterName
        } else {
          options.naming.toFieldName
        }
        val others: mutable.Map[String, Json] = mutable.Map.empty
        others += "properties" -> Json.fromFields(
          x.getFields.get.map(f =>
            naming(f.name) -> generateType(f.value match {
              case opt: TyOptional if opt.getContent.isDefined => opt.getContent.get
              case x => x
            })
          )
        )

        if (x.getValue(KeyAdditionalProperties).contains(false)) {
          others += "additionalProperties" -> Json.False
        }
        if (x.getValue(KeyRequired).contains(true)) {
          others += "required" -> Json.fromValues(
            x.getFields.get
              .filterNot(f => f.value.isInstanceOf[TyOptional])
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
      case _: TyAny | TyJsonAny() if options.useListForJsonAny =>
        Some(
          Json.fromValues(
            Seq("number", "string", "boolean", "object", "array", "null").map(Json.fromString)
          )
        )
      case _: TyAny | TyJsonAny() if !options.useListForJsonAny =>
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
      case _: TyInet => Some(jsonObjectOf("string", "format" -> Json.fromString("hostname")))
      case _: TyUuid => Some(jsonObjectOf("string", "format" -> Json.fromString("uuid")))
      case _ => None
    }
    ty match {
      case ty: Extendable if ty.getValue(KeyComment).isDefined =>
        coded
          .flatMap(x => x.asObject)
          .map(x => x.add("$comment", Json.fromString(ty.getValue(KeyComment).get)))
          .map(Json.fromJsonObject)
      case _ => coded
    }
  }
  def generateType(ty: TyNode): Json = {
    val new_ty = ty match {
      case x: TyStructImpl if x.getValue(KeyDataframe).contains(true) =>
        convertToMatrix(x)
      case x => x
    }
    encodeOrThrow(new_ty, "json schema")
  }

}

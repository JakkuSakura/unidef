package unidef.languages.javascript

import unidef.languages.common._

class JsonSchemaCommon(extended: Boolean) extends TypeDecoder {
  def parseType(ty: String): Option[TyNode] =
    ty.toLowerCase match {
      case "integer" if !extended =>
        Some(TyInteger(BitSize.Unknown, signed = true))
      case "string" => Some(TyString)
      case "boolean" => Some(TyBoolean)
      case "number" => Some(TyNumericClass())
      case "object" => Some(TyStruct())
      case "null" => Some(TyNull)
      case "int" | "i32" | "integer" if extended => Some(TyInteger(BitSize.B32))
      case "uint" | "u32" if extended =>
        Some(TyInteger(BitSize.B32, signed = false))
      case "long" | "i64" if extended => Some(TyInteger(BitSize.B64))
      case "ulong" | "u64" if extended =>
        Some(TyInteger(BitSize.B64, signed = false))
      case "float" if extended => Some(TyFloat(BitSize.B32))
      case "double" if extended => Some(TyFloat(BitSize.B64))
      case "str" | "varchar" | "text" if extended => Some(TyString)
      case "json" | "jsonb" => Some(TyJsonObject)
      case "timestamp" if extended =>
        Some(TyTimeStamp())
      case "timestamptz" if extended =>
        Some(TyTimeStamp())
      case "bytea" | "[u8]" if extended => Some(TyByteArray)
      case "bool" if extended => Some(TyBoolean)
      case "dict" if extended => Some(TyStruct())
      case "any" if extended => Some(TyAny)
      case "void" | "unit" if extended => Some(TyUnit)
      case _ => None

    }

  override def decode(name: String): Option[TyNode] = parseType(name)

}

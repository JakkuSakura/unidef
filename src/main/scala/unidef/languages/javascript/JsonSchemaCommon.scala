package unidef.languages.javascript

import unidef.languages.common.*

class JsonSchemaCommon(extended: Boolean) extends TypeDecoder[String] {
  override def decode(name: String): Option[TyNode] = name.toLowerCase match {
    case "integer" if !extended =>
      Some(TyIntegerImpl(None, Some(true)))
    case "string" => Some(TyStringImpl())
    case "boolean" => Some(TyBooleanImpl())
    case "number" => Some(TyNumericImpl())
    case "object" => Some(TyStructImpl(None, None, None, None))
    case "null" => Some(TyNull)
    case "int" | "i32" | "integer" if extended => Some(TyIntegerImpl(Some(BitSize.B32), Some(true)))
    case "uint" | "u32" if extended =>
      Some(TyIntegerImpl(Some(BitSize.B32), Some(false)))
    case "long" | "i64" if extended => Some(TyIntegerImpl(Some(BitSize.B64), Some(true)))
    case "ulong" | "u64" if extended =>
      Some(TyIntegerImpl(Some(BitSize.B64), Some(false)))
    case "i128" if extended => Some(TyIntegerImpl(Some(BitSize.B128), Some(true)))
    case "u128" if extended => Some(TyIntegerImpl(Some(BitSize.B128), Some(false)))
    case "float" if extended => Some(TyFloatImpl(Some(BitSize.B32)))
    case "double" | "f64" if extended => Some(TyFloatImpl(Some(BitSize.B64)))
    case "str" | "varchar" | "text" if extended => Some(TyStringImpl())
    case "json" | "jsonb" => Some(TyJsonAny())
    case "timestamp" if extended =>
      Some(TyTimeStamp())
    case "timestamptz" if extended =>
      Some(TyTimeStamp())
    // TODO really extended only?
    case "array" | "list" if extended => Some(TyListImpl(Some(TyAny)))
    case "option" | "optional" if extended => Some(TyOptionalImpl(Some(TyAny)))
    case "bytea" | "[u8]" if extended => Some(TyByteArrayImpl())
    case "bool" if extended => Some(TyBooleanImpl())
    case "dict" if extended => Some(TyStructImpl(None, None, None, None))
    case "any" if extended => Some(TyAny)
    case "void" | "unit" if extended => Some(TyUnit)
    case _ => None

  }

}

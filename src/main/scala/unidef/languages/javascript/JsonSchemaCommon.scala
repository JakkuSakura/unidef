package unidef.languages.javascript

import unidef.common.ty.*

class JsonSchemaCommon(extended: Boolean) extends TypeDecoder[String] {
  override def decode(name: String): Option[TyNode] = name.toLowerCase match {
    case s"$x?" if extended => decode(x).map(x => TyOptionalImpl(x))
    case s"$x[]" if extended => decode(x).map(x => TyListImpl(x))
    case "integer" if !extended =>
      Some(TyIntegerImpl(None, Some(true)))
    case "string" => Some(TyStringImpl())
    case "boolean" => Some(TyBooleanImpl())
    case "number" => Some(TyNumericImpl())
    case "object" => Some(TyStructBuilder().build())
    case "null" => Some(TyNullImpl())
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
      Some(TyTimeStampBuilder().build())
    case "timestamptz" if extended =>
      Some(TyTimeStampBuilder().hasTimeZone(true).build())
    // TODO really extended only?
    case "array" | "list" if extended => Some(TyListImpl(TyAnyImpl()))
    case "option" | "optional" if extended => Some(TyOptionalImpl(TyAnyImpl()))
    case "bytea" | "[u8]" if extended => Some(TyByteArrayImpl())
    case "bool" if extended => Some(TyBooleanImpl())
    case "dict" if extended => Some(TyStructBuilder().build())
    case "any" if extended => Some(TyAnyImpl())
    case "void" | "unit" if extended => Some(TyUnitImpl())
    case _ => None

  }

}

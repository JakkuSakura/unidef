package unidef.languages.javascript

import unidef.common.ty.*

class JsonSchemaCommon(extended: Boolean) extends TypeDecoder[String] {
  override def decode(name: String): Option[TyNode] = name.toLowerCase match {
    case s"$x?" if extended => decode(x).map(x => Types.option(x))
    case s"$x[]" if extended => decode(x).map(x => Types.list(x))
    case "integer" if !extended =>
      Some(TyIntegerImpl(None, Some(true)))
    case "string" => Some(Types.string())
    case "boolean" => Some(Types.bool())
    case "number" => Some(TyNumericImpl())
    case "object" => Some(TyStructBuilder().build())
    case "null" => Some(TyNullImpl())
    case "int" | "i32" | "integer" if extended => Some(Types.i32())
    case "uint" | "u32" if extended =>
      Some(Types.u32())
    case "long" | "i64" if extended => Some(Types.i64())
    case "ulong" | "u64" if extended =>
      Some(Types.u64())
    case "i128" if extended => Some(Types.i128())
    case "u128" if extended => Some(Types.u128())
    case "float" if extended => Some(Types.f32())
    case "double" | "f64" if extended => Some(Types.f64())
    case "str" | "varchar" | "text" if extended => Some(Types.string())
    case "json" => Some(TyJsonAnyBuilder().isBinary(false).build())
    case "jsonb" => Some(TyJsonAnyBuilder().isBinary(true).build())
    case "timestamp" if extended =>
      Some(TyTimeStampBuilder().hasTimeZone(false).build())
    case "timestamptz" if extended =>
      Some(TyTimeStampBuilder().hasTimeZone(true).build())
    // TODO really extended only?
    case "array" | "list" if extended => Some(Types.list(Types.any()))
    case "option" | "optional" if extended => Some(Types.option(Types.any()))
    case "bytea" | "[u8]" if extended => Some(TyByteArrayImpl())
    case "bool" if extended => Some(Types.bool())
    case "dict" if extended => Some(TyStructBuilder().build())
    case "any" if extended => Some(Types.any())
    case "void" | "unit" if extended => Some(Types.unit())
    case _ => None

  }

}

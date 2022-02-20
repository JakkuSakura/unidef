package unidef.languages.javascript

import io.circe.ParsingFailure
import unidef.languages.common._

object JsonSchemaCommon extends TypeResolver {
  def parseType(ty: String): Either[ParsingFailure, TyNode] =
    ty.toLowerCase match {
      case "integer" => Right(TyInteger(BitSize.Unknown, signed = true))
      case "number"  => Right(TyNumericClass())
      case "object"  => Right(TyStruct(None))
      case "null"    => Right(TyNull)
      case "string"  => Right(TyString)
      case "boolean" => Right(TyBoolean)
      case _         => Left(ParsingFailure(s"Unknown type: $ty", null))
    }

  override def decode(language: String, typeName: String)(
    implicit resolver: TypeResolver
  ): Option[TyNode] = ???

  override def encode(language: String, node: TyNode)(
    implicit resolver: TypeResolver
  ): Option[String] = ???
}
object JsonSchemaExtended extends TypeResolver {
  def parseType(ty: String): Either[ParsingFailure, TyNode] = {
    ty.toLowerCase match {
      case "int" | "i32" | "integer"             => Right(TyInteger(BitSize.B32))
      case "uint" | "u32"                        => Right(TyInteger(BitSize.B32, signed = false))
      case "long" | "i64"                        => Right(TyInteger(BitSize.B64))
      case "ulong" | "u64"                       => Right(TyInteger(BitSize.B64, signed = false))
      case "float"                               => Right(TyFloat(BitSize.B32))
      case "double"                              => Right(TyFloat(BitSize.B64))
      case "number"                              => Right(TyRealClass())
      case "str" | "string" | "varchar" | "text" => Right(TyString)
      case "object"                              => Right(TyStruct(None))
      case "json" | "jsonb"                      => Right(TyJsonObject)
      case "timestamp" =>
        Right(TyTimeStamp())
      case "timestamptz" =>
        Right(TyTimeStamp())
      case "bytea" | "[u8]"   => Right(TyByteArray)
      case "bool" | "boolean" => Right(TyBoolean)
      case "dict"             => Right(TyStruct(None))
      case "null"             => Right(TyNull)
      case _                  => Left(ParsingFailure("Unknown type " + ty, null))

    }
  }

  override def decode(language: String, typeName: String)(
    implicit resolver: TypeResolver
  ): Option[TyNode] = ???

  override def encode(language: String, node: TyNode)(
    implicit resolver: TypeResolver
  ): Option[String] = ???
}

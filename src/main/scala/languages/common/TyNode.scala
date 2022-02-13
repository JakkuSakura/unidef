package com.jeekrs.unidef
package languages.common

import utils.{ExtKey, Extendable}

import io.circe.ParsingFailure

import java.util.TimeZone
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.TimeUnit

/**
  * This is a very generic type model
  */
class TyNode extends Extendable

// scala: Type[A, B, ..]
class GenericType(val generics: List[TyNode]) extends TyNode

// scala: (A, B)
case class TupleType(values: List[TyNode]) extends GenericType(values)

// scala: Option[A]
case class OptionalType(value: TyNode) extends GenericType(List(value))

// scala: Either[A, B] rust: Result<Ok, Err>
case class ResultType(ok: TyNode, err: TyNode)
    extends GenericType(List(ok, err))

// rust: Vec<T>
case class VectorType(value: TyNode) extends GenericType(List(value))

// scala: A -> B
case class MappingType(key: TyNode, value: TyNode)
    extends GenericType(List(key, value))

sealed class BitSize(val bits: Int)

object BitSize {
  case object B256 extends BitSize(256)
  case object B128 extends BitSize(128)
  case object B64 extends BitSize(64)
  case object B32 extends BitSize(32)
  case object B16 extends BitSize(16)
  case object B4 extends BitSize(4)
  case object B2 extends BitSize(2)
  case object B1 extends BitSize(1)
  case object Unknown extends BitSize(0)
  case object BigInt extends BitSize(-1)
}

case class IntegerType(bitSize: BitSize, signed: Boolean = true) extends TyNode

class RealType extends TyNode

// sql: decimal(p, s)
case class DecimalType(precision: Int, scale: Int) extends RealType

// scala: f32, f64
case class FloatType(bitSize: BitSize) extends RealType

// rust: enum with multiple names
case class VariantType(names: List[String]) extends TyNode

case class EnumType(variants: List[VariantType], simple_enum: Boolean = true)
    extends TyNode

case class FieldType(name: String, value: TyNode) extends TyNode

case class StructType(name: String,
                      fields: List[FieldType],
                      dataType: Boolean = false)
    extends TyNode

case class DictType(key: TyNode, value: TyNode)
    extends GenericType(List(key, value))

case class ListType(value: TyNode) extends GenericType(List(value))
case class SetType(value: TyNode) extends GenericType(List(value))

case object JsonObjectType extends TyNode

case object StringType extends TyNode
case object CharType extends TyNode

case object AnyType extends TyNode

case object UnitType extends TyNode

case object NullType extends TyNode

case object UnknownType extends TyNode

case object UndefinedType extends TyNode

case class TimeStampType(timeUnit: TimeUnit, timezone: Boolean = false)
    extends TyNode

case class DateTimeType(timezone: Option[TimeZone]) extends TyNode

case class ReferenceType(referee: TyNode) extends TyNode

case class NamedType(name: String) extends TyNode

case object Mutability extends ExtKey {
  override type V = Boolean
}

case object Derive extends ExtKey {
  override type V = List[String]

}

case object Attributes extends ExtKey {
  override type V = List[FunctionApplyNode]

}

object TypeParser {
  def parse(ty: String): Either[ParsingFailure, TyNode] =
    ty.toLowerCase match {
      case "int" | "i32"                         => Right(IntegerType(BitSize.B32))
      case "uint" | "u32"                        => Right(IntegerType(BitSize.B32, signed = false))
      case "long" | "i64"                        => Right(IntegerType(BitSize.B64))
      case "ulong" | "u64"                       => Right(IntegerType(BitSize.B64, signed = false))
      case "float"                               => Right(FloatType(BitSize.B32))
      case "double"                              => Right(FloatType(BitSize.B64))
      case "str" | "string" | "varchar" | "text" => Right(StringType)
      case "json" | "jsonb"                      => Right(JsonObjectType)
      case "timestamp" =>
        Right(TimeStampType(TimeUnit.MILLISECONDS, timezone = false))
      case "timestamptz" =>
        Right(TimeStampType(TimeUnit.MILLISECONDS, timezone = true))
      case _ => Left(ParsingFailure("Unknown type " + ty, null))

    }

}

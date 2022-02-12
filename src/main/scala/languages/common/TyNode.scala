package com.jeekrs.unidef
package languages.common

import utils.{ExtKey, Extendable}

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.IntType
import io.circe.ParsingFailure

import java.util.TimeZone
import scala.concurrent.duration.TimeUnit

/**
 * This is a very generic type model
 */
class TyNode extends Extendable

// scala: Type[A, B, ..]
class GenericType(generics: List[TyNode]) extends TyNode

// scala: (A, B)
case class TupleType(values: List[TyNode]) extends GenericType(values)

// scala: Option[A]
case class OptionalType(value: TyNode) extends GenericType(List(value))

// scala: Either[A, B] rust: Result<Ok, Err>
case class ResultType(ok: TyNode, err: TyNode) extends GenericType(List(ok, err))

// rust: Vec<T>
case class VectorType(value: TyNode) extends GenericType(List(value))

// scala: A -> B
case class MappingType(key: TyNode, value: TyNode) extends GenericType(List(key, value))

enum BitSize(bits: Int):
    case B256 extends BitSize(256)
    case B128 extends BitSize(128)
    case B64 extends BitSize(64)
    case B32 extends BitSize(32)
    case B16 extends BitSize(16)
    case B4 extends BitSize(4)
    case B2 extends BitSize(2)
    case B1 extends BitSize(1)
    case Unknown extends BitSize(0)
    case BigInt extends BitSize(-1)

case class IntegerType(bitSize: BitSize, signed: Boolean = true) extends TyNode

class RealType extends TyNode

// sql: decimal(p, s)
case class DecimalType(precision: Int, scale: Int) extends RealType

// scala: f32, f64
case class FloatType(bitSize: BitSize) extends RealType

// rust: enum with multiple names
case class VariantType(names: List[String]) extends TyNode

case class EnumType(variants: List[VariantType]) extends TyNode

case class FieldType(name: String, value: TyNode) extends TyNode

case class StructType(name: String, fields: List[FieldType], dataType: Boolean = false) extends TyNode

case class DictType(key: TyNode, value: TyNode) extends GenericType(List(key, value))

case object StringType extends TyNode

case object AnyType extends TyNode

case object UnitType extends TyNode

case object NullType extends TyNode

case object UnknownType extends TyNode

case object UndefinedType extends TyNode

case class TimeStampType(timeUnit: TimeUnit) extends TyNode

case class DateTimeType(timezone: Option[TimeZone]) extends TyNode

case class ReferenceType(referee: TyNode) extends TyNode

case class NamedType(name: String) extends TyNode


case object Mutability extends ExtKey :
    override type V = Boolean

case object Derive extends ExtKey :
    override type V = List[String]

case object Attributes extends ExtKey :
    override type V = List[FunctionApplyNode]

object TypeParser:
    def parse(ty: String): Either[ParsingFailure, TyNode] =
        ty match
            case "int" => Right(IntegerType(BitSize.B32))
            case "str" | "string" => Right(StringType)
            case _ => Left(ParsingFailure("Unknown type " + ty, null))
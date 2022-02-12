package com.jeekrs.unidef
package languages.common

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.IntType
import io.circe.ParsingFailure

import java.util.TimeZone
import scala.concurrent.duration.TimeUnit

/**
 * This is a very generic type model
 */
class TyNode extends IrNode

class GenericType(generics: List[TyNode]) extends TyNode

case class TupleType(values: List[TyNode]) extends GenericType(values)

case class OptionalType(value: TyNode) extends GenericType(List(value))

case class ResultType(ok: TyNode, err: TyNode) extends GenericType(List(ok, err))

case class VectorType(value: TyNode) extends GenericType(List(value))

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

case class DecimalType(precision: Int, scale: Int) extends RealType

case class FloatType(bitSize: BitSize) extends RealType

case class VariantType(names: List[String]) extends TyNode

case class EnumType(variants: List[VariantType]) extends TyNode

case class FieldType(name: String, value: TyNode) extends TyNode

object FieldType:
    object PrimaryKey extends ExtKey :
        override type V = Boolean

    object AutoIncr extends ExtKey :
        override type V = Boolean

case class StructType(name: String, fields: List[FieldType], dataType: Boolean = false) extends TyNode

case class DictType(key: TyNode, value: TyNode) extends GenericType(List(key, value))

object StringType extends TyNode :
    override def toString: String = "String"

object AnyType extends TyNode :
    override def toString: String = "Any"

object UnitType extends TyNode :
    override def toString: String = "Unit"

object NullType extends TyNode :
    override def toString: String = "Null"

object UnknownType extends TyNode :
    override def toString: String = "Unknown"

object UndefinedType extends TyNode :
    override def toString: String = "Undefined"


case class TimeStampType(timeUnit: TimeUnit) extends TyNode

case class DateTimeType(timezone: Option[TimeZone]) extends TyNode

case class ReferenceType(referee: TyNode) extends TyNode

case class NamedType(name: String) extends TyNode

object ReferenceType:
    sealed trait LifeTime

    object LifeTime extends ExtKey :
        override type V = LifeTime

        class StaticLifeTime extends LifeTime

        class NamedLifeTime(name: String) extends LifeTime

object Mutability extends ExtKey :
    override type V = Boolean

object Derive extends ExtKey :
    override type V = List[String]

object Attributes extends ExtKey :
    override type V = List[FunctionApplyNode]

object TypeParser:
    def parse(ty: String): Either[ParsingFailure, TyNode] =
        ty match
            case "int" => Right(IntegerType(BitSize.B32))
            case "str" | "string" => Right(StringType)
            case _ => Left(ParsingFailure("Unknown type " + ty, null))
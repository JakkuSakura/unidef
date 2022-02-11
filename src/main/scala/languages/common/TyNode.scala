package com.jeekrs.unidef
package languages.common

import java.util.TimeZone
import scala.concurrent.duration.TimeUnit

/**
 * This is a very generic type model
 */
class TyNode extends IrNode

class GenericType(generics: List[TyNode]) extends TyNode

class TupleType(values: List[TyNode]) extends GenericType(values)

class OptionalType(value: TyNode) extends GenericType(List(value))

class ResultType(ok: TyNode, err: TyNode) extends GenericType(List(ok, err))

class VectorType(value: TyNode) extends GenericType(List(value))

class MappingType(key: TyNode, value: TyNode) extends GenericType(List(key, value))

enum BitSize(bits: Int):
    case B256 extends BitSize(256)
    case B128 extends BitSize(128)
    case B64 extends BitSize(64)
    case B32 extends BitSize(32)
    case B16 extends BitSize(16)
    case B4 extends BitSize(4)
    case B2 extends BitSize(2)
    case B1 extends BitSize(1)

class IntegerType(bitSize: BitSize, signed: Boolean = true) extends TyNode

class RealType extends TyNode

class DecimalType(precision: Int, scale: Int) extends RealType

class FloatType(bitSize: BitSize) extends RealType

class VariantType(names: List[String]) extends TyNode

class EnumType(variants: List[VariantType]) extends TyNode

class FieldType(name: String, value: TyNode) extends TyNode

object FieldType:
    object PrimaryKey extends ExtKey :
        override type V = Boolean


class StructType(name: String, fields: List[FieldType], dataType: Boolean = false) extends TyNode

class DictType(key: TyNode, value: TyNode) extends GenericType(List(key, value))

object AnyType extends TyNode

object UnitType extends TyNode

object NullType extends TyNode

object UnknownType extends TyNode


class TimeStampType(timeUnit: TimeUnit) extends TyNode

class DateTimeType(timezone: Option[TimeZone]) extends TyNode

class ReferenceType(referee: TyNode) extends TyNode

class NamedType(name: String) extends TyNode

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
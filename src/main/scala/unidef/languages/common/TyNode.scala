package unidef.languages.common

import unidef.languages.sql.SqlCommon.KeySimpleEnum

import java.util.TimeZone

/**
  * This is a very generic type model
  */
trait TyNode
trait TypeBuilder {
  type Type <: TyNode
  def optionalKeys: Seq[Keyword] = Nil
}

// scala: Type[A, B, ..]
class TyGeneric(val generics: Seq[TyNode]) extends TyNode

// scala: (A, B)
case class TyTuple(values: Seq[TyNode]) extends TyGeneric(values)

// scala: Option[A]
case class TyOptional(value: TyNode) extends TyGeneric(Seq(value))

// scala: Either[A, B] rust: Result<Ok, Err>
case class TyResult(ok: TyNode, err: TyNode) extends TyGeneric(Seq(ok, err))

class TyListOf(value: TyNode) extends TyGeneric(Seq(value)) with TyJson

// rust: Vec<T>
case class TyList(value: TyNode) extends TyListOf(value)

case object TyRecord extends TyNode

// scala: A -> B
case class TyMapping(key: TyNode, value: TyNode)
    extends TyGeneric(Seq(key, value))

sealed class BitSize(val bits: Int)

object BitSize {
  case object B256 extends BitSize(256)
  case object B128 extends BitSize(128)
  case object B64 extends BitSize(64)
  case object B32 extends BitSize(32)
  case object B16 extends BitSize(16)
  case object B8 extends BitSize(4)
  case object B1 extends BitSize(1)
  case object Unknown extends BitSize(0)
  case object Unlimited extends BitSize(-1)
}

class TyNumeric extends Extendable with TyNode with TyJson

case class TyNumericClass() extends TyNumeric
case class TyInteger(bitSize: BitSize, signed: Boolean = true) extends TyNumeric

class TyReal extends TyNumeric

case class TyRealClass() extends TyReal
// sql: decimal(p, s)
case class TyDecimal(precision: Option[Int], scale: Option[Int]) extends TyReal

// scala: f32, f64
case class TyFloat(bitSize: BitSize) extends TyReal

// rust: enum with multiple names
case class TyVariant(names: Seq[String]) extends TyNode

case class TyEnum(variants: Seq[TyVariant]) extends Extendable with TyNode
object TyEnum extends TypeBuilder {
  override type Type = TyEnum

  override def optionalKeys: Seq[Keyword] = Seq(KeyName, KeySimpleEnum)
}
case class TyField(name: String, value: TyNode) extends Extendable with TyNode

// None means that fields are unknown
case class TyStruct(fields: Option[Seq[TyField]]) extends Extendable with TyNode
object TyStruct extends TypeBuilder {
  override type Type = TyStruct

  override def optionalKeys: Seq[Keyword] = Seq(KeyName, KeyDataType)
}

case object KeyDataType extends KeywordBoolean

case class TyDict(key: TyNode, value: TyNode) extends TyGeneric(Seq(key, value))
case object TyByteArray extends TyListOf(TyInteger(BitSize.B16))

case class TySet(value: TyNode) extends TyGeneric(Seq(value))

trait TyJson extends TyNode
case object TyJsonAny extends TyJson
case object TyJsonObject extends TyJson // TyStruct(None)
case object TyBoolean extends TyJson
case object TyString extends TyJson

case object TyChar extends TyNode

case object TyAny extends TyNode

case object TyUnit extends TyNode

case object TyNull extends TyNode
case object TyNothing extends TyNode
case object TyUnknown extends TyNode

case object TyUndefined extends TyNode
case object TyInet extends TyNode
case object TyUuid extends TyNode

case class TyLambda(params: TyNode, ret: TyNode) extends TyNode
case class TyTimeStamp() extends Extendable with TyNode
case class TyUnion(types: Seq[TyNode]) extends TyNode

case object KeyTimeUnit extends Keyword {
  override type V = java.util.concurrent.TimeUnit
}

case object KeyHasTimeZone extends KeywordBoolean

case class TyDateTime(timezone: Option[TimeZone]) extends TyNode

case class TyReference(referee: TyNode) extends TyNode

case class TyNamed(name: String) extends TyNode

case object Mutability extends KeywordBoolean

// #[derive(Debug)] in Rust
case object KeyDerive extends Keyword {
  override type V = Array[String]
}

case object KeyAttributes extends Keyword {
  override type V = Seq[AstFunctionApply]

}
case object KeyFields extends KeywordOnly
case object KeyName extends KeywordString
case object KeyType extends KeywordOnly

package unidef.languages.common

import java.util.TimeZone

/**
  * This is a very generic type model
  */
trait TyNode

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

case class TyInteger(bitSize: BitSize, signed: Boolean = true) extends TyNumeric

class TyReal extends TyNumeric

// sql: decimal(p, s)
case class TyDecimal(precision: Option[Int], scale: Option[Int]) extends TyReal

// scala: f32, f64
case class TyFloat(bitSize: BitSize) extends TyReal

// rust: enum with multiple names
case class TyVariant(names: Seq[String]) extends TyNode

case class TyEnum(name: String, variants: Seq[TyVariant])
    extends Extendable
    with TyNode

case class TyField(name: String, value: TyNode) extends Extendable with TyNode

case class TyStruct(fields: Seq[TyField]) extends Extendable with TyNode
case object DataType extends KeywordBoolean

case class TyDict(key: TyNode, value: TyNode) extends TyGeneric(Seq(key, value))
case object TyByteArray extends TyListOf(TyInteger(BitSize.B16))

case class TySet(value: TyNode) extends TyGeneric(Seq(value))

trait TyJson extends TyNode
case object TyJsonAny extends TyJson
case object TyJsonObject extends TyJson
case object TyBoolean extends TyJson
case object TyString extends TyJson

case object TyChar extends TyNode

case object TyAny extends TyNode

case object TyUnit extends TyNode

case object TyNull extends TyNode

case object TyUnknown extends TyNode

case object TyUndefined extends TyNode
case object TyInet extends TyNode
case object TyUuid extends TyNode

case class TyLambda(params: TyNode, ret: TyNode) extends TyNode
case class TyTimeStamp() extends Extendable with TyNode

case object HasTimeUnit extends Keyword {
  override type V = java.util.concurrent.TimeUnit
}

case object HasTimeZone extends KeywordBoolean

case class TyDateTime(timezone: Option[TimeZone]) extends TyNode

case class TyReference(referee: TyNode) extends TyNode

case class TyNamed(name: String) extends TyNode

case object Mutability extends KeywordBoolean

// #[derive(Debug)] in Rust
case object Derive extends Keyword {
  override type V = Array[String]
}

case object Attributes extends Keyword {
  override type V = Seq[AstFunctionApply]

}
case object Fields extends KeywordOnly
case object Name extends KeywordString
case object Type extends KeywordOnly

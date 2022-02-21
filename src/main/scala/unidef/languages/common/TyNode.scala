package unidef.languages.common

import java.util.TimeZone

/**
  * This is a very generic type model
  */
trait TyTypeExpr {
  def asTypeNode: TyNode
}

trait TyNode extends TyTypeExpr {
  def asTypeNode: TyNode = this
}

// everything is generic
trait TyTypeVar extends TyNode

// scala: (A, B, ..)
case class TyTuple(values: Seq[TyNode]) extends TyNode

trait HasValue extends Extendable {
  def getValue: Option[TyNode] = getValue(KeyValue)
}

// scala: Option[A]
case class TyOptional(value: TyNode) extends HasValue with TyNode {
  override def getValue: Option[TyNode] = Some(value)
}

// scala: Either[A, B] rust: Result<Ok, Err>
case class TyResult(ok: TyNode, err: TyNode) extends TyNode

class TyListOf(value: TyNode) extends HasValue with TyJson {
  override def getValue: Option[TyNode] = Some(value)
}

// rust: Vec<T>
case class TyList(value: TyNode) extends TyListOf(value)

case object TyRecord extends TyNode

// scala: A -> B
case class TyMapping(key: TyNode, value: TyNode) extends TyNode

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
case class TyVariant(names: Seq[String], code: Option[Int] = None)
    extends TyNode

case class TyEnum(variants: Seq[TyVariant])
    extends Extendable
    with TyNode
    with HasName
    with HasValue

case class TyField(name: String, value: TyNode) extends Extendable with TyNode

trait TyClass extends TyNode
// None means that fields are unknown
case class TyStruct()
    extends Extendable
    with TyClass
    with HasName
    with HasFields

case object KeyDataType extends KeywordBoolean

case class TyDict(key: TyNode, value: TyNode) extends TyNode
case object TyByteArray extends TyListOf(TyInteger(BitSize.B16))

case class TySet(value: TyNode) extends TyNode

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
trait TyApplicable extends TyNode {
  def parameterType: TyNode
  def returnType: TyNode
}
case class TyLambda(override val parameterType: TyNode,
                    override val returnType: TyNode)
    extends Extendable
    with TyApplicable

object TyFunction {
  def apply(params: Seq[TyNode], ret: TyNode): TyLambda =
    TyLambda(TyTuple(params), ret)
}

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
trait HasFields extends Extendable {
  def getFields: Option[Seq[TyField]] = getValue(KeyFields)
}
case object KeyFields extends Keyword {
  override type V = Seq[TyField]
}
case object KeyProperties extends KeywordOnly
case object KeyType extends KeywordOnly
case object KeyValue extends Keyword {
  override type V = TyNode
}
case object KeySimpleEnum extends KeywordBoolean

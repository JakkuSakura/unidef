package unidef.languages.common

import java.util.TimeZone

trait TyNode

case object TyNode extends TyNode
case class TyApp(ty: TyNode, tyArgs: List[TyNode]) extends TyNode

// everything is generic
trait TyTypeVar extends TyNode

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

// rust: enum with multiple names
case class TyVariant(names: Seq[String], code: Option[Int] = None) extends Extendable with TyNode

case class TyEnum(variants: Seq[TyVariant])
    extends Extendable
    with TyNode
    with HasName
    with HasValue {
  override def getName: Option[String] = getValue(KeyName)
  override def getValue: Option[TyNode] = getValue(KeyValue)
}

case class TyField(name: String, value: TyNode) extends Extendable with TyNode

case object KeyDataType extends KeywordBoolean

case class TyDict(key: TyNode, value: TyNode) extends TyNode

trait TyJson extends TyNode
case class TyJsonAny() extends Extendable with TyJson
case object KeyIsBinary extends KeywordBoolean
case object TyJsonObject extends TyJson // TyStruct(None)

case object TyChar extends TyNode

case object TyAny extends TyNode

case object TyUnit extends TyNode

case object TyNull extends TyNode
case object TyNothing extends TyNode
case object TyUnknown extends TyNode

case object TyUndefined extends TyNode
case object TyInet extends TyNode
//TODO: ipv4/ipv6/ip/hostname/...
case object TyUuid extends TyNode
trait TyApplicable extends TyNode {
  def parameterType: TyNode
  def returnType: TyNode
}
case class TyLambda(override val parameterType: TyNode, override val returnType: TyNode)
    extends Extendable
    with TyApplicable

object TyFunction {
  def apply(params: List[TyNode], ret: TyNode): TyLambda =
    TyLambda(TyTupleImpl(Some(params)), ret)
}

case class TyTimeStamp() extends Extendable with TyNode
case class TyUnion(types: Seq[TyNode]) extends TyNode

case object KeyTimeUnit extends Keyword {
  override type V = java.util.concurrent.TimeUnit
}

case object KeyHasTimeZone extends KeywordBoolean

case class TyDateTime(timezone: Option[TimeZone]) extends TyNode

case class TyReference(referee: TyNode) extends TyNode

case class TyNamed(name: String) extends Extendable with TyNode

case object Mutability extends KeywordBoolean

// #[derive(Debug)] in Rust
case object KeyDerive extends Keyword {
  override type V = Array[String]
}

case object KeyProperties extends KeywordOnly
case object KeyType extends KeywordOnly

case object KeySimpleEnum extends KeywordBoolean

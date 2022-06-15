package unidef.common.ty

import io.circe.{Encoder, Json}
import unidef.common.{BaseNode, Extendable, Keyword, KeywordBoolean, KeywordOnly}

import java.util.TimeZone
import scala.quoted.{Expr, Quotes}

trait TyNode extends BaseNode

// TODO: lift to BaseNode level and rename
trait TyCommentable extends TyNode {
  def getComment: String
  def setComment(comment: String): this.type
}

def exprOption[T](
    exp: Option[T]
)(using quotes: Quotes, toExpr: quoted.ToExpr[T], t: quoted.Type[T]): Expr[Option[T]] = {
  import quotes.reflect.*
  exp match
    case Some(value) => '{ Some(${ Expr(value) }) }
    case None => '{ None }
}
case object TyNode extends TyNode with quoted.ToExpr[TyNode] {
  def apply(ty: TyNode)(using quotes: Quotes): Expr[TyNode] = {
    import quotes.reflect.*
    ???
  }
}
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
case class TyVariant(names: List[String], code: Option[Int] = None, name: Option[String] = None) extends Extendable with TyNode with HasName {
  override def getName: Option[String] = name
}

case class TyEnum(variants: List[TyVariant], simpleEnum: Option[Boolean] = None, name: Option[String] = None, value: Option[TyNode] = None, schema: Option[String] = None)
    extends Extendable
    with TyNode {
  def getName: Option[String] = name
  def getValue: Option[TyNode] = value
}

case object KeyDataType extends KeywordBoolean

case class TyDict(key: TyNode, value: TyNode) extends TyNode

trait TyJson extends TyNode
case class TyJsonAny() extends Extendable with TyJson
case object KeyIsBinary extends KeywordBoolean
case object TyJsonObject extends TyJson // TyStruct(None)

trait TyApplicable extends TyNode {
  def parameterType: TyNode
  def returnType: TyNode
}
case class TyLambda(override val parameterType: TyNode, override val returnType: TyNode)
    extends Extendable
    with TyApplicable

object TyFunction {
  def apply(params: List[TyNode], ret: TyNode): TyLambda =
    TyLambda(TyTupleImpl(params), ret)
}

case class TyTimeStamp(
    hasTimeZone: Option[Boolean] = None,
    timeUnit: Option[java.util.concurrent.TimeUnit] = None
) extends TyNode
case class TyUnion(types: List[TyNode]) extends TyNode

case class TyDateTime(timezone: Option[TimeZone]) extends TyNode

case class TyReference(referee: TyNode) extends TyNode

case class TyNamed(name: String) extends Extendable with TyNode

case class TyConstTupleString(values: List[String]) extends TyNode

// #[derive(Debug)] in Rust
case object KeyDerive extends Keyword {
  override type V = Array[String]
}

case object KeyProperties extends KeywordOnly
case object KeyType extends KeywordOnly

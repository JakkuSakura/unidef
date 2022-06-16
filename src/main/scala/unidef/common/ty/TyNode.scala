package unidef.common.ty

import io.circe.{Encoder, Json}
import unidef.common.BaseNode

import java.util.TimeZone
import scala.quoted.{Expr, Quotes}

trait TyNode extends BaseNode

case object TyNode extends TyNode

trait TyUnknown extends TyNode
case object TyUnknown extends TyUnknown
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

case class TyConstTupleString(values: List[String]) extends TyNode

package unidef.common.ty

import io.circe.{Encoder, Json}
import unidef.common.{BaseNode, Extendable, Keyword, KeywordBoolean, KeywordOnly}

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

trait TyJson extends TyNode
case class TyJsonAny() extends Extendable with TyJson
case object KeyIsBinary extends KeywordBoolean
case object TyJsonObject extends TyJson // TyStruct(None)

case class TyConstTupleString(values: List[String]) extends TyNode

// #[derive(Debug)] in Rust
case object KeyDerive extends Keyword {
  override type V = Array[String]
}

case object KeyProperties extends KeywordOnly
case object KeyType extends KeywordOnly

package unidef.common.ty

import unidef.common.BaseNode

import java.util.TimeZone

trait TyNode extends BaseNode

case object TyNode extends TyNode

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
  case object Native extends BitSize(-2)
}

case class TyConstTupleString(values: List[String]) extends TyNode

sealed trait LifeTime

case object LifeTime {

  case object StaticLifeTime extends LifeTime

  case class NamedLifeTime(name: String) extends LifeTime
}

object Types {
  def named(name: String): TyNamed = TyNamedBuilder().ref(name).build()
  def i32(): TyInteger = TyIntegerBuilder().bitSize(BitSize.B32).signed(true).build()

  def string(): TyString = TyStringBuilder().build()
  def unit(): TyUnit = TyUnitBuilder().build()
  def bool(): TyBoolean = TyBooleanBuilder().build()

  def list(ty: TyNode): TyList = TyListBuilder().value(ty).build()

}
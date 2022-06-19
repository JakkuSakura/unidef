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
  private def integer(bitSize: BitSize, signed: Boolean) =
    TyIntegerBuilder().bitSize(bitSize).signed(signed).build()

  def i8(): TyInteger = integer(BitSize.B8, true)
  def i16(): TyInteger = integer(BitSize.B16, true)
  def i32(): TyInteger = integer(BitSize.B32, true)
  def i64(): TyInteger = integer(BitSize.B64, true)
  def i128(): TyInteger = integer(BitSize.B128, true)

  def u8(): TyInteger = integer(BitSize.B8, true)
    def u16(): TyInteger = integer(BitSize.B16, true)
  def u32(): TyInteger = integer(BitSize.B32, false)
  def u64(): TyInteger = integer(BitSize.B64, false)
  def u128(): TyInteger = integer(BitSize.B128, false)
  def isize(): TyInteger = integer(BitSize.Native, true)
  def usize(): TyInteger = integer(BitSize.Native, false)
  def f32(): TyFloat = TyFloatBuilder().bitSize(BitSize.B32).build()
  def f64(): TyFloat = TyFloatBuilder().bitSize(BitSize.B64).build()

  def string(): TyString = TyStringBuilder().build()
  def unit(): TyUnit = TyUnitBuilder().build()
  def any(): TyAny = TyAnyBuilder().build()
  def bool(): TyBoolean = TyBooleanBuilder().build()

  def list(ty: TyNode): TyList = TyListBuilder().value(ty).build()
  def map(key: TyNode, value: TyNode, mapType: String = ""): TyMap =
    TyMapBuilder().key(key).value(value).mapType(Option(mapType).filterNot(_.isEmpty)).build()

  def option(ty: TyNode): TyOption = TyOptionBuilder().value(ty).build()

}

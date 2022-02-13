package com.jeekrs.unidef
package languages.rust

import utils.ExtKey

sealed trait LifeTime

object LifeTime extends ExtKey {
  override type V = LifeTime

  case object StaticLifeTime extends LifeTime

  case class NamedLifeTime(name: String) extends LifeTime
}

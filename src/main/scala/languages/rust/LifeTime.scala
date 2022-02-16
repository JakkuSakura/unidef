package com.jeekrs.unidef
package languages.rust

import languages.common.Keyword

sealed trait LifeTime

case object LifeTime extends Keyword {
  override type V = LifeTime

  case object StaticLifeTime extends LifeTime

  case class NamedLifeTime(name: String) extends LifeTime
}

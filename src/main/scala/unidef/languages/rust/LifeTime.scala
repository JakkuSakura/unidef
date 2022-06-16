package unidef.languages.rust

sealed trait LifeTime

case object LifeTime {

  case object StaticLifeTime extends LifeTime

  case class NamedLifeTime(name: String) extends LifeTime
}

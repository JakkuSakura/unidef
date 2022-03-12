package unidef.scalai

import scala.tasty.inspector.TastyInspector

object InspectTasty {
  def main(args: Array[String]): Unit =
    val tastyFiles = List("examples/scala_parser$package.tasty")
    TastyInspector.inspectTastyFiles(tastyFiles)(new ScalaiTastyLifter)
}

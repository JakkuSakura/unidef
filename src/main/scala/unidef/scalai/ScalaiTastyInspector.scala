package unidef.scalai

import scala.quoted.*
import scala.tasty.inspector.*

class ScalaiTastyInspector extends Inspector:
  def inspect(using Quotes)(tastys: List[Tasty[quotes.type]]): Unit =
    import quotes.reflect.*
    for tasty <- tastys do
      val tree = tasty.ast

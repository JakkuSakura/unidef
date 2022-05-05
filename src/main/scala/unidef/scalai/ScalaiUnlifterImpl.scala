package unidef.scalai

import com.typesafe.scalalogging.Logger
import unidef.languages.common.AstNode

import scala.quoted.{Expr, Quotes}

class ScalaiUnlifterImpl(using val quotes: Quotes) {
  import quotes.reflect.*

  val logger = Logger[this.type]

  def unlift(x: AstNode): Tree = ???

}

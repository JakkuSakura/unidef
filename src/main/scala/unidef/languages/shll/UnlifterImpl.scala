package unidef.languages.shll

import com.typesafe.scalalogging.Logger
import unidef.common.ast.AstNode

import scala.quoted.{Expr, Quotes}

class UnlifterImpl(using val quotes: Quotes) {
  import quotes.reflect.*

  val logger = Logger[this.type]

  def unlift(x: AstNode): Tree = ???

}

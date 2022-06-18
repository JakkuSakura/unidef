package unidef.languages.shll

import unidef.common.ast.*

class PrettyPrinter {
  def print(n: AstNode): String = {
    n match {
      case n: AstLiteralString =>
        n.literalString
      case n: AstLiteralInt =>
        n.literalInt.toString
      case n: AstLiteralUnit =>
        "()"
      case n: AstLiteralNone =>
        "None"
      case n: AstLiteralNull =>
        "null"
    }
  }
}

object PrettyPrinter {
  def print(n: AstNode): Unit = {
    println(PrettyPrinter().print(n))
  }
}
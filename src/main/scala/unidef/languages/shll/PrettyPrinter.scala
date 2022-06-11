package unidef.languages.shll

import unidef.common.ast.{AstLiteral, AstNode}

class PrettyPrinter {
  def print(n: AstNode): String = {
    n match {
      case n: AstLiteral =>
        n.getLiteralValue.get
    }
  }
}

object PrettyPrinter {
  def print(n: AstNode): Unit = {
    println(PrettyPrinter().print(n))
  }
}
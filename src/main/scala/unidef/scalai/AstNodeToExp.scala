package unidef.scalai

import unidef.languages.common.*

import scala.quoted.{Expr, Quotes}

object AstNodeToExpr extends quoted.ToExpr[AstNode] {
  def apply(x: AstNode)(using Quotes): Expr[AstNode] = AstNodeToExprImpl().toExprAst(x)
}
class AstNodeToExprImpl(using val quotes: Quotes) {
  import quotes.reflect.*
  def toExprType(x: TyNode): Expr[TyNode] = {
    x match {
      // TODO: extendable
      case TyNamed(name) => '{ TyNamed(${ Expr(name) }) }
    }
  }
  def toExprAst(node: AstNode): Expr[AstNode] = {
    node match {
      case AstUnit => '{ AstUnit }
      case AstNull => '{ AstNull }
      case AstUndefined => '{ AstUndefined }
      case AstLiteralInteger(v) => '{ AstLiteralInteger(${ Expr(v) }) }
      case AstTyped(typed) => '{ AstTyped(${ toExprType(typed) }) }
    }
  }
}

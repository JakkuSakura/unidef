package unidef.scalai

import com.typesafe.scalalogging.Logger
import unidef.languages.common.*

import scala.collection.mutable
import scala.quoted.*
import scala.tasty.inspector.*

class ScalaiTastyHelper extends Inspector {
  val stmts = mutable.ArrayBuffer[AstNode]()
  def getAstNode: AstProgram = {
    val ast = AstProgram(stmts.toList)
    stmts.clear()
    ast
  }
  def inspect(using Quotes)(tastys: List[Tasty[quotes.type]]): Unit = {
    val lifter = ScalaiLifterImpl()
    for (tasty <- tastys) {
      val tree = tasty.ast
      stmts ++= lifter.liftPackageClause(tree.asInstanceOf[lifter.quotes.reflect.PackageClause])
    }
  }

}
def liftImpl[T](x: Expr[T])(using Quotes, quoted.Type[T]): AstNode = {
  import quotes.reflect.*
  val tree: Term = x.asTerm
  val lifter = ScalaiLifterImpl()
  val value = lifter.liftTree(tree.asInstanceOf[lifter.quotes.reflect.Tree])
  value
}

class AstNodeToExpr extends quoted.ToExpr[AstNode] {
  def apply(x: AstNode)(using Quotes): Expr[AstNode] = ???
  // TODO: serialize and deserialize
}
def liftQuotedImpl[T](x: Expr[T])(using Quotes, quoted.Type[T]): Expr[AstNode] = {
  val toExpr = AstNodeToExpr()
  toExpr(liftImpl(x))
}

def unliftImpl[T](x: AstNode)(using Quotes, quoted.Type[T]): Expr[T] = {
  import quotes.reflect.*
  val unlifter = ScalaiUnlifterImpl()
  val tree = unlifter.unlift(x)
  val value = tree.asExprOf[T]
  value
}

def liftAndUnliftImpl[T](x: Expr[T])(using Quotes, quoted.Type[T]): Expr[T] = {
  val ast = liftImpl(x)
  val unlifted = unliftImpl(ast)
  unlifted
}
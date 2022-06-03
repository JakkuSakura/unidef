package unidef.languages.shll

import com.typesafe.scalalogging.Logger
import unidef.common.ast.{AstNode, AstProgram}


import scala.collection.mutable
import scala.quoted.*
import scala.tasty.inspector.*

class TastyHelper extends Inspector {
  val stmts = mutable.ArrayBuffer[AstNode]()
  def getAstNode: AstProgram = {
    val ast = AstProgram(stmts.toList)
    stmts.clear()
    ast
  }
  def inspect(using Quotes)(tastys: List[Tasty[quotes.type]]): Unit = {
    val lifter = LifterImpl()
    for (tasty <- tastys) {
      val tree = tasty.ast
      stmts ++= lifter.liftPackageClause(tree.asInstanceOf[lifter.quotes.reflect.PackageClause])
    }
  }

}
def liftImpl[T](x: Expr[T])(using Quotes, quoted.Type[T]): AstNode = {
  import quotes.reflect.*
  val tree: Term = x.asTerm
  val lifter = LifterImpl()
  val value = lifter.liftTree(tree.asInstanceOf[lifter.quotes.reflect.Tree])
  value
}


def stageImpl[T](x: Expr[T])(using quotes: Quotes, t: quoted.Type[T]): Expr[String] = {
  import quotes.reflect.*
  Expr(x.asTerm.show(using Printer.TreeShortCode))
}

def unliftImpl[T](x: AstNode)(using Quotes, quoted.Type[T]): Expr[T] = {
  import quotes.reflect.*
  val unlifter = UnlifterImpl()
  val tree = unlifter.unlift(x)
  val value = tree.asExprOf[T]
  value
}

def liftAndUnliftImpl[T](x: Expr[T])(using Quotes, quoted.Type[T]): Expr[T] = {
  val ast = liftImpl(x)
  val unlifted = unliftImpl(ast)
  unlifted
}

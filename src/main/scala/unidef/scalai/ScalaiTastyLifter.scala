package unidef.scalai

import com.typesafe.scalalogging.Logger

import scala.quoted.*
import scala.tasty.inspector.*

class ScalaiTastyLifter extends Inspector {
  val logger = Logger[this.type]
  def inspect(using Quotes)(tastys: List[Tasty[quotes.type]]): Unit = {
    import quotes.reflect.*
    for (tasty <- tastys) {
      val tree = tasty.ast
      liftPackageClause(tree.asInstanceOf[quotes.reflect.PackageClause])
    }
  }
  def liftPackageClause(using Quotes)(tree: quotes.reflect.PackageClause): Unit = {
    import quotes.reflect._
    tree match {
      case PackageClause(pid, stats) =>
        for (s <- stats) {
          liftStmt(s.asInstanceOf[Statement])
        }
    }
  }

  def liftStmt(using Quotes)(tree: quotes.reflect.Statement): Unit = {
    import quotes.reflect._
    logger.debug(tree.show(using Printer.TreeStructure))
    tree match {
      case d @ ValDef(_, _, _) =>

      case ClassDef(name, defDef, parents, sefl, body) =>
        liftClassDef(name, defDef, parents, sefl, body)
    }
  }
  def liftClassDef(using
      Quotes
  )(
      name: String,
      head: quotes.reflect.DefDef,
      parents: List[quotes.reflect.Tree],
      sefl: Option[quotes.reflect.ValDef],
      body: List[quotes.reflect.Statement]
  ): Unit = {
    import quotes.reflect._
    logger.debug("name " + name)
    logger.debug("head " + head)
    logger.debug("parents " + parents)
    logger.debug("sefl " + sefl)
    logger.debug("body " + body)
  }
}

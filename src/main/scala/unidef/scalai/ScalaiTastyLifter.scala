package unidef.scalai

import com.typesafe.scalalogging.Logger
import unidef.languages.common.{AstImport, AstNode, AstProgram, AstRawCode, AstStatement, AstUnit}

import scala.collection.mutable
import scala.quoted.*
import scala.tasty.inspector.*

class ScalaiTastyLifter extends Inspector {
  val logger = Logger[this.type]
  val stmts = mutable.ArrayBuffer[AstNode]()
  def getAstNode: AstProgram = {
    val ast = AstProgram(stmts.toSeq)
    stmts.clear()
    ast
  }
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
  def liftImport(using Quotes)(tree: quotes.reflect.Import): Unit = {
    import quotes.reflect._
    tree match {
      case Import(expr, List(name)) =>
        stmts += AstImport(expr.show + "." + name)
    }
  }
  def liftValDef(using Quotes)(tree: quotes.reflect.ValDef): Unit = {
    import quotes.reflect._
    tree match {
      case ValDef(name, tpt, rhs) =>
        stmts += AstRawCode(s"val $name: ${tpt.show} = ${rhs}")
    }
  }
  def liftStmt(using Quotes)(tree: quotes.reflect.Statement): Unit = {
    import quotes.reflect._
    logger.debug(tree.show(using Printer.TreeStructure))
    tree match {
      case i @ Import(_, _) => liftImport(i)
      case d @ ValDef(_, _, _) => liftValDef(d)

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

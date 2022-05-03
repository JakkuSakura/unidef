package unidef.scalai

import com.typesafe.scalalogging.Logger
import unidef.languages.common.*

import scala.collection.mutable
import scala.quoted.*
import scala.tasty.inspector.*

class ScalaiTastyLifter extends Inspector {
  val logger = Logger[this.type]
  val stmts = mutable.ArrayBuffer[AstNode]()
  def getAstNode: AstProgram = {
    val ast = AstProgram(stmts.toList)
    stmts.clear()
    ast
  }
  def inspect(using Quotes)(tastys: List[Tasty[quotes.type]]): Unit = {
    import quotes.reflect.*
    for (tasty <- tastys) {
      val tree = tasty.ast
      stmts ++= liftPackageClause(tree.asInstanceOf[quotes.reflect.PackageClause])
    }
  }
  def liftPackageClause(using Quotes)(tree: quotes.reflect.PackageClause): List[AstNode] = {
    import quotes.reflect.*
    val stmts = mutable.ArrayBuffer[AstNode]()

    tree match {
      case PackageClause(pid, stats) =>
        for (s <- stats) {
          stmts += liftStmt(s.asInstanceOf[Statement])
        }
    }
    stmts.toList
  }
  def liftImport(using Quotes)(tree: quotes.reflect.Import): AstImport = {
    import quotes.reflect.*
    tree match {
      case Import(expr, List(name)) =>
        AstImport(expr.show + "." + name)
    }
  }
  def liftValDef(using Quotes)(tree: quotes.reflect.ValDef): AstRawCode = {
    import quotes.reflect.*
    tree match {
      case ValDef(name, tpt, rhs) =>
        AstRawCode(s"val $name: ${tpt.show} = ${rhs}")
    }
  }
  def liftStmt(using Quotes)(tree: quotes.reflect.Statement): AstNode = {
    import quotes.reflect.*
    logger.debug(tree.show(using Printer.TreeStructure))
    tree match {
      case i @ Import(_, _) => liftImport(i)
      case d @ ValDef(_, _, _) => liftValDef(d)

      case ClassDef(name, defDef, parents, sefl, body) =>
        liftClassDef(name, defDef, parents, sefl, body)

      case x =>
        logger.error(s"Unsupported statement: ${x.show}")
        AstRawCode(x.show)
    }
  }
  def liftParameter(using Quotes)(tree: quotes.reflect.ValDef): TyField = {
    import quotes.reflect.*
    logger.debug(tree.toString)

    tree match {
      case ValDef(name, tpt, rhs) =>
        // TODO: process tpt and rhs
        TyField(name, TyNamed(tpt.toString))
    }
  }
  // TODO support currying
  def extractParams(using Quotes)(params: List[quotes.reflect.ParamClause]):
      (List[quotes.reflect.TypeDef], List[quotes.reflect.ValDef]) = {
    val (tyParams, dynParams) = params match {
      case Nil => (Nil, Nil)
      case quotes.reflect.TypeParamClause(ts) :: Nil => (ts, Nil, Nil)
      case quotes.reflect.TypeParamClause(ts) :: quotes.reflect.TermParamClause(pr) :: Nil => (ts, pr)
      case quotes.reflect.TermParamClause(pr) :: Nil => (Nil, pr)
      case _ => throw Exception(s"Illegal parameter list specification " + params)
    }
    (tyParams, dynParams)
  }

  def liftType(using Quotes)(tree: quotes.reflect.TypeTree): TyNode = {
    import quotes.reflect.*
    // TODO
    TyAnyImpl()
  }
  def liftMethodDef(using Quotes)(name: String, params: List[quotes.reflect.ParamClause], ty: quotes.reflect.TypeTree, term: Option[quotes.reflect.Term]): AstFunctionDecl = {
    import quotes.reflect.*
    val (tys, paramss) =  extractParams(params)

    val retType = liftType(ty)
    val body = term.map(liftStmt)
    AstFunctionDecl(AstLiteralString(name), paramss.map(liftParameter), retType).trySetValue(KeyBody, body)
  }
  def liftClassBodyStmt(using
      Quotes
  )(tree: quotes.reflect.Statement): Option[AstFunctionDecl] = {
    import quotes.reflect.*
    logger.debug(tree.show(using Printer.TreeStructure))
    tree match {
      case DefDef(name, param, ty, term) =>
        Some(liftMethodDef(name, param, ty, term))
      case t @ TypeDef(name, ty) =>
        None
    }
  }
  def liftClassDefHeadFields(using Quotes)(tree: quotes.reflect.DefDef): List[TyField] = {
    import quotes.reflect.*
    // TODO
    Nil
  }
  def liftClassDefParents(using Quotes)(tree: List[quotes.reflect.Tree]): List[AstClassIdent] = {
    import quotes.reflect.*
    // TODO
    Nil
  }

  def liftClassDef(using
      Quotes
  )(
      name: String,
      head: quotes.reflect.DefDef,
      parents: List[quotes.reflect.Tree],
      sefl: Option[quotes.reflect.ValDef],
      body: List[quotes.reflect.Statement]
  ): AstClassDecl = {
    import quotes.reflect.*
    logger.debug("name " + name)
    logger.debug("head " + head)
    logger.debug("parents " + parents)
    logger.debug("sefl " + sefl)
    logger.debug("body " + body)
    val stmts: List[AstFunctionDecl] = body.flatMap(x => liftClassBodyStmt(x)).toList

    val fields = liftClassDefHeadFields(head)
    val derived = liftClassDefParents(parents)
    AstClassDecl(
      AstLiteralString(name),
      fields,
      stmts,
      derived
    )
  }
}

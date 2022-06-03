package unidef.languages.shll

import com.typesafe.scalalogging.Logger
import unidef.common.ty.*
import unidef.common.ast.*
import scala.collection.mutable
import scala.quoted.Quotes

class LifterImpl(using val quotes: Quotes) {
  import quotes.reflect.*

  val logger = Logger[this.type]

  def liftTree(tree: Tree): AstNode = {
    logger.debug(tree.show(using Printer.TreeStructure))
    tree match {
      case Inlined(_, _, Literal(IntConstant(v))) => AstLiteralImpl(Some(v.toString), Some(TyIntegerImpl(Some(BitSize.B32), Some(true))))

    }
  }
  def liftPackageClause(tree: PackageClause): List[AstNode] = {
    val stmts = mutable.ArrayBuffer[AstNode]()

    tree match {
      case PackageClause(pid, stats) =>
        for (s <- stats) {
          stmts += liftStmt(s.asInstanceOf[Statement])
        }
    }
    stmts.toList
  }
  def liftImport(tree: Import): AstImport = {
    tree match {
      case Import(expr, List(name)) =>
        AstImport(expr.show + "." + name)
    }
  }
  def liftValDef(tree: ValDef): AstRawCode = {
    tree match {
      case ValDef(name, tpt, rhs) =>
        AstRawCodeImpl(Some(s"val $name: ${tpt.show} = ${rhs}"), None)
    }
  }
  def liftStmt(tree: Statement): AstNode = {
    logger.debug(tree.show(using Printer.TreeStructure))
    tree match {
      case i @ Import(_, _) => liftImport(i)
      case d @ ValDef(_, _, _) => liftValDef(d)

      case ClassDef(name, defDef, parents, sefl, body) =>
        liftClassDef(name, defDef, parents, sefl, body)
      case Apply(fun, args) => ???
      case x =>
        logger.error(s"Unsupported statement: ${x.show}")
        AstRawCodeImpl(Some(x.show), None)
    }
  }
  def liftParameter(tree: ValDef): TyField = {
    logger.debug(tree.toString)

    tree match {
      case ValDef(name, tpt, rhs) =>
        // TODO: process tpt and rhs
        TyField(name, TyNamed(tpt.toString))
    }
  }
  // TODO support currying
  def extractParams(params: List[ParamClause]): (List[TypeDef], List[ValDef]) = {
    val (tyParams, dynParams) = params match {
      case Nil => (Nil, Nil)
      case TypeParamClause(ts) :: Nil => (ts, Nil, Nil)
      case TypeParamClause(ts) :: TermParamClause(pr) :: Nil => (ts, pr)
      case TermParamClause(pr) :: Nil => (Nil, pr)
      case _ => throw Exception(s"Illegal parameter list specification " + params)
    }
    (tyParams, dynParams)
  }

  def liftType(tree: TypeTree): TyNode = {
    // TODO
    TyAnyImpl()
  }
  def liftMethodDef(
      name: String,
      params: List[ParamClause],
      ty: TypeTree,
      term: Option[Term]
  ): AstFunctionDecl = {
    val (tys, paramss) = extractParams(params)
    // TODO
    val retType = liftType(ty)
    val body = term.map(liftStmt)
    AstFunctionDecl(AstLiteralString(name), paramss.map(liftParameter), retType)
      .trySetValue(KeyBody, body)
  }
  def liftClassBodyStmt(tree: Statement): Option[AstFunctionDecl] = {
    logger.debug(tree.show(using Printer.TreeStructure))
    tree match {
      case DefDef(name, param, ty, term) =>
        Some(liftMethodDef(name, param, ty, term))
      case t @ TypeDef(name, ty) =>
        None
    }
  }
  def liftClassDefHeadFields(tree: DefDef): List[TyField] = {
    // TODO
    Nil
  }
  def liftClassDefParents(tree: List[Tree]): List[AstClassIdent] = {
    // TODO
    Nil
  }

  def liftClassDef(
      name: String,
      head: DefDef,
      parents: List[Tree],
      sefl: Option[ValDef],
      body: List[Statement]
  ): AstClassDecl = {
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

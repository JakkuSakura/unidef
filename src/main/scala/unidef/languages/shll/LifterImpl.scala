package unidef.languages.shll

import com.typesafe.scalalogging.Logger
import unidef.common.ty.*
import unidef.common.ast.*
import unidef.languages.scala.ScalaCommon

import scala.collection.mutable
import scala.quoted.Quotes

case class LiftException(msg: String) extends Exception(msg)

class LifterImpl(using val quotes: Quotes) {
  import quotes.reflect.*

  val logger = Logger[this.type]
  val common = ScalaCommon()
  // tree is everything
  def liftTree(tree: Tree): AstNode = {
    logger.debug("lift tree " + tree.show(using Printer.TreeStructure))
    ???
  }
  // term is expression
  def liftTerm(term: Term): AstNode = {
    logger.debug("lift term " + term.show(using Printer.TreeStructure))
    term match {
      case Block(Nil, x) => liftTerm(x)
      case Block(decls, Literal(UnitConstant())) => AstDeclsImpl(decls.flatMap(liftDecl))
      case Block(decls, x) => throw LiftException(s"Unsupported term: ${x.show}")
      case Apply(fun, args) =>
        AstApplyImpl(
          liftTerm(fun),
          Asts.arguments(args.map(liftTerm).zipWithIndex.map { case (v, i) =>
            AstArgumentImpl(i.toString, Some(v))
          })
        )
      case Literal(UnitConstant()) => AstLiteralUnitImpl()
      case Literal(IntConstant(v)) =>
        AstLiteralIntImpl(v)
      case Literal(StringConstant(v)) => AstLiteralStringImpl(v)

      case Ident(name) =>
        AstIdentImpl(name)
      case Inlined(_, _, x) =>
        liftTree(x)
      case x => throw LiftException(s"Unsupported term: ${x.show}")
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

  def liftValDef(tree: ValDef): AstValDef = {
    tree match {
      case ValDef(name, tpt, rhs) =>
        AstValDefBuilder().name(name).ty(liftType(tpt)).value(rhs.map(liftTerm)).build()
    }
  }
  def liftStmt(tree: Statement): AstNode = {
    logger.debug("lift stmt " + tree.show(using Printer.TreeStructure))
    tree match {
      case i @ Import(_, _) => liftImport(i)
      case d @ ValDef(_, _, _) => liftValDef(d)

      case ClassDef(name, defDef, parents, sefl, body) =>
        liftClassDef(name, defDef, parents, sefl, body)

      case Block(stmts, expr) => AstBlockImpl(stmts.map(liftStmt) :+ liftStmt(expr))
      case x: Term => liftTerm(x)
      case x =>
        logger.error(s"unsupported statement: ${x.show} \n${x}")
        AstRawCodeImpl(x.show, None)
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

  def liftType(tree: TypeTree): TyNode =
    tree match {
      case Ident(ty) =>
        common.decodeOrThrow(ty, "scala")
      case x =>
        common.decodeOrThrow(x.show, "scala")
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
    AstFunctionDeclBuilder()
      .name(name)
      .parameters(Asts.parameters(paramss.map(liftValDef)))
      .returnType(retType)
      .body(body)
      .build()
  }
  def liftDecl(tree: Statement): Option[AstNode] = {
    logger.debug("liftDecl", tree.show(using Printer.TreeStructure))
    tree match {
      case DefDef(name, param, ty, term) =>
        Some(liftMethodDef(name, param, ty, term))
      case ValDef(name, ty, value) =>
        Some(AstValDefBuilder().name(name).ty(liftType(ty)).value(value.map(liftTerm)).build())
      case t @ TypeDef(name, ty) =>
        None
    }
  }
  def liftClassDefHeadFields(tree: DefDef): List[AstValDef] = {
    // TODO
    Nil
  }
  def liftClassDefParents(tree: List[Tree]): List[AstIdent] = {
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
    val stmts = mutable.ArrayBuffer[AstFunctionDecl]()
    val valDefs = mutable.ArrayBuffer[AstValDef]()

    body.flatMap(x => liftDecl(x)).foreach {
      case x: AstFunctionDecl => stmts += x
      case x: AstValDef => valDefs += x
    }

    liftClassDefHeadFields(head).foreach { case x: TyField =>
      valDefs += x
    }

    val derived = liftClassDefParents(parents)
    AstClassDeclBuilder()
      .name(name)
      .parameters(Asts.parameters(Nil))
      .fields(valDefs.toList)
      .methods(stmts.toList)
      . derives(derived)
      .build()

  }
}

package unidef.languages.shll

import unidef.common.ast.*
import com.typesafe.scalalogging.Logger
import unidef.common.ty.TyNode

import scala.collection.mutable
case class SpecializeException(msg: String, node: AstNode) extends Exception(msg + ": " + node)
case class ValueContext(
    values: Map[String, AstNode] = Map.empty,
    tyValues: Map[String, TyNode] = Map.empty,
    parent: Option[ValueContext] = None
) {
  def getValue(name: String): Option[AstNode] = {
    values.get(name).orElse(parent.flatMap(_.getValue(name)))
  }

  def getTyValue(name: String): Option[TyNode] = {
    tyValues.get(name).orElse(parent.flatMap(_.getTyValue(name)))
  }
}
object ValueContext {
  def empty: ValueContext = ValueContext()
  def from(
      parent: ValueContext,
      values: Map[String, AstNode] = Map.empty,
      tyValues: Map[String, TyNode] = Map.empty
  ): ValueContext = {
    ValueContext(values, tyValues, Some(parent))

  }
}
case class SpecializeCache(
    funcDeclMap: mutable.HashMap[String, AstFunctionDecl] = mutable.HashMap.empty,
    clsDeclMap: mutable.HashMap[String, AstClassDecl] = mutable.HashMap.empty,
    specializedFunctions: mutable.HashMap[String, AstFunctionDecl] = mutable.HashMap.empty,
    specializeId: mutable.HashMap[String, Int] = mutable.HashMap.empty
) {
  def getAndIncrSpecializeId(name: String): Int = {
    specializeId.get(name) match {
      case Some(id) =>
        val newId = id + 1
        specializeId += (name -> newId)
        newId
      case None =>
        val newId = 0
        specializeId += (name -> newId)
        newId
    }
  }
}

case class Specializer() {
  var logger: Logger = Logger[this.type]
  var cache: SpecializeCache = SpecializeCache()
  def specialize(n: AstNode): AstNode = {
    cache = SpecializeCache()
    val v = specializeNode(n, ValueContext())
    val specialized = cache.specializedFunctions.values.toList
    if (specialized.isEmpty) {
      v
    } else {
      AstDeclsImpl(specialized ::: v :: Nil)
    }
  }

  def specializeNode(n: AstNode, ctx: ValueContext): AstNode = {
    logger.debug("Specializing " + n)
    n match {
      case d: AstFunctionDecl => specializeFunctionDecl(d, ctx)
      case n: AstBlock => specializeBlock(n, ctx)
      case ds: AstDecls => AstDeclsImpl(ds.decls.map(specializeDecl(_, ctx)))
      case n: AstApply => specializeApply(n, ctx)
      case n: AstIdent => specializeIdent(n, ctx)
      case n: AstLiteral => n
      case x => throw SpecializeException("cannot specialize", x)
    }

  }

  def specializeIdent(id: AstIdent, ctx: ValueContext): AstNode = {
    ctx.getValue(id.name).get // TODO specialize with function arguments
  }

  def specializeApply(n: AstApply, ctx: ValueContext): AstNode = {
    n.applicant match {
      case id: AstIdent if cache.funcDeclMap.contains(id.name) =>
        val func = cache.funcDeclMap(id.name)

        specializeFunctionApply(func, n.arguments, ctx)
      case _ =>
        val f = specializeNode(n.applicant, ctx)
        // TODO process arguments specially
        val args = Asts.flattenArguments(n.arguments).map(specializeNode(_, ctx))
        AstApplyImpl(
          f,
          Asts.arguments(args.zipWithIndex.map({ case (a, i) =>
            AstArgumentImpl(i.toString, Some(a))
          }))
        )
    }

  }
  def specializeBlock(d: AstBlock, ctx: ValueContext): AstBlock = {
    AstBlockImpl(d.stmts.map(specializeNode(_, ctx)))
  }

  def specializeDecl(d: AstNode, ctx: ValueContext): AstNode = {
    logger.debug("Specializing decl " + d)
    d match {
      case d: AstFunctionDecl => specializeFunctionDecl(d, ctx)
      case c: AstClassDecl => specializeClassDecl(c, ctx)
      case _ => throw SpecializeException("cannot specialize ", d)
    }
  }
  def specializeClassDecl(c: AstClassDecl, ctx: ValueContext): AstClassDecl = {
    cache.clsDeclMap(c.name) = c
    c
  }
  def isSpecializedFunctionDecl(d: AstFunctionDecl): Boolean = {
    Asts.flattenParameters(d.parameters).isEmpty && d.body.isDefined
  }

  def specializeFunctionApply(
      func: AstFunctionDecl,
      args: AstArgumentLists,
      ctx: ValueContext
  ): AstApply = {
    val parameters = Asts.flattenParameters(func.parameters)
    val flatArgs = Asts
      .flattenArguments(args)
      .map(x => AstArgumentImpl(x.name, x.value.map(specializeNode(_, ctx))))
    val mapping = flatArgs
      .map { a =>
        a.name -> a
      }
      .map { case (k, v) =>
        k.toIntOption match {
          case Some(x) => parameters(x).name -> v
          case None => k -> v
        }
      }

    val body = specializeFunctionBody(
      func,
      ValueContext.from(ctx, values = mapping.toMap)
    )
    val newFunc = func
      .asInstanceOf[AstFunctionDeclImpl]
      .copy(name = func.name + "_" + cache.getAndIncrSpecializeId(func.name), body = Some(body))
    cache.specializedFunctions(newFunc.name) = newFunc
    AstApplyImpl(AstIdentImpl(newFunc.name), Asts.arguments(Nil))
  }
  def specializeFunctionBody(
      d: AstFunctionDecl,
      ctx: ValueContext
  ): AstNode = {
    val body = specializeNode(d.body.get, ctx)
    body
  }

  def specializeFunctionDecl(
      d: AstFunctionDecl,
      ctx: ValueContext
  ): AstFunctionDecl = {
    cache.funcDeclMap(d.name) = d
    if (isSpecializedFunctionDecl(d))
      // TODO evaluate contestant
      val body = specializeFunctionBody(d, ctx)
      d.asInstanceOf[AstFunctionDeclImpl].copy(body = Some(body))
    else {
      d
    }
  }
}

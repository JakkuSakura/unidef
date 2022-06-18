package unidef.languages.shll

import unidef.common.ast.*
import com.typesafe.scalalogging.Logger

import scala.collection.mutable
case class SpecializeException(msg: String) extends Exception(msg)
case class SpecializeContext (
                               funcDeclMap: mutable.HashMap[String, AstFunctionDecl] = mutable.HashMap.empty,
                               clsDeclMap: mutable.HashMap[String, AstClassDecl] = mutable.HashMap.empty,
                             )
case class Specializer () {
  var logger: Logger = Logger[this.type]
  var ctx: SpecializeContext = SpecializeContext()
  def specialize(n: AstNode): AstNode = {
    ctx = SpecializeContext()
    specializeImpl(n)
  }

  def specializeImpl(n: AstNode): AstNode = {
    logger.debug("Specializing " + n)
    n match {
      case d: AstFunctionDecl => specializeFunctionDecl(d)
      case ds: AstDecls => AstDeclsImpl(ds.decls.map(specializeDecl))
      case x => throw SpecializeException("cannot specialize " + x)
    }

  }
  def specializeDecl(d: AstNode): AstNode = {
    logger.debug("Specializing decl " + d)
    d match {
      case d: AstFunctionDecl => specializeFunctionDecl(d)
      case c: AstClassDecl => specializeClassDecl(c)
      case _ => throw SpecializeException("cannot specialize " + d)
    }
  }
  def specializeClassDecl(c: AstClassDecl): AstClassDecl = {
    ctx.clsDeclMap(c.name) = c
    c
  }
//  def isSpecializedFunctionDecl(d: AstFunctionDecl): Boolean = {
//  
//  }
  def specializeFunctionDecl(d: AstFunctionDecl): AstFunctionDecl = {
    ctx.funcDeclMap(d.name) = d
    d
  }
}

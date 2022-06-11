package unidef.common.ast

import unidef.common.ty.*


trait HasExpr extends AstNode {
  def getExpr: Option[AstNode]
}
trait HasAlternative extends AstNode {
  def getAlternative: Option[AstNode]
}
trait HasLanguage extends AstNode {
  def getLanguage: Option[String]
}
trait HasSymbol extends AstNode {
  def getSymbol: Option[String]
}
trait HasNodes extends AstNode {
  def getNodes: Option[List[AstNode]]
}
trait HasValue extends AstNode {
  def getValue: Option[AstNode]
}
trait HasApplicable extends AstNode {
  def getApplicable: Option[AstNode]
}
trait HasMutability extends AstNode {
  def getMutability: Option[Boolean]
}
trait HasArguments extends AstNode {
  def getArguments: Option[List[AstNode]]
}
trait HasTy extends AstNode {
  def getTy: Option[TyNode]
}
trait HasName extends AstNode {
  def getName: Option[String]
}
trait HasCode extends AstNode {
  def getCode: Option[String]
}
trait HasLiteralValue extends AstNode {
  def getLiteralValue: Option[String]
}
trait HasDecls extends AstNode {
  def getDecls: Option[List[AstNode]]
}
trait HasQualifier extends AstNode {
  def getQualifier: Option[AstNode]
}
trait HasTest extends AstNode {
  def getTest: Option[AstNode]
}
trait HasFlow extends AstNode {
  def getFlow: Option[FlowControl]
}
trait HasConsequent extends AstNode {
  def getConsequent: Option[AstNode]
}
class AstAwaitImpl(val expr: Option[AstNode]) extends AstAwait {
  override def getExpr: Option[AstNode] = {
    expr
  }
}
class AstIfImpl(val test: Option[AstNode], val consequent: Option[AstNode], val alternative: Option[AstNode]) extends AstIf {
  override def getTest: Option[AstNode] = {
    test
  }
  override def getConsequent: Option[AstNode] = {
    consequent
  }
  override def getAlternative: Option[AstNode] = {
    alternative
  }
}
class AstFlowControlImpl(val flow: Option[FlowControl], val value: Option[AstNode]) extends AstFlowControl {
  override def getFlow: Option[FlowControl] = {
    flow
  }
  override def getValue: Option[AstNode] = {
    value
  }
}
class AstStatementImpl(val expr: Option[AstNode]) extends AstStatement {
  override def getExpr: Option[AstNode] = {
    expr
  }
}
class AstNullImpl() extends AstNull 
class AstSelectImpl(val qualifier: Option[AstNode], val symbol: Option[String]) extends AstSelect {
  override def getQualifier: Option[AstNode] = {
    qualifier
  }
  override def getSymbol: Option[String] = {
    symbol
  }
}
class AstBlockImpl(val nodes: Option[List[AstNode]]) extends AstBlock {
  override def getNodes: Option[List[AstNode]] = {
    nodes
  }
}
class AstApplyImpl(val applicable: Option[AstNode], val arguments: Option[List[AstNode]]) extends AstApply {
  override def getApplicable: Option[AstNode] = {
    applicable
  }
  override def getArguments: Option[List[AstNode]] = {
    arguments
  }
}
class AstDeclsImpl(val decls: Option[List[AstNode]]) extends AstDecls {
  override def getDecls: Option[List[AstNode]] = {
    decls
  }
}
class AstLiteralImpl(val literal_value: Option[String], val ty: Option[TyNode]) extends AstLiteral {
  override def getLiteralValue: Option[String] = {
    literal_value
  }
  override def getTy: Option[TyNode] = {
    ty
  }
}
class AstUnitImpl() extends AstUnit 
class AstValDefImpl(val name: Option[String], val ty: Option[TyNode], val value: Option[AstNode], val mutability: Option[Boolean]) extends AstValDef {
  override def getName: Option[String] = {
    name
  }
  override def getTy: Option[TyNode] = {
    ty
  }
  override def getValue: Option[AstNode] = {
    value
  }
  override def getMutability: Option[Boolean] = {
    mutability
  }
}
class AstRawCodeImpl(val code: Option[String], val language: Option[String]) extends AstRawCode {
  override def getCode: Option[String] = {
    code
  }
  override def getLanguage: Option[String] = {
    language
  }
}
class AstUndefinedImpl() extends AstUndefined 
trait AstAwait extends AstNode with HasExpr {
  def getExpr: Option[AstNode]
}
trait AstIf extends AstNode with HasTest with HasConsequent with HasAlternative {
  def getTest: Option[AstNode]
  def getConsequent: Option[AstNode]
  def getAlternative: Option[AstNode]
}
trait AstFlowControl extends AstNode with HasFlow with HasValue {
  def getFlow: Option[FlowControl]
  def getValue: Option[AstNode]
}
trait AstStatement extends AstNode with HasExpr {
  def getExpr: Option[AstNode]
}
trait AstNull extends AstNode 
trait AstSelect extends AstNode with HasQualifier with HasSymbol {
  def getQualifier: Option[AstNode]
  def getSymbol: Option[String]
}
trait AstBlock extends AstNode with HasNodes {
  def getNodes: Option[List[AstNode]]
}
trait AstApply extends AstNode with HasApplicable with HasArguments {
  def getApplicable: Option[AstNode]
  def getArguments: Option[List[AstNode]]
}
trait AstDecls extends AstNode with HasDecls {
  def getDecls: Option[List[AstNode]]
}
trait AstLiteral extends AstNode with HasLiteralValue with HasTy {
  def getLiteralValue: Option[String]
  def getTy: Option[TyNode]
}
trait AstUnit extends AstNode 
trait AstValDef extends AstNode with HasName with HasTy with HasValue with HasMutability {
  def getName: Option[String]
  def getTy: Option[TyNode]
  def getValue: Option[AstNode]
  def getMutability: Option[Boolean]
}
trait AstRawCode extends AstNode with HasCode with HasLanguage {
  def getCode: Option[String]
  def getLanguage: Option[String]
}
trait AstUndefined extends AstNode 
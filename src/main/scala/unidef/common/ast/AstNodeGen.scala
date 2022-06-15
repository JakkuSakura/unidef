package unidef.common.ast

import unidef.common.ty.*


trait HasLanguage() extends AstNode {
  def getLanguage: Option[String]
}
trait HasAlternative() extends AstNode {
  def getAlternative: Option[AstNode]
}
trait HasCode() extends AstNode {
  def getCode: String
}
trait HasSymbol() extends AstNode {
  def getSymbol: String
}
trait HasFlow() extends AstNode {
  def getFlow: Option[FlowControl]
}
trait HasTest() extends AstNode {
  def getTest: Option[AstNode]
}
trait HasNodes() extends AstNode {
  def getNodes: Option[List[AstNode]]
}
trait HasExpr() extends AstNode {
  def getExpr: AstNode
}
trait HasTy() extends AstNode {
  def getTy: TyNode
}
trait HasMutability() extends AstNode {
  def getMutability: Option[Boolean]
}
trait HasName() extends AstNode {
  def getName: String
}
trait HasDecls() extends AstNode {
  def getDecls: List[AstNode]
}
trait HasConsequent() extends AstNode {
  def getConsequent: Option[AstNode]
}
trait HasApplicable() extends AstNode {
  def getApplicable: AstNode
}
trait HasQualifier() extends AstNode {
  def getQualifier: AstNode
}
trait HasLiteralValue() extends AstNode {
  def getLiteralValue: String
}
trait HasValue() extends AstNode {
  def getValue: Option[AstNode]
}
trait HasArguments() extends AstNode {
  def getArguments: List[AstNode]
}
class AstAwaitImpl(val expr: AstNode) extends AstAwait {
  override def getExpr: AstNode = {
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
class AstStatementImpl(val expr: AstNode) extends AstStatement {
  override def getExpr: AstNode = {
    expr
  }
}
class AstNullImpl() extends AstNull 
class AstSelectImpl(val qualifier: AstNode, val symbol: String) extends AstSelect {
  override def getQualifier: AstNode = {
    qualifier
  }
  override def getSymbol: String = {
    symbol
  }
}
class AstBlockImpl(val nodes: Option[List[AstNode]]) extends AstBlock {
  override def getNodes: Option[List[AstNode]] = {
    nodes
  }
}
class AstApplyImpl(val applicable: AstNode, val arguments: List[AstNode]) extends AstApply {
  override def getApplicable: AstNode = {
    applicable
  }
  override def getArguments: List[AstNode] = {
    arguments
  }
}
class AstDeclsImpl(val decls: List[AstNode]) extends AstDecls {
  override def getDecls: List[AstNode] = {
    decls
  }
}
class AstLiteralImpl(val literal_value: String, val ty: TyNode) extends AstLiteral {
  override def getLiteralValue: String = {
    literal_value
  }
  override def getTy: TyNode = {
    ty
  }
}
class AstUnitImpl() extends AstUnit 
class AstValDefImpl(val name: String, val ty: TyNode, val value: Option[AstNode], val mutability: Option[Boolean]) extends AstValDef {
  override def getName: String = {
    name
  }
  override def getTy: TyNode = {
    ty
  }
  override def getValue: Option[AstNode] = {
    value
  }
  override def getMutability: Option[Boolean] = {
    mutability
  }
}
class AstRawCodeImpl(val code: String, val language: Option[String]) extends AstRawCode {
  override def getCode: String = {
    code
  }
  override def getLanguage: Option[String] = {
    language
  }
}
class AstUndefinedImpl() extends AstUndefined 
trait AstAwait() extends AstNode with HasExpr {
  def getExpr: AstNode
}
trait AstIf() extends AstNode with HasTest with HasConsequent with HasAlternative {
  def getTest: Option[AstNode]
  def getConsequent: Option[AstNode]
  def getAlternative: Option[AstNode]
}
trait AstFlowControl() extends AstNode with HasFlow with HasValue {
  def getFlow: Option[FlowControl]
  def getValue: Option[AstNode]
}
trait AstStatement() extends AstNode with HasExpr {
  def getExpr: AstNode
}
trait AstNull() extends AstNode 
trait AstSelect() extends AstNode with HasQualifier with HasSymbol {
  def getQualifier: AstNode
  def getSymbol: String
}
trait AstBlock() extends AstNode with HasNodes {
  def getNodes: Option[List[AstNode]]
}
trait AstApply() extends AstNode with HasApplicable with HasArguments {
  def getApplicable: AstNode
  def getArguments: List[AstNode]
}
trait AstDecls() extends AstNode with HasDecls {
  def getDecls: List[AstNode]
}
trait AstLiteral() extends AstNode with HasLiteralValue with HasTy {
  def getLiteralValue: String
  def getTy: TyNode
}
trait AstUnit() extends AstNode 
trait AstValDef() extends AstNode with HasName with HasTy with HasValue with HasMutability {
  def getName: String
  def getTy: TyNode
  def getValue: Option[AstNode]
  def getMutability: Option[Boolean]
}
trait AstRawCode() extends AstNode with HasCode with HasLanguage {
  def getCode: String
  def getLanguage: Option[String]
}
trait AstUndefined() extends AstNode 
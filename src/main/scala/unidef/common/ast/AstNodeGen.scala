package unidef.common.ast

import unidef.common.ty.*


trait HasLanguage() extends AstNode {
  def language: Option[String]
}
trait HasAlternative() extends AstNode {
  def alternative: Option[AstNode]
}
trait HasCode() extends AstNode {
  def code: String
}
trait HasSymbol() extends AstNode {
  def symbol: String
}
trait HasFlow() extends AstNode {
  def flow: Option[FlowControl]
}
trait HasTest() extends AstNode {
  def test: Option[AstNode]
}
trait HasNodes() extends AstNode {
  def nodes: Option[List[AstNode]]
}
trait HasExpr() extends AstNode {
  def expr: AstNode
}
trait HasTy() extends AstNode {
  def ty: TyNode
}
trait HasMutability() extends AstNode {
  def mutability: Option[Boolean]
}
trait HasName() extends AstNode {
  def name: String
}
trait HasDecls() extends AstNode {
  def decls: List[AstNode]
}
trait HasConsequent() extends AstNode {
  def consequent: Option[AstNode]
}
trait HasApplicable() extends AstNode {
  def applicable: AstNode
}
trait HasQualifier() extends AstNode {
  def qualifier: AstNode
}
trait HasLiteralValue() extends AstNode {
  def literalValue: String
}
trait HasValue() extends AstNode {
  def value: Option[AstNode]
}
trait HasArguments() extends AstNode {
  def arguments: List[AstNode]
}
class AstAwaitImpl(val expr: AstNode) extends AstAwait {

}
class AstIfImpl(val test: Option[AstNode], val consequent: Option[AstNode], val alternative: Option[AstNode]) extends AstIf {

}
class AstFlowControlImpl(val flow: Option[FlowControl], val value: Option[AstNode]) extends AstFlowControl {

}
class AstStatementImpl(val expr: AstNode) extends AstStatement {

}
class AstNullImpl() extends AstNull
class AstSelectImpl(val qualifier: AstNode, val symbol: String) extends AstSelect {

}
class AstBlockImpl(val nodes: Option[List[AstNode]]) extends AstBlock {

}
class AstApplyImpl(val applicable: AstNode, val arguments: List[AstNode]) extends AstApply {

}
class AstDeclsImpl(val decls: List[AstNode]) extends AstDecls {

}
class AstLiteralImpl(val literalValue: String, val ty: TyNode) extends AstLiteral {

}
class AstUnitImpl() extends AstUnit
class AstValDefImpl(val name: String, val ty: TyNode, val value: Option[AstNode], val mutability: Option[Boolean]) extends AstValDef {

}
class AstRawCodeImpl(val code: String, val language: Option[String]) extends AstRawCode {

}
class AstUndefinedImpl() extends AstUndefined
trait AstAwait() extends AstNode with HasExpr {
  def expr: AstNode
}
trait AstIf() extends AstNode with HasTest with HasConsequent with HasAlternative {
  def test: Option[AstNode]
  def consequent: Option[AstNode]
  def alternative: Option[AstNode]
}
trait AstFlowControl() extends AstNode with HasFlow with HasValue {
  def flow: Option[FlowControl]
  def value: Option[AstNode]
}
trait AstStatement() extends AstNode with HasExpr {
  def expr: AstNode
}
trait AstNull() extends AstNode
trait AstSelect() extends AstNode with HasQualifier with HasSymbol {
  def qualifier: AstNode
  def symbol: String
}
trait AstBlock() extends AstNode with HasNodes {
  def nodes: Option[List[AstNode]]
}
trait AstApply() extends AstNode with HasApplicable with HasArguments {
  def applicable: AstNode
  def arguments: List[AstNode]
}
trait AstDecls() extends AstNode with HasDecls {
  def decls: List[AstNode]
}
trait AstLiteral() extends AstNode with HasLiteralValue with HasTy {
  def literalValue: String
  def ty: TyNode
}
trait AstUnit() extends AstNode
trait AstValDef() extends AstNode with HasName with HasTy with HasValue with HasMutability {
  def name: String
  def ty: TyNode
  def value: Option[AstNode]
  def mutability: Option[Boolean]
}
trait AstRawCode() extends AstNode with HasCode with HasLanguage {
  def code: String
  def language: Option[String]
}
trait AstUndefined() extends AstNode 
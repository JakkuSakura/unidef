package unidef.common.ast

import unidef.common.ty.*


trait HasTest() extends AstNode {
  def test: Option[AstNode]
}
trait HasApplicable() extends AstNode {
  def applicable: AstNode
}
trait HasFlow() extends AstNode {
  def flow: Option[FlowControl]
}
trait HasMutability() extends AstNode {
  def mutability: Option[Boolean]
}
trait HasCode() extends AstNode {
  def code: String
}
trait HasValue() extends AstNode {
  def value: Option[AstNode]
}
trait HasQualifier() extends AstNode {
  def qualifier: AstNode
}
trait HasLanguage() extends AstNode {
  def language: Option[String]
}
trait HasLiteralValue() extends AstNode {
  def literalValue: String
}
trait HasArguments() extends AstNode {
  def arguments: List[AstNode]
}
trait HasDecls() extends AstNode {
  def decls: List[AstNode]
}
trait HasSymbol() extends AstNode {
  def symbol: String
}
trait HasAlternative() extends AstNode {
  def alternative: Option[AstNode]
}
trait HasName() extends AstNode {
  def name: String
}
trait HasConsequent() extends AstNode {
  def consequent: Option[AstNode]
}
trait HasTy() extends AstNode {
  def ty: TyNode
}
trait HasNodes() extends AstNode {
  def nodes: Option[List[AstNode]]
}
trait HasExpr() extends AstNode {
  def expr: AstNode
}
class AstAwaitImpl(val expr: AstNode) extends AstAwait 
class AstIfImpl(val test: Option[AstNode], val consequent: Option[AstNode], val alternative: Option[AstNode]) extends AstIf 
class AstFlowControlImpl(val flow: Option[FlowControl], val value: Option[AstNode]) extends AstFlowControl 
class AstStatementImpl(val expr: AstNode) extends AstStatement 
class AstNullImpl() extends AstNull 
class AstSelectImpl(val qualifier: AstNode, val symbol: String) extends AstSelect 
class AstBlockImpl(val nodes: Option[List[AstNode]]) extends AstBlock 
class AstApplyImpl(val applicable: AstNode, val arguments: List[AstNode]) extends AstApply 
class AstDeclsImpl(val decls: List[AstNode]) extends AstDecls 
class AstLiteralImpl(val literalValue: String, val ty: TyNode) extends AstLiteral 
class AstUnitImpl() extends AstUnit 
class AstValDefImpl(val name: String, val ty: TyNode, val value: Option[AstNode], val mutability: Option[Boolean]) extends AstValDef 
class AstRawCodeImpl(val code: String, val language: Option[String]) extends AstRawCode 
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
class AstAwaitBuilder() {
  var expr: Option[AstNode] = None
  def expr(expr: AstNode): AstAwaitBuilder = {
    this.expr = Some(expr)
    this
  }
  def build(): AstAwaitImpl = {
    AstAwaitImpl(expr.get)
  }
}
class AstIfBuilder() {
  var test: Option[AstNode] = None
  var consequent: Option[AstNode] = None
  var alternative: Option[AstNode] = None
  def test(test: AstNode): AstIfBuilder = {
    this.test = Some(test)
    this
  }
  def test(test: Option[AstNode]): AstIfBuilder = {
    this.test = test
    this
  }
  def consequent(consequent: AstNode): AstIfBuilder = {
    this.consequent = Some(consequent)
    this
  }
  def consequent(consequent: Option[AstNode]): AstIfBuilder = {
    this.consequent = consequent
    this
  }
  def alternative(alternative: AstNode): AstIfBuilder = {
    this.alternative = Some(alternative)
    this
  }
  def alternative(alternative: Option[AstNode]): AstIfBuilder = {
    this.alternative = alternative
    this
  }
  def build(): AstIfImpl = {
    AstIfImpl(test, consequent, alternative)
  }
}
class AstFlowControlBuilder() {
  var flow: Option[FlowControl] = None
  var value: Option[AstNode] = None
  def flow(flow: FlowControl): AstFlowControlBuilder = {
    this.flow = Some(flow)
    this
  }
  def flow(flow: Option[FlowControl]): AstFlowControlBuilder = {
    this.flow = flow
    this
  }
  def value(value: AstNode): AstFlowControlBuilder = {
    this.value = Some(value)
    this
  }
  def value(value: Option[AstNode]): AstFlowControlBuilder = {
    this.value = value
    this
  }
  def build(): AstFlowControlImpl = {
    AstFlowControlImpl(flow, value)
  }
}
class AstStatementBuilder() {
  var expr: Option[AstNode] = None
  def expr(expr: AstNode): AstStatementBuilder = {
    this.expr = Some(expr)
    this
  }
  def build(): AstStatementImpl = {
    AstStatementImpl(expr.get)
  }
}
class AstNullBuilder() {
  def build(): AstNullImpl = {
    AstNullImpl()
  }
}
class AstSelectBuilder() {
  var qualifier: Option[AstNode] = None
  var symbol: Option[String] = None
  def qualifier(qualifier: AstNode): AstSelectBuilder = {
    this.qualifier = Some(qualifier)
    this
  }
  def symbol(symbol: String): AstSelectBuilder = {
    this.symbol = Some(symbol)
    this
  }
  def build(): AstSelectImpl = {
    AstSelectImpl(qualifier.get, symbol.get)
  }
}
class AstBlockBuilder() {
  var nodes: Option[List[AstNode]] = None
  def nodes(nodes: List[AstNode]): AstBlockBuilder = {
    this.nodes = Some(nodes)
    this
  }
  def nodes(nodes: Option[List[AstNode]]): AstBlockBuilder = {
    this.nodes = nodes
    this
  }
  def build(): AstBlockImpl = {
    AstBlockImpl(nodes)
  }
}
class AstApplyBuilder() {
  var applicable: Option[AstNode] = None
  var arguments: Option[List[AstNode]] = None
  def applicable(applicable: AstNode): AstApplyBuilder = {
    this.applicable = Some(applicable)
    this
  }
  def arguments(arguments: List[AstNode]): AstApplyBuilder = {
    this.arguments = Some(arguments)
    this
  }
  def build(): AstApplyImpl = {
    AstApplyImpl(applicable.get, arguments.get)
  }
}
class AstDeclsBuilder() {
  var decls: Option[List[AstNode]] = None
  def decls(decls: List[AstNode]): AstDeclsBuilder = {
    this.decls = Some(decls)
    this
  }
  def build(): AstDeclsImpl = {
    AstDeclsImpl(decls.get)
  }
}
class AstLiteralBuilder() {
  var literalValue: Option[String] = None
  var ty: Option[TyNode] = None
  def literalValue(literalValue: String): AstLiteralBuilder = {
    this.literalValue = Some(literalValue)
    this
  }
  def ty(ty: TyNode): AstLiteralBuilder = {
    this.ty = Some(ty)
    this
  }
  def build(): AstLiteralImpl = {
    AstLiteralImpl(literalValue.get, ty.get)
  }
}
class AstUnitBuilder() {
  def build(): AstUnitImpl = {
    AstUnitImpl()
  }
}
class AstValDefBuilder() {
  var name: Option[String] = None
  var ty: Option[TyNode] = None
  var value: Option[AstNode] = None
  var mutability: Option[Boolean] = None
  def name(name: String): AstValDefBuilder = {
    this.name = Some(name)
    this
  }
  def ty(ty: TyNode): AstValDefBuilder = {
    this.ty = Some(ty)
    this
  }
  def value(value: AstNode): AstValDefBuilder = {
    this.value = Some(value)
    this
  }
  def value(value: Option[AstNode]): AstValDefBuilder = {
    this.value = value
    this
  }
  def mutability(mutability: Boolean): AstValDefBuilder = {
    this.mutability = Some(mutability)
    this
  }
  def mutability(mutability: Option[Boolean]): AstValDefBuilder = {
    this.mutability = mutability
    this
  }
  def build(): AstValDefImpl = {
    AstValDefImpl(name.get, ty.get, value, mutability)
  }
}
class AstRawCodeBuilder() {
  var code: Option[String] = None
  var language: Option[String] = None
  def code(code: String): AstRawCodeBuilder = {
    this.code = Some(code)
    this
  }
  def language(language: String): AstRawCodeBuilder = {
    this.language = Some(language)
    this
  }
  def language(language: Option[String]): AstRawCodeBuilder = {
    this.language = language
    this
  }
  def build(): AstRawCodeImpl = {
    AstRawCodeImpl(code.get, language)
  }
}
class AstUndefinedBuilder() {
  def build(): AstUndefinedImpl = {
    AstUndefinedImpl()
  }
}
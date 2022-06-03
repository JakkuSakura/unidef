package unidef.common.ast

import unidef.common.ty.*

trait HasFlow extends AstNode {
  def getFlow: Option[FlowControl]
}

trait HasTy extends AstNode {
  def getTy: Option[TyNode]
}

trait HasTest extends AstNode {
  def getTest: Option[AstNode]
}

trait HasQualifier extends AstNode {
  def getQualifier: Option[AstNode]
}

trait HasAlternative extends AstNode {
  def getAlternative: Option[AstNode]
}

trait HasExpr extends AstNode {
  def getExpr: Option[AstNode]
}

trait HasSymbol extends AstNode {
  def getSymbol: Option[String]
}

trait HasCode extends AstNode {
  def getCode: Option[String]
}

trait HasValue extends AstNode {
  def getValue: Option[AstNode]
}

trait HasLiteralValue extends AstNode {
  def getLiteralValue: Option[String]
}

trait HasLanguage extends AstNode {
  def getLanguage: Option[String]
}

trait HasConsequent extends AstNode {
  def getConsequent: Option[AstNode]
}

trait HasNodes extends AstNode {
  def getNodes: Option[List[AstNode]]
}

class AstAwaitImpl(val expr: Option[AstNode]) extends AstAwait {

  override def getExpr: Option[AstNode] = {
    expr
  }

}

class AstIfImpl(
    val test: Option[AstNode],
    val consequent: Option[AstNode],
    val alternative: Option[AstNode]
) extends AstIf {

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

class AstFlowControlImpl(val flow: Option[FlowControl], val value: Option[AstNode])
    extends AstFlowControl {

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

class AstNullImpl() extends AstNull {}

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

class AstLiteralImpl(val literal_value: Option[String], val ty: Option[TyNode]) extends AstLiteral {

  override def getLiteralValue: Option[String] = {
    literal_value
  }

  override def getTy: Option[TyNode] = {
    ty
  }

}

class AstUnitImpl() extends AstUnit {}

class AstRawCodeImpl(val code: Option[String], val language: Option[String]) extends AstRawCode {

  override def getCode: Option[String] = {
    code
  }

  override def getLanguage: Option[String] = {
    language
  }

}

class AstUndefinedImpl() extends AstUndefined {}

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

trait AstNull extends AstNode {}

trait AstSelect extends AstNode with HasQualifier with HasSymbol {

  def getQualifier: Option[AstNode]

  def getSymbol: Option[String]

}

trait AstBlock extends AstNode with HasNodes {

  def getNodes: Option[List[AstNode]]

}

trait AstLiteral extends AstNode with HasLiteralValue with HasTy {

  def getLiteralValue: Option[String]

  def getTy: Option[TyNode]

}

trait AstUnit extends AstNode {}

trait AstRawCode extends AstNode with HasCode with HasLanguage {

  def getCode: Option[String]

  def getLanguage: Option[String]

}

trait AstUndefined extends AstNode {}

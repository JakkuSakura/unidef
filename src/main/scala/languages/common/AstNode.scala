package com.jeekrs.unidef
package languages.common

import utils.{ExtKey, Extendable}

/**
  * The following AST nodes describes a general language that tries to be compatible with other languages
  * It should expose minimal interface while keep its original information
  * Rules: everything is an expression
  */
class AstNode extends Extendable {
  def inferType: TyNode = UnknownType

}

// Unit is the bottom type
class UnitNode extends AstNode

// Null is a null reference/pointer
class NoneNode extends AstNode

// Undefined
class UndefinedNode extends AstNode

case class BlockNode(nodes: List[AstNode], flatten: Boolean = false)
    extends AstNode

case class StatementNode(node: AstNode) extends AstNode

case class ExpressionNode(node: AstNode) extends AstNode

case class ConditionalNode(test: AstNode, success: AstNode, failure: AstNode)
    extends AstNode

sealed trait FlowControl
case object Next extends FlowControl
case object Continue extends FlowControl
case object Break extends FlowControl
case object Return extends FlowControl

case class FlowControlNode(flow: FlowControl, value: AstNode) extends AstNode

class LiteralNode extends AstNode

case class LiteralString(value: String) extends LiteralNode

case class LiteralChar(value: Char) extends LiteralNode

case class LiteralInteger(value: Int) extends LiteralNode

case class LiteralFloat(value: Double) extends LiteralNode

// difference is from https://github.com/ron-rs/ron
case class LiteralDict(values: List[(AstNode, AstNode)]) extends LiteralNode

case class LiteralStruct(values: List[(AstNode, AstNode)]) extends LiteralNode

case class LiteralOptional(value: Option[AstNode]) extends LiteralNode

case class ArgumentNode(name: String, value: AstNode) extends AstNode

sealed trait AccessModifier
case object Public extends AccessModifier
case object Private extends AccessModifier
case object Protected extends AccessModifier
case object Package extends AccessModifier
case class Limited(path: String) extends AccessModifier

case class FunctionDeclNode(name: AstNode,
                            arguments: List[ArgumentNode],
                            returnType: AstNode,
                            access: AccessModifier,
                            isAsync: Boolean = false,
                            body: AstNode)
    extends AstNode

case class ClassIdent(name: String) extends AstNode

case class ClassDeclNode(name: AstNode,
                         fields: List[FieldType],
                         methods: List[AstNode] = List(),
                         derived: List[ClassIdent] = List(),
) extends AstNode {

  override def inferType: StructType = ???
}

object ClassDeclNode {

  case object DataClass extends ExtKey {
    override type V = Boolean
  }
}

case class IdentifierNode(id: String) extends AstNode

case class DirectiveNode(directive: String) extends AstNode

sealed trait BinaryOperator
object BinaryOperator {
  case object Plus extends BinaryOperator
  case object Minus extends BinaryOperator
  case object Multiply extends BinaryOperator
  case object Divide extends BinaryOperator
}

case class BinaryOperatorNode(left: AstNode, right: AstNode, op: BinaryOperator)
    extends AstNode {
  def toFunctionApply: FunctionApplyNode =
    FunctionApplyNode(FunctionIdentNode(op.toString), List(left, right), Map())

}

case class FunctionIdentNode(name: String) extends AstNode

case class FunctionApplyNode(func: FunctionIdentNode,
                             args: List[AstNode],
                             kwArgs: Map[String, AstNode],
                             applyArgs: List[AstNode] = List(),
                             applyKwArgs: List[AstNode] = List())
    extends AstNode

case class AwaitNode(value: AstNode) extends AstNode

case class RawCodeNode(raw: String) extends AstNode

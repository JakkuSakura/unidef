package com.jeekrs.unidef
package languages.common

import utils.{ExtKey, Extendable}

/**
 * The following AST nodes describes a general language that tries to be compatible with other languages
 * It should expose minimal interface while keep its original information
 * Rules: everything is an expression
 */

class AstNode extends Extendable :
    def inferType: TyNode = UnknownType


// Unit is the bottom type
class UnitNode extends AstNode

// Null is a null reference/pointer
class NoneNode extends AstNode

// Undefined
class UndefinedNode extends AstNode


case class BlockNode(nodes: List[AstNode], flatten: Boolean = false) extends AstNode

case class StatementNode(node: AstNode) extends AstNode

case class ExpressionNode(node: AstNode) extends AstNode

case class ConditionalNode(test: AstNode, success: AstNode, failure: AstNode) extends AstNode

enum FlowControl:
    case Next
    case Continue
    case Break
    case Return

class FlowControlNode(flow: FlowControl, value: AstNode) extends AstNode


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

enum AccessModifier:
    case Public
    case Private
    case Protected
    case Package
    case Limited(path: String)

case class FunctionDeclNode(
                             name: AstNode,
                             arguments: List[ArgumentNode],
                             returnType: AstNode,
                             access: AccessModifier,
                             isAsync: Boolean = false,
                             body: AstNode
                           ) extends AstNode

case class ClassIdent(name: String) extends AstNode

case class ClassDeclNode(
                          name: AstNode,
                          derived: List[ClassIdent],
                          fields: List[FieldType],
                          methods: List[AstNode],
                        ) extends AstNode

object ClassDeclNode:
    case object DataClass extends ExtKey :
        override type V = Boolean

case class IdentifierNode(id: String) extends AstNode

case class DirectiveNode(directive: String) extends AstNode


enum BinaryOperator:
    case Plus
    case Minus
    case Multiply
    case Divide

case class BinaryOperatorNode(left: AstNode, right: AstNode, op: BinaryOperator) extends AstNode :
    def toFunctionApply: FunctionApplyNode =
        FunctionApplyNode(FunctionIdentNode(op.toString), List(left, right), Map())

case class FunctionIdentNode(name: String) extends AstNode

case class FunctionApplyNode(
                              func: FunctionIdentNode,
                              args: List[AstNode],
                              kwArgs: Map[String, AstNode],
                              applyArgs: List[AstNode] = List(),
                              applyKwArgs: List[AstNode] = List()
                            ) extends AstNode

case class AwaitNode(value: AstNode) extends AstNode

case class RawCodeNode(raw: String) extends AstNode

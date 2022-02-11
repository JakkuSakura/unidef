package com.jeekrs.unidef
package languages.common

/**
 * The following AST nodes describes a general language that tries to be compatible with other languages
 * It should expose minimal interface while keep its original information
 * Rules: everything is an expression
 */

class AstNode extends IrNode:
    def inferType: TyNode = UnknownType


// Unit is the bottom type
class UnitNode extends AstNode

// Null is a null reference/pointer
class NoneNode extends AstNode

// Undefined
class UndefinedNode extends AstNode


class BlockNode(nodes: List[AstNode], flatten: Boolean = false) extends AstNode

class StatementNode(node: AstNode) extends AstNode

class ExpressionNode(node: AstNode) extends AstNode

class ConditionalNode(test: AstNode, success: AstNode, failure: AstNode) extends AstNode

enum FlowControl:
    case Next
    case Continue
    case Break
    case Return

class FlowControlNode(flow: FlowControl, value: AstNode) extends AstNode


class LiteralNode extends AstNode

class LiteralString(value: String) extends LiteralNode

class LiteralChar(value: Char) extends LiteralNode

class LiteralInteger(value: Int) extends LiteralNode

class LiteralFloat(value: Double) extends LiteralNode

// difference is from https://github.com/ron-rs/ron
class LiteralDict(values: List[(AstNode, AstNode)]) extends LiteralNode

class LiteralStruct(values: List[(AstNode, AstNode)]) extends LiteralNode

class LiteralOptional(value: Option[AstNode]) extends LiteralNode


class ArgumentNode(name: String, value: AstNode) extends AstNode

enum AccessModifier:
    case Public
    case Private
    case Protected
    case Package
    case Limited(path: String)

class FunctionDeclNode(
                        name: AstNode,
                        arguments: List[ArgumentNode],
                        returnType: AstNode,
                        access: AccessModifier,
                        isAsync: Boolean = false,
                        body: AstNode
                      ) extends AstNode

class ClassIdent(name: String) extends AstNode

class ClassDeclNode(
                     name: AstNode,
                     derived: List[ClassIdent],
                     fields: List[AstNode], // TODO: types
                     methods: List[AstNode]
                   ) extends AstNode

class IdentifierNode(id: String) extends AstNode

class DirectiveNode(directive: String) extends AstNode


enum BinaryOperator:
    case Plus
    case Minus
    case Multiply
    case Divide

class BinaryOperatorNode(left: AstNode, right: AstNode, op: BinaryOperator) extends AstNode :
    def toFunctionApply: FunctionApplyNode =
        FunctionApplyNode(FunctionIdentNode(op.toString), List(left, right), Map())

class FunctionIdentNode(name: String) extends AstNode

class FunctionApplyNode(
                         func: FunctionIdentNode,
                         args: List[AstNode],
                         kwArgs: Map[String, AstNode],
                         applyArgs: List[AstNode] = List(),
                         applyKwArgs: List[AstNode] = List()
                       ) extends AstNode

class AwaitNode(value: AstNode) extends AstNode

package com.jeekrs.unidef
package languages.common

import utils.{ExtKey, ExtKeyBoolean, ExtKeyString, Extendable}

import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}
import io.circe.generic.JsonCodec
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import io.circe.generic.semiauto._

/**
  * The following AST nodes describes a general language that tries to be compatible with other languages
  * It should expose minimal interface while keep its original information
  * Rules: everything is an expression
  */
class AstNode extends Extendable {
  def inferType: TyNode = UnknownType

}
class StaticTypeNode(ty: TyNode) extends AstNode {
  override def inferType: TyNode = ty
}

// Unit is the bottom type
case object UnitNode extends StaticTypeNode(UnitType)

// Null is a null reference/pointer
case object NullNode extends StaticTypeNode(NullType)

// Undefined
case object UndefinedNode extends StaticTypeNode(UndefinedType)

case class TypedNode(ty: TyNode) extends StaticTypeNode(ty)

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

case class LiteralString(value: String) extends LiteralNode {
  override def inferType: TyNode = StringType
}

case class LiteralChar(value: Char) extends LiteralNode {
  override def inferType: TyNode = CharType
}

case class LiteralInteger(value: Int) extends LiteralNode {
  override def inferType: TyNode = IntegerType(BitSize.B32)
}

case class LiteralFloat(value: Double) extends LiteralNode {
  override def inferType: TyNode = FloatType(BitSize.B64)
}

// difference is from https://github.com/ron-rs/ron
case class LiteralDict(values: List[(AstNode, AstNode)]) extends LiteralNode

case class LiteralStruct(values: List[(AstNode, AstNode)]) extends LiteralNode

case class LiteralOptional(value: Option[AstNode]) extends LiteralNode

sealed trait AccessModifier
object AccessModifier {
  case object Public extends AccessModifier
  case object Private extends AccessModifier
  case object Protected extends AccessModifier
  case object Package extends AccessModifier
  case class Limited(path: String) extends AccessModifier

}

case class FunctionDeclNode(name: AstNode,
                            parameters: List[FieldType],
                            returnType: AstNode,
                            access: AccessModifier,
                            body: AstNode)
    extends AstNode

case class ClassIdent(name: String) extends AstNode

case class ClassDeclNode(name: AstNode,
                         fields: List[FieldType],
                         methods: List[AstNode] = List(),
                         derived: List[ClassIdent] = List(),
) extends AstNode {

  override def inferType: StructType =
    StructType(name.asInstanceOf[LiteralString].value, fields)
}

object ClassDeclNode {
  case object DataClass extends ExtKeyBoolean
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

case class RawCodeNode(raw: String, lang: Option[String] = None) extends AstNode

case class Annotation(value: AstNode) extends AstNode

case object Annotations extends ExtKey {
  override type V = List[Annotation]
  private val lsDecoder =
    deriveDecoder[List[String]].map(x => x.map(y => Annotation(RawCodeNode(y))))
  override def decoder: Option[Decoder[List[Annotation]]] =
    Some(lsDecoder)
}

case object Comment extends ExtKeyString

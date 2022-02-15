package com.jeekrs.unidef
package languages.common

import utils.{ExtKey, ExtKeyBoolean, ExtKeyString, Extendable}

import io.circe.Decoder
import io.circe.generic.semiauto._

/**
  * The following AST nodes describes a general language that tries to be compatible with other languages
  * It should expose minimal interface while keep its original information
  * Rules: everything is an expression
  */
class AstNode extends Extendable {
  def inferType: TyNode = TyUnknown

}
class AstStaticType(ty: TyNode) extends AstNode {
  override def inferType: TyNode = ty
}

// Unit is the bottom type
case object AstUnit extends AstStaticType(TyUnit)

// Null is a null reference/pointer
case object AstNull extends AstStaticType(TyNull)

// Undefined
case object AstUndefined extends AstStaticType(TyUndefined)

case class AstTyped(ty: TyNode) extends AstStaticType(ty)

case class AstBlock(nodes: List[AstNode], flatten: Boolean = false)
    extends AstNode

case class AstStatement(node: AstNode) extends AstNode

case class AstExpression(node: AstNode) extends AstNode

case class ConditionalNode(test: AstNode, success: AstNode, failure: AstNode)
    extends AstNode

sealed trait FlowControl
case object Next extends FlowControl
case object Continue extends FlowControl
case object Break extends FlowControl
case object Return extends FlowControl

case class AstFlowControl(flow: FlowControl, value: AstNode) extends AstNode

class AstLiteral extends AstNode

case class AstLiteralString(value: String) extends AstLiteral {
  override def inferType: TyNode = TyString
}

case class AstLiteralChar(value: Char) extends AstLiteral {
  override def inferType: TyNode = TyChar
}

case class AstLiteralInteger(value: Int) extends AstLiteral {
  override def inferType: TyNode = TyInteger(BitSize.B32)
}

case class AstLiteralFloat(value: Double) extends AstLiteral {
  override def inferType: TyNode = TyFloat(BitSize.B64)
}

// difference is from https://github.com/ron-rs/ron
case class AstLiteralDict(values: List[(AstNode, AstNode)]) extends AstLiteral

case class AstLiteralStruct(values: List[(AstNode, AstNode)]) extends AstLiteral

case class AstLiteralOptional(value: Option[AstNode]) extends AstLiteral

sealed trait AccessModifier
object AccessModifier {
  case object Public extends AccessModifier
  case object Private extends AccessModifier
  case object Protected extends AccessModifier
  case object Package extends AccessModifier
  case class Limited(path: String) extends AccessModifier

}

case class AstFunctionDecl(name: AstNode,
                           parameters: List[TyField],
                           returnType: AstNode,
                           access: AccessModifier,
                           body: AstNode)
    extends AstNode {
  def literalName: Option[String] = name match {
    case AstLiteralString(value) => Some(value)
    case _                       => None

  }
}

case class AstClassIdent(name: String) extends AstNode

case class AstClassDecl(name: AstNode,
                        fields: List[TyField],
                        methods: List[AstNode] = List(),
                        derived: List[AstClassIdent] = List(),
) extends AstNode {
  def literalName: Option[String] = name match {
    case AstLiteralString(value) => Some(value)
    case _                       => None
  }
  override def inferType: TyStruct =
    TyStruct(literalName.get, fields)
}

object AstClassDecl {
  case object DataClass extends ExtKeyBoolean
}

case class AstIdentifier(id: String) extends AstNode

case class AstDirective(directive: String) extends AstNode

sealed trait BinaryOperator
object BinaryOperator {
  case object Plus extends BinaryOperator
  case object Minus extends BinaryOperator
  case object Multiply extends BinaryOperator
  case object Divide extends BinaryOperator
}

case class AstBinaryOperator(left: AstNode, right: AstNode, op: BinaryOperator)
    extends AstNode {
  def toFunctionApply: AstFunctionApply =
    AstFunctionApply(AstFunctionIdent(op.toString), List(left, right), Map())

}

case class AstFunctionIdent(name: String) extends AstNode

case class AstFunctionApply(func: AstFunctionIdent,
                            args: List[AstNode],
                            kwArgs: Map[String, AstNode],
                            applyArgs: List[AstNode] = List(),
                            applyKwArgs: List[AstNode] = List())
    extends AstNode

case class AstAwait(value: AstNode) extends AstNode

case class AstRawCode(raw: String, lang: Option[String] = None) extends AstNode

case class AstAnnotation(value: AstNode) extends AstNode

case object AstAnnotations extends AstNode with ExtKey {
  override type V = List[AstAnnotation]

  override def name: String = "annotations"
  private val lsDecoder =
    deriveDecoder[List[String]]
      .map(x => x.map(y => AstAnnotation(AstRawCode(y))))
  override def decoder: Option[Decoder[List[AstAnnotation]]] =
    Some(lsDecoder)
}

case object AstComment extends AstNode with ExtKeyString {
  override def name: String = "comment"
}

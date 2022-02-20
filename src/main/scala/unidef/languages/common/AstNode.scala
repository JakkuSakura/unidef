package unidef.languages.common

import io.circe.Decoder
import io.circe.generic.semiauto._

/**
  * The following AST nodes describes a general language that tries to be compatible with other languages
  * It should expose minimal interface while keep its original information
  * Rules: everything is an expression
  */
trait AstNode

trait AstTypeExpr {
  def inferType: TyNode
}

class AstStaticType(ty: TyNode) extends AstNode with AstTypeExpr {
  override def inferType: TyNode = ty
}

// Unit is the bottom type
case object AstUnit extends AstStaticType(TyUnit)

// Null is a null reference/pointer
case object AstNull extends AstStaticType(TyNull)

// Undefined
case object AstUndefined extends AstStaticType(TyUndefined)

case class AstTyped(ty: TyNode) extends AstStaticType(ty)

case class AstBlock(nodes: Seq[AstNode], flatten: Boolean = false)
    extends AstNode

case class AstStatement(node: AstNode) extends AstNode

case class AstExpression(node: AstNode) extends AstNode

case class ConditionalNode(test: AstNode, success: AstNode, failure: AstNode)
    extends AstNode

sealed trait FlowControl
object FlowControl {
  case object Next extends FlowControl
  case object Break extends FlowControl
  case object Continue extends FlowControl
  case object Return extends FlowControl
  case object Throw extends FlowControl
}

case class AstFlowControl(flow: FlowControl, value: AstNode) extends AstNode

class AstLiteral(ty: TyNode) extends AstStaticType(ty)

case class AstLiteralString(value: String) extends AstLiteral(TyString)

case class AstLiteralChar(value: Char) extends AstLiteral(TyChar)
case class AstLiteralInteger(value: Int)
    extends AstLiteral(TyInteger(BitSize.B32))

case class AstLiteralFloat(value: Double)
    extends AstLiteral(TyFloat(BitSize.B64))

// difference is from https://github.com/ron-rs/ron
case class AstLiteralDict(values: Seq[(AstNode, AstNode)])

case class AstLiteralStruct(values: Seq[(AstNode, AstNode)])

case class AstLiteralOptional(value: Option[AstTypeExpr])

sealed trait AccessModifier

object AccessModifier extends Keyword {
  override type V = AccessModifier
  case object Public extends AccessModifier
  case object Private extends AccessModifier
  case object Protected extends AccessModifier
  case object Package extends AccessModifier
  case class Limited(path: String) extends AccessModifier
}

case class AstFunctionDecl(name: AstNode,
                           parameters: Seq[TyField],
                           returnType: TyNode,
                           body: Option[AstNode])
    extends Extendable
    with AstNode {
  def literalName: Option[String] = name match {
    case AstLiteralString(value) => Some(value)
    case _                       => None

  }
}
case class AstLambdaDecl(parameters: Seq[TyField],
                         returnType: TyNode,
                         body: AstNode)
    extends Extendable
    with AstNode

case class AstClassIdent(name: String) extends AstNode

case class AstClassDecl(name: AstNode,
                        fields: Seq[TyField],
                        methods: Seq[AstNode] = Nil,
                        derived: Seq[AstClassIdent] = Nil,
) extends Extendable
    with AstNode
    with AstTypeExpr {
  def literalName: Option[String] = name match {
    case AstLiteralString(value) => Some(value)
    case _                       => None
  }
  override def inferType: TyStruct =
    TyStruct(Some(fields)).setValue(KeyName, literalName.get)

}

object AstClassDecl {
  case object DataClass extends KeywordBoolean
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
    AstFunctionApply(AstFunctionIdent(op.toString), Seq(left, right), Map())

}

case class AstFunctionIdent(name: String) extends AstNode

case class AstFunctionApply(func: AstFunctionIdent,
                            args: Seq[AstNode],
                            kwArgs: Map[String, AstNode],
                            applyArgs: Seq[AstNode] = Nil,
                            applyKwArgs: Seq[AstNode] = Nil)
    extends AstNode

case class AstAwait(value: AstNode) extends AstNode

case class AstRawCode(raw: String) extends Extendable with AstNode

case class AstAnnotation(value: AstNode) extends AstNode

case object AstAnnotations extends AstNode with Keyword {
  override type V = Seq[AstAnnotation]

  override def name: String = "annotations"
  private val lsDecoder =
    deriveDecoder[List[String]]
      .map(x => x.map(y => AstAnnotation(AstRawCode(y))))
  override def decoder: Option[Decoder[Seq[AstAnnotation]]] =
    Some(lsDecoder.map(_.toSeq))
}

case object AstComment extends AstNode with KeywordString {
  override def name: String = "comment"
}

case object Body extends KeywordOnly
case object Language extends KeywordString
case object Parameters extends KeywordOnly
case object Return extends KeywordOnly

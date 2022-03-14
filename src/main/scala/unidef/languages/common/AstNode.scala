package unidef.languages.common
import unidef.languages.common.*

import io.circe.Decoder
import io.circe.generic.semiauto._

/** The following AST nodes describes a general language that tries to be compatible with other
  * languages It should expose minimal interface while keep its original information Rules:
  * everything is an expression
  */
trait AstNode extends TyNode

class AstStaticType(ty: TyNode) extends AstNode

// Unit is the bottom type
case object AstUnit extends AstStaticType(TyUnit)

// Null is a null reference/pointer
case object AstNull extends AstStaticType(TyNull)

// Undefined
case object AstUndefined extends AstStaticType(TyUndefined)

case class AstTyped(ty: TyNode) extends AstStaticType(ty)

case class AstBlock(nodes: Seq[AstNode], flatten: Boolean = false) extends AstNode

case class AstStatement(node: AstNode) extends AstNode

case class AstExpression(node: AstNode) extends AstNode

case class ConditionalNode(test: AstNode, success: AstNode, failure: AstNode) extends AstNode

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

case class AstLiteralString(value: String) extends AstLiteral(TyStringImpl())

case class AstLiteralChar(value: Char) extends AstLiteral(TyChar)
case class AstLiteralInteger(value: Int) extends AstLiteral(TyIntegerImpl(Some(BitSize.B32), None))

case class AstLiteralFloat(value: Double) extends AstLiteral(TyFloatImpl(Some(BitSize.B64)))

// TODO extends AstLiteral(TyFloat(BitSize.B32))
// difference is from https://github.com/ron-rs/ron
case class AstLiteralDict(values: Seq[(AstNode, AstNode)]) extends AstNode

case class AstLiteralStruct(values: Seq[(AstNode, AstNode)]) extends AstNode

case class AstLiteralArray(values: Seq[AstNode]) extends AstNode
case class AstLiteralOptional(value: Option[AstNode]) extends AstNode
case class AstLiteralBoolean(value: Boolean) extends AstLiteral(TyBooleanImpl())

sealed trait AccessModifier

object AccessModifier extends Keyword {
  override type V = AccessModifier
  case object Public extends AccessModifier
  case object Private extends AccessModifier
  case object Protected extends AccessModifier
  case object Package extends AccessModifier
  case class Limited(path: String) extends AccessModifier
}

case class AstFunctionDecl(
    name: AstNode,
    parameters: List[TyField],
    override val returnType: TyNode
) extends Extendable
    with AstNode
    with TyApplicable
    with HasName
    with HasBody {
  override def getName: Option[String] = literalName
  override def parameterType: TyNode = TyTupleImpl(Some(parameters))
  def literalName: Option[String] = name match {
    case AstLiteralString(value) => Some(value)
    case _ => None

  }
}

case class AstLambdaDecl(parameters: List[TyField], returnType: TyNode, body: AstNode)
    extends Extendable
    with AstNode

case class AstClassIdent(name: String) extends AstNode

case class AstClassDecl(
    name: AstNode,
    fields: List[TyField],
    methods: Seq[AstNode] = Nil,
    derived: Seq[AstClassIdent] = Nil
) extends Extendable
    with AstNode
    with TyStruct {

  override def getName: Option[String] = literalName
  override def getFields: Option[List[TyField]] = Some(fields)
  def literalName: Option[String] = name match {
    case AstLiteralString(value) => Some(value)
    case _ => None
  }

  override def getAttributes: Option[List[String]] = Some(Nil)

  override def getDerives: Option[List[String]] = Some(derived.map(_.name).toList)
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

case class AstBinaryOperator(left: AstNode, right: AstNode, op: BinaryOperator) extends AstNode {
  def toFunctionApply: AstFunctionApply =
    AstFunctionApply(AstFunctionIdent(op.toString), Seq(left, right), Map())

}

case class AstFunctionIdent(name: String) extends AstNode

case class AstFunctionApply(
    func: AstFunctionIdent,
    args: Seq[AstNode],
    kwArgs: Map[String, AstNode],
    applyArgs: Seq[AstNode] = Nil,
    applyKwArgs: Seq[AstNode] = Nil
) extends AstNode

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

trait HasBody extends Extendable {
  def getBody: Option[AstNode] = getValue(KeyBody)
}

case object KeyBody extends Keyword {
  override type V = AstNode
}

case object KeyLanguage extends KeywordString
case object KeyParameters extends KeywordOnly
case object KeyReturn extends Keyword {
  override type V = TyNode
}
case object KeyOverride extends KeywordBoolean
case object KeyClassType extends KeywordString

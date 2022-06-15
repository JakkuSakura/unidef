package unidef.common.ast

import io.circe.Decoder
import io.circe.generic.semiauto.*
import unidef.common.{BaseNode, Extendable, Keyword, KeywordBoolean, KeywordOnly, KeywordString}
import unidef.common.ty.*

/** The following AST nodes describes a general language that tries to be compatible with other
  * languages It should expose minimal interface while keep its original information Rules:
  * everything is an expression
  */
trait AstNode extends BaseNode

class AstStaticType(ty: TyNode) extends AstNode

case class AstTyped(ty: TyNode) extends AstStaticType(ty)

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

def AstLiteralString(x: String): AstLiteral = AstLiteralImpl(x, TyStringImpl())
def AstLiteralUnit(): AstLiteral = AstLiteralImpl("()", TyUnitImpl())
//class AstLiteral(ty: TyNode) extends AstStaticType(ty)
//
//case class AstLiteralString(value: String) extends AstLiteral(TyStringImpl())
//
//case class AstLiteralChar(value: Char) extends AstLiteral(TyCharImpl())
//case class AstLiteralInteger(value: Int) extends AstLiteral(TyIntegerImpl(Some(BitSize.B32), None))
//
//case class AstLiteralFloat(value: Double) extends AstLiteral(TyFloatImpl(Some(BitSize.B64)))
//
//// TODO extends AstLiteral(TyFloat(BitSize.B32))
//// difference is from https://github.com/ron-rs/ron
//case class AstLiteralDict(values: List[(AstNode, AstNode)]) extends AstNode
//
//case class AstLiteralStruct(values: List[(AstNode, AstNode)]) extends AstNode
//
//case class AstLiteralArray(values: List[AstNode]) extends AstNode
//case class AstLiteralOptional(value: Option[AstNode]) extends AstNode
//case class AstLiteralBoolean(value: Boolean) extends AstLiteral(TyBooleanImpl())

sealed trait AccessModifier

object AccessModifier extends Keyword {
  override type V = AccessModifier
  case object Public extends AccessModifier
  case object Private extends AccessModifier
  case object Protected extends AccessModifier
  case object Package extends AccessModifier
  case class Limited(path: String) extends AccessModifier
}
def extractArgumentStruct(func: AstFunctionDecl): TyStruct = {
  TyStructImpl(func.getName, Some(func.parameters), None, None, dataframe = func.getDataframe, schema = None, comment = func.comment.getOrElse(""))
}
case class AstFunctionDecl(
    name: String,
    parameters: List[TyField],
    override val returnType: TyNode,
    dataframe: Option[Boolean] = None,
    var comment: Option[String] = None
) extends Extendable
    with AstNode
    with TyApplicable
    with HasName
    with HasBody
    with HasDataframe
    with TyCommentable {
  override def getName: Option[String] = Some(name)

  override def parameterType: TyNode = TyTupleImpl(parameters)

  override def getDataframe: Option[Boolean] = dataframe

  override def getComment: String = comment.getOrElse("")

  override def setComment(comment: String): AstFunctionDecl.this.type = {
    this.comment = Some(comment)
    this
  }
}

case class AstLambdaDecl(parameters: List[TyField], returnType: TyNode, body: AstNode)
    extends Extendable
    with AstNode

case class AstClassIdent(name: String) extends AstNode

case class AstClassDecl(
    name: String,
    parameters: List[AstValDef],
    fields: List[AstValDef],
    methods: List[AstNode] = Nil,
    derived: List[AstClassIdent] = Nil,
    schema: Option[String] = None,
    dataframe: Option[Boolean] = None
) extends Extendable
    with AstNode
    with TyStruct {

  override def getName: Option[String] = Some(name)
  
  override def getFields: Option[List[TyField]] = Some(fields.map(x => TyField(x.getName, x.getTy, ???, x.getValue.map(x => true))))

  override def getAttributes: Option[List[String]] = Some(Nil)

  override def getDerives: Option[List[String]] = Some(derived.map(_.name).toList)

  override def getSchema: Option[String] = schema

  override def getDataframe: Option[Boolean] = {
    dataframe
  }

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
    AstFunctionApply(AstFunctionIdent(op.toString), List(left, right), Map())

}

case class AstFunctionIdent(name: String) extends AstNode

case class AstFunctionApply(
    func: AstFunctionIdent,
    args: List[AstNode],
    kwArgs: Map[String, AstNode],
    applyArgs: List[AstNode] = Nil,
    applyKwArgs: List[AstNode] = Nil
) extends AstNode

case class AstAnnotation(value: AstNode) extends AstNode

case object AstAnnotations extends AstNode with Keyword {
  override type V = List[AstAnnotation]

  override def name: String = "annotations"
  private val lsDecoder =
    deriveDecoder[List[String]]
      .map(x => x.map(y => AstAnnotation(AstRawCodeImpl(y, None))))
  override def decoder: Option[Decoder[List[AstAnnotation]]] =
    Some(lsDecoder.map(_.toSeq))
}
case class AstComment(comment: String) extends AstNode

case class AstProgram(statements: List[AstNode]) extends AstNode

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

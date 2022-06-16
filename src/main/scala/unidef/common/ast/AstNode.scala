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

sealed trait FlowControl
object FlowControl {
  case object Next extends FlowControl
  case object Break extends FlowControl
  case object Continue extends FlowControl
  case object Return extends FlowControl
  case object Throw extends FlowControl
}

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
  TyStructBuilder()
    .name(func.name)
    .fields(func.parameters)
    .dataframe(func.dataframe)
    .comment(func.comment)
    .build()

}
case class AstFunctionDecl(
    name: String,
    parameters: List[TyField],
    returnType: TyNode,
    dataframe: Option[Boolean] = None,
    var comment: Option[String] = None
) extends Extendable
    with AstNode
    with HasBody
    with HasDataframe {}

case class AstLambdaDecl(parameters: List[TyField], returnType: TyNode, body: AstNode)
    extends Extendable
    with AstNode

case class AstClassIdent(name: String) extends AstNode

def getField(x: AstValDef): TyField =
  TyFieldBuilder().name(x.name).value(x.ty).defaultNone(x.value.isDefined).build()
def getFields(self: AstClassDecl): List[TyField] =
  self.fields.map(getField)

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

package unidef.common.ast

import io.circe.Decoder
import io.circe.generic.semiauto.*
import unidef.common.BaseNode
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

object AccessModifier {
  case object Public extends AccessModifier
  case object Private extends AccessModifier
  case object Protected extends AccessModifier
  case object Package extends AccessModifier
  case class Limited(path: String) extends AccessModifier
}
def extractArgumentStruct(func: AstFunctionDecl): TyStruct = {
  TyStructBuilder()
    .name(func.name)
    .fields(func.parameters.map(getField))
    .dataframe(func.dataframe)
    .comment(func.comment)
    .build()

}

case class AstClassIdent(name: String) extends AstNode

def getField(x: AstValDef): TyField =
  TyFieldBuilder().name(x.name).value(x.ty).build()

sealed trait BinaryOperator
object BinaryOperator {
  case object Plus extends BinaryOperator
  case object Minus extends BinaryOperator
  case object Multiply extends BinaryOperator
  case object Divide extends BinaryOperator
}

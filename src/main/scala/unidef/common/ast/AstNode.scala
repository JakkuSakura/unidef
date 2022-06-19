package unidef.common.ast

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
    .fields(Asts.flattenParameters(func.parameters).map(getField))
    .dataframe(func.dataframe)
    .comment(func.comment)
    .build()

}

def getField(x: AstValDef): TyField =
  TyFieldBuilder().name(x.name).value(x.ty).build()

sealed trait BinaryOperator
object BinaryOperator {
  case object Plus extends BinaryOperator
  case object Minus extends BinaryOperator
  case object Multiply extends BinaryOperator
  case object Divide extends BinaryOperator
}

case object Asts {
  def flattenParameters(parameters: AstParameterLists): List[AstValDef] =
    parameters.parameterListsContent.flatMap(_.parameterListContent)
  def parameters(parameters: Seq[AstValDef]): AstParameterLists =
    AstParameterListsImpl(List(AstParameterListImpl(parameters.toList)))

  def flattenArguments(arguments: AstArgumentLists): List[AstArgument] =
    arguments.argumentListsContent.flatMap(_.argumentListContent)
  def arguments(arguments: Seq[AstArgument]): AstArgumentLists =
    AstArgumentListsImpl(List(AstArgumentListImpl(arguments.toList)))

  def unit(): AstLiteralUnit = AstLiteralUnitImpl()
//  def boolean(value: Boolean): AstLiteralBoolean() = AstBooleanImpl(value)
  def int(value: Int): AstLiteralInt = AstLiteralIntImpl(value)
  def string(value: String): AstLiteralString = AstLiteralStringImpl(value)

  def ident(name: String): AstIdent = AstIdentImpl(name)
}

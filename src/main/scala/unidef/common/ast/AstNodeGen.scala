package unidef.common.ast

import unidef.common.ty.*

import scala.collection.mutable

trait HasSchema() extends AstNode {
  def schema: Option[String]
}
trait HasTest() extends AstNode {
  def test: AstNode
}
trait HasApplicable() extends AstNode {
  def applicable: AstNode
}
trait HasFields() extends AstNode {
  def fields: List[AstValDef]
}
trait HasClassType() extends AstNode {
  def classType: Option[String]
}
trait HasMethods() extends AstNode {
  def methods: List[AstNode]
}
trait HasDerived() extends AstNode {
  def derived: List[AstClassIdent]
}
trait HasFlow() extends AstNode {
  def flow: Option[FlowControl]
}
trait HasMutability() extends AstNode {
  def mutability: Option[Boolean]
}
trait HasLiteralString() extends AstNode {
  def literalString: String
}
trait HasCode() extends AstNode {
  def code: String
}
trait HasValue() extends AstNode {
  def value: Option[AstNode]
}
trait HasQualifier() extends AstNode {
  def qualifier: AstNode
}
trait HasLanguage() extends AstNode {
  def language: Option[String]
}
trait HasLiteralValue() extends AstNode {
  def literalValue: String
}
trait HasArguments() extends AstNode {
  def arguments: List[AstNode]
}
trait HasParameters() extends AstNode {
  def parameters: List[AstValDef]
}
trait HasDataframe() extends AstNode {
  def dataframe: Option[Boolean]
}
trait HasNodes() extends AstNode {
  def nodes: List[AstNode]
}
trait HasDecls() extends AstNode {
  def decls: List[AstNode]
}
trait HasSymbol() extends AstNode {
  def symbol: String
}
trait HasAlternative() extends AstNode {
  def alternative: Option[AstNode]
}
trait HasLiteralInt() extends AstNode {
  def literalInt: Int
}
trait HasName() extends AstNode {
  def name: String
}
trait HasConsequent() extends AstNode {
  def consequent: Option[AstNode]
}
trait HasTy() extends AstNode {
  def ty: TyNode
}
trait HasExpr() extends AstNode {
  def expr: AstNode
}
class AstLiteralNullImpl() extends AstLiteralNull
class AstAwaitImpl(val expr: AstNode) extends AstAwait
class AstLiteralNoneImpl() extends AstLiteralNone
class AstIfImpl(
    val test: AstNode,
    val consequent: Option[AstNode],
    val alternative: Option[AstNode]
) extends AstIf
class AstFlowControlImpl(val flow: Option[FlowControl], val value: Option[AstNode])
    extends AstFlowControl
class AstLiteralUnitImpl() extends AstLiteralUnit
class AstStatementImpl(val expr: AstNode) extends AstStatement
class AstNullImpl() extends AstNull
class AstSelectImpl(val qualifier: AstNode, val symbol: String) extends AstSelect
class AstApplyImpl(val applicable: AstNode, val arguments: List[AstNode]) extends AstApply
class AstDeclsImpl(val decls: List[AstNode]) extends AstDecls
class AstLiteralStringImpl(val literalString: String) extends AstLiteralString
class AstLiteralImpl(val literalValue: String, val ty: TyNode) extends AstLiteral
class AstUnitImpl() extends AstUnit
class AstUndefinedImpl() extends AstUndefined
class AstBlockImpl(val nodes: List[AstNode]) extends AstBlock
class AstClassDeclImpl(
    val name: String,
    val parameters: List[AstValDef],
    val fields: List[AstValDef],
    val methods: List[AstNode],
    val derived: List[AstClassIdent],
    val schema: Option[String],
    val dataframe: Option[Boolean],
    val classType: Option[String]
) extends AstClassDecl
class AstValDefImpl(
    val name: String,
    val ty: TyNode,
    val value: Option[AstNode],
    val mutability: Option[Boolean]
) extends AstValDef
class AstTypeImpl(val ty: TyNode) extends AstType
class AstLiteralIntImpl(val literalInt: Int) extends AstLiteralInt
class AstRawCodeImpl(val code: String, val language: Option[String]) extends AstRawCode
trait AstLiteralNull() extends AstNode
trait AstAwait() extends AstNode with HasExpr {
  def expr: AstNode
}
trait AstLiteralNone() extends AstNode
trait AstIf() extends AstNode with HasTest with HasConsequent with HasAlternative {
  def test: AstNode
  def consequent: Option[AstNode]
  def alternative: Option[AstNode]
}
trait AstFlowControl() extends AstNode with HasFlow with HasValue {
  def flow: Option[FlowControl]
  def value: Option[AstNode]
}
trait AstLiteralUnit() extends AstNode
trait AstStatement() extends AstNode with HasExpr {
  def expr: AstNode
}
trait AstNull() extends AstNode
trait AstSelect() extends AstNode with HasQualifier with HasSymbol {
  def qualifier: AstNode
  def symbol: String
}
trait AstApply() extends AstNode with HasApplicable with HasArguments {
  def applicable: AstNode
  def arguments: List[AstNode]
}
trait AstDecls() extends AstNode with HasDecls {
  def decls: List[AstNode]
}
trait AstLiteralString() extends AstNode with HasLiteralString {
  def literalString: String
}
trait AstLiteral() extends AstNode with HasLiteralValue with HasTy {
  def literalValue: String
  def ty: TyNode
}
trait AstUnit() extends AstNode
trait AstUndefined() extends AstNode
trait AstBlock() extends AstNode with HasNodes {
  def nodes: List[AstNode]
}
trait AstClassDecl()
    extends AstNode
    with HasName
    with HasParameters
    with HasFields
    with HasMethods
    with HasDerived
    with HasSchema
    with HasDataframe
    with HasClassType {
  def name: String
  def parameters: List[AstValDef]
  def fields: List[AstValDef]
  def methods: List[AstNode]
  def derived: List[AstClassIdent]
  def schema: Option[String]
  def dataframe: Option[Boolean]
  def classType: Option[String]
}
trait AstValDef() extends AstNode with HasName with HasTy with HasValue with HasMutability {
  def name: String
  def ty: TyNode
  def value: Option[AstNode]
  def mutability: Option[Boolean]
}
trait AstType() extends AstNode with HasTy {
  def ty: TyNode
}
trait AstLiteralInt() extends AstNode with HasLiteralInt {
  def literalInt: Int
}
trait AstRawCode() extends AstNode with HasCode with HasLanguage {
  def code: String
  def language: Option[String]
}
class AstLiteralNullBuilder() {
  def build(): AstLiteralNullImpl = {
    AstLiteralNullImpl()
  }
}
class AstAwaitBuilder() {
  var expr: Option[AstNode] = None
  def expr(expr: AstNode): AstAwaitBuilder = {
    this.expr = Some(expr)
    this
  }
  def build(): AstAwaitImpl = {
    AstAwaitImpl(expr.get)
  }
}
class AstLiteralNoneBuilder() {
  def build(): AstLiteralNoneImpl = {
    AstLiteralNoneImpl()
  }
}
class AstIfBuilder() {
  var test: Option[AstNode] = None
  var consequent: Option[AstNode] = None
  var alternative: Option[AstNode] = None
  def test(test: AstNode): AstIfBuilder = {
    this.test = Some(test)
    this
  }
  def consequent(consequent: AstNode): AstIfBuilder = {
    this.consequent = Some(consequent)
    this
  }
  def consequent(consequent: Option[AstNode]): AstIfBuilder = {
    this.consequent = consequent
    this
  }
  def alternative(alternative: AstNode): AstIfBuilder = {
    this.alternative = Some(alternative)
    this
  }
  def alternative(alternative: Option[AstNode]): AstIfBuilder = {
    this.alternative = alternative
    this
  }
  def build(): AstIfImpl = {
    AstIfImpl(test.get, consequent, alternative)
  }
}
class AstFlowControlBuilder() {
  var flow: Option[FlowControl] = None
  var value: Option[AstNode] = None
  def flow(flow: FlowControl): AstFlowControlBuilder = {
    this.flow = Some(flow)
    this
  }
  def flow(flow: Option[FlowControl]): AstFlowControlBuilder = {
    this.flow = flow
    this
  }
  def value(value: AstNode): AstFlowControlBuilder = {
    this.value = Some(value)
    this
  }
  def value(value: Option[AstNode]): AstFlowControlBuilder = {
    this.value = value
    this
  }
  def build(): AstFlowControlImpl = {
    AstFlowControlImpl(flow, value)
  }
}
class AstLiteralUnitBuilder() {
  def build(): AstLiteralUnitImpl = {
    AstLiteralUnitImpl()
  }
}
class AstStatementBuilder() {
  var expr: Option[AstNode] = None
  def expr(expr: AstNode): AstStatementBuilder = {
    this.expr = Some(expr)
    this
  }
  def build(): AstStatementImpl = {
    AstStatementImpl(expr.get)
  }
}
class AstNullBuilder() {
  def build(): AstNullImpl = {
    AstNullImpl()
  }
}
class AstSelectBuilder() {
  var qualifier: Option[AstNode] = None
  var symbol: Option[String] = None
  def qualifier(qualifier: AstNode): AstSelectBuilder = {
    this.qualifier = Some(qualifier)
    this
  }
  def symbol(symbol: String): AstSelectBuilder = {
    this.symbol = Some(symbol)
    this
  }
  def build(): AstSelectImpl = {
    AstSelectImpl(qualifier.get, symbol.get)
  }
}
class AstApplyBuilder() {
  var applicable: Option[AstNode] = None
  var arguments: mutable.ArrayBuffer[AstNode] = mutable.ArrayBuffer.empty
  def applicable(applicable: AstNode): AstApplyBuilder = {
    this.applicable = Some(applicable)
    this
  }
  def arguments(arguments: List[AstNode]): AstApplyBuilder = {
    this.arguments ++= arguments
    this
  }
  def argument(argument: AstNode): AstApplyBuilder = {
    this.arguments += argument
    this
  }
  def build(): AstApplyImpl = {
    AstApplyImpl(applicable.get, arguments.toList)
  }
}
class AstDeclsBuilder() {
  var decls: mutable.ArrayBuffer[AstNode] = mutable.ArrayBuffer.empty
  def decls(decls: List[AstNode]): AstDeclsBuilder = {
    this.decls ++= decls
    this
  }
  def decl(decl: AstNode): AstDeclsBuilder = {
    this.decls += decl
    this
  }
  def build(): AstDeclsImpl = {
    AstDeclsImpl(decls.toList)
  }
}
class AstLiteralStringBuilder() {
  var literalString: Option[String] = None
  def literalString(literalString: String): AstLiteralStringBuilder = {
    this.literalString = Some(literalString)
    this
  }
  def build(): AstLiteralStringImpl = {
    AstLiteralStringImpl(literalString.get)
  }
}
class AstLiteralBuilder() {
  var literalValue: Option[String] = None
  var ty: Option[TyNode] = None
  def literalValue(literalValue: String): AstLiteralBuilder = {
    this.literalValue = Some(literalValue)
    this
  }
  def ty(ty: TyNode): AstLiteralBuilder = {
    this.ty = Some(ty)
    this
  }
  def build(): AstLiteralImpl = {
    AstLiteralImpl(literalValue.get, ty.get)
  }
}
class AstUnitBuilder() {
  def build(): AstUnitImpl = {
    AstUnitImpl()
  }
}
class AstUndefinedBuilder() {
  def build(): AstUndefinedImpl = {
    AstUndefinedImpl()
  }
}
class AstBlockBuilder() {
  var nodes: mutable.ArrayBuffer[AstNode] = mutable.ArrayBuffer.empty
  def nodes(nodes: List[AstNode]): AstBlockBuilder = {
    this.nodes ++= nodes
    this
  }
  def node(node: AstNode): AstBlockBuilder = {
    this.nodes += node
    this
  }
  def build(): AstBlockImpl = {
    AstBlockImpl(nodes.toList)
  }
}
class AstClassDeclBuilder() {
  var name: Option[String] = None
  var parameters: mutable.ArrayBuffer[AstValDef] = mutable.ArrayBuffer.empty
  var fields: mutable.ArrayBuffer[AstValDef] = mutable.ArrayBuffer.empty
  var methods: mutable.ArrayBuffer[AstNode] = mutable.ArrayBuffer.empty
  var derived: mutable.ArrayBuffer[AstClassIdent] = mutable.ArrayBuffer.empty
  var schema: Option[String] = None
  var dataframe: Option[Boolean] = None
  var classType: Option[String] = None
  def name(name: String): AstClassDeclBuilder = {
    this.name = Some(name)
    this
  }
  def parameters(parameters: List[AstValDef]): AstClassDeclBuilder = {
    this.parameters ++= parameters
    this
  }
  def parameter(parameter: AstValDef): AstClassDeclBuilder = {
    this.parameters += parameter
    this
  }
  def fields(fields: List[AstValDef]): AstClassDeclBuilder = {
    this.fields ++= fields
    this
  }
  def field(field: AstValDef): AstClassDeclBuilder = {
    this.fields += field
    this
  }
  def methods(methods: List[AstNode]): AstClassDeclBuilder = {
    this.methods ++= methods
    this
  }
  def method(method: AstNode): AstClassDeclBuilder = {
    this.methods += method
    this
  }
  def derived(derived: List[AstClassIdent]): AstClassDeclBuilder = {
    this.derived ++= derived
    this
  }
  def derived(derived: AstClassIdent): AstClassDeclBuilder = {
    this.derived += derived
    this
  }
  def schema(schema: String): AstClassDeclBuilder = {
    this.schema = Some(schema)
    this
  }
  def schema(schema: Option[String]): AstClassDeclBuilder = {
    this.schema = schema
    this
  }
  def dataframe(dataframe: Boolean): AstClassDeclBuilder = {
    this.dataframe = Some(dataframe)
    this
  }
  def dataframe(dataframe: Option[Boolean]): AstClassDeclBuilder = {
    this.dataframe = dataframe
    this
  }
  def classType(classType: String): AstClassDeclBuilder = {
    this.classType = Some(classType)
    this
  }
  def classType(classType: Option[String]): AstClassDeclBuilder = {
    this.classType = classType
    this
  }
  def build(): AstClassDeclImpl = {
    AstClassDeclImpl(
      name.get,
      parameters.toList,
      fields.toList,
      methods.toList,
      derived.toList,
      schema,
      dataframe,
      classType
    )
  }
}
class AstValDefBuilder() {
  var name: Option[String] = None
  var ty: Option[TyNode] = None
  var value: Option[AstNode] = None
  var mutability: Option[Boolean] = None
  def name(name: String): AstValDefBuilder = {
    this.name = Some(name)
    this
  }
  def ty(ty: TyNode): AstValDefBuilder = {
    this.ty = Some(ty)
    this
  }
  def value(value: AstNode): AstValDefBuilder = {
    this.value = Some(value)
    this
  }
  def value(value: Option[AstNode]): AstValDefBuilder = {
    this.value = value
    this
  }
  def mutability(mutability: Boolean): AstValDefBuilder = {
    this.mutability = Some(mutability)
    this
  }
  def mutability(mutability: Option[Boolean]): AstValDefBuilder = {
    this.mutability = mutability
    this
  }
  def build(): AstValDefImpl = {
    AstValDefImpl(name.get, ty.get, value, mutability)
  }
}
class AstTypeBuilder() {
  var ty: Option[TyNode] = None
  def ty(ty: TyNode): AstTypeBuilder = {
    this.ty = Some(ty)
    this
  }
  def build(): AstTypeImpl = {
    AstTypeImpl(ty.get)
  }
}
class AstLiteralIntBuilder() {
  var literalInt: Option[Int] = None
  def literalInt(literalInt: Int): AstLiteralIntBuilder = {
    this.literalInt = Some(literalInt)
    this
  }
  def build(): AstLiteralIntImpl = {
    AstLiteralIntImpl(literalInt.get)
  }
}
class AstRawCodeBuilder() {
  var code: Option[String] = None
  var language: Option[String] = None
  def code(code: String): AstRawCodeBuilder = {
    this.code = Some(code)
    this
  }
  def language(language: String): AstRawCodeBuilder = {
    this.language = Some(language)
    this
  }
  def language(language: Option[String]): AstRawCodeBuilder = {
    this.language = language
    this
  }
  def build(): AstRawCodeImpl = {
    AstRawCodeImpl(code.get, language)
  }
}

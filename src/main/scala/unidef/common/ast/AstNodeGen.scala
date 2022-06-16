package unidef.common.ast

import unidef.common.ty.*

trait HasSchema() extends AstNode {
  def schema: Option[String]
}
trait HasTest() extends AstNode {
  def test: Option[AstNode]
}
trait HasApplicable() extends AstNode {
  def applicable: AstNode
}
trait HasMethods() extends AstNode {
  def methods: Option[List[AstNode]]
}
trait HasDataframe() extends AstNode {
  def dataframe: Option[Boolean]
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
trait HasClassType() extends AstNode {
  def classType: Option[String]
}
trait HasFields() extends AstNode {
  def fields: Option[List[AstValDef]]
}
trait HasDecls() extends AstNode {
  def decls: List[AstNode]
}
trait HasDerived() extends AstNode {
  def derived: Option[List[AstClassIdent]]
}
trait HasSymbol() extends AstNode {
  def symbol: String
}
trait HasParameters() extends AstNode {
  def parameters: Option[List[AstValDef]]
}
trait HasFlow() extends AstNode {
  def flow: Option[FlowControl]
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
trait HasNodes() extends AstNode {
  def nodes: Option[List[AstNode]]
}
trait HasExpr() extends AstNode {
  def expr: AstNode
}
class AstLiteralNullImpl() extends AstLiteralNull
class AstAwaitImpl(val expr: AstNode) extends AstAwait
class AstLiteralNoneImpl() extends AstLiteralNone
class AstIfImpl(
    val test: Option[AstNode],
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
class AstBlockImpl(val nodes: Option[List[AstNode]]) extends AstBlock
class AstClassDeclImpl(
    val name: String,
    val parameters: Option[List[AstValDef]],
    val fields: Option[List[AstValDef]],
    val methods: Option[List[AstNode]],
    val derived: Option[List[AstClassIdent]],
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
  def test: Option[AstNode]
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
  def nodes: Option[List[AstNode]]
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
  def parameters: Option[List[AstValDef]]
  def fields: Option[List[AstValDef]]
  def methods: Option[List[AstNode]]
  def derived: Option[List[AstClassIdent]]
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
  def test(test: Option[AstNode]): AstIfBuilder = {
    this.test = test
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
    AstIfImpl(test, consequent, alternative)
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
  var arguments: Option[List[AstNode]] = None
  def applicable(applicable: AstNode): AstApplyBuilder = {
    this.applicable = Some(applicable)
    this
  }
  def arguments(arguments: List[AstNode]): AstApplyBuilder = {
    this.arguments = Some(arguments)
    this
  }
  def build(): AstApplyImpl = {
    AstApplyImpl(applicable.get, arguments.get)
  }
}
class AstDeclsBuilder() {
  var decls: Option[List[AstNode]] = None
  def decls(decls: List[AstNode]): AstDeclsBuilder = {
    this.decls = Some(decls)
    this
  }
  def build(): AstDeclsImpl = {
    AstDeclsImpl(decls.get)
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
  var nodes: Option[List[AstNode]] = None
  def nodes(nodes: List[AstNode]): AstBlockBuilder = {
    this.nodes = Some(nodes)
    this
  }
  def nodes(nodes: Option[List[AstNode]]): AstBlockBuilder = {
    this.nodes = nodes
    this
  }
  def build(): AstBlockImpl = {
    AstBlockImpl(nodes)
  }
}
class AstClassDeclBuilder() {
  var name: Option[String] = None
  var parameters: Option[List[AstValDef]] = None
  var fields: Option[List[AstValDef]] = None
  var methods: Option[List[AstNode]] = None
  var derived: Option[List[AstClassIdent]] = None
  var schema: Option[String] = None
  var dataframe: Option[Boolean] = None
  var classType: Option[String] = None
  def name(name: String): AstClassDeclBuilder = {
    this.name = Some(name)
    this
  }
  def parameters(parameters: List[AstValDef]): AstClassDeclBuilder = {
    this.parameters = Some(parameters)
    this
  }
  def parameters(parameters: Option[List[AstValDef]]): AstClassDeclBuilder = {
    this.parameters = parameters
    this
  }
  def fields(fields: List[AstValDef]): AstClassDeclBuilder = {
    this.fields = Some(fields)
    this
  }
  def fields(fields: Option[List[AstValDef]]): AstClassDeclBuilder = {
    this.fields = fields
    this
  }
  def methods(methods: List[AstNode]): AstClassDeclBuilder = {
    this.methods = Some(methods)
    this
  }
  def methods(methods: Option[List[AstNode]]): AstClassDeclBuilder = {
    this.methods = methods
    this
  }
  def derived(derived: List[AstClassIdent]): AstClassDeclBuilder = {
    this.derived = Some(derived)
    this
  }
  def derived(derived: Option[List[AstClassIdent]]): AstClassDeclBuilder = {
    this.derived = derived
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
    AstClassDeclImpl(name.get, parameters, fields, methods, derived, schema, dataframe, classType)
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

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
trait HasRecords() extends AstNode {
  def records: Option[Boolean]
}
trait HasParameterLists() extends AstNode {
  def parameterLists: List[AstParameterList]
}
trait HasFields() extends AstNode {
  def fields: List[AstValDef]
}
trait HasReturnType() extends AstNode {
  def returnType: TyNode
}
trait HasDataframe() extends AstNode {
  def dataframe: Option[Boolean]
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
trait HasStmts() extends AstNode {
  def stmts: List[AstNode]
}
trait HasAutoIncr() extends AstNode {
  def autoIncr: Option[Boolean]
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
trait HasParameterList() extends AstNode {
  def parameterList: List[AstValDef]
}
trait HasArguments() extends AstNode {
  def arguments: List[AstNode]
}
trait HasParameters() extends AstNode {
  def parameters: List[AstValDef]
}
trait HasPrimaryKey() extends AstNode {
  def primaryKey: Option[Boolean]
}
trait HasOverwrite() extends AstNode {
  def overwrite: Option[Boolean]
}
trait HasDirective() extends AstNode {
  def directive: String
}
trait HasArgumentList() extends AstNode {
  def argumentList: List[AstArgument]
}
trait HasClassId() extends AstNode {
  def classId: String
}
trait HasVariableIdentifier() extends AstNode {
  def variableIdentifier: String
}
trait HasDecls() extends AstNode {
  def decls: List[AstNode]
}
trait HasSymbol() extends AstNode {
  def symbol: String
}
trait HasArgumentLists() extends AstNode {
  def argumentLists: List[AstArgumentList]
}
trait HasFlow() extends AstNode {
  def flow: Option[FlowControl]
}
trait HasMutability() extends AstNode {
  def mutability: Option[Boolean]
}
trait HasArgumentsLists() extends AstNode {
  def argumentsLists: AstArgumentLists
}
trait HasBody() extends AstNode {
  def body: Option[AstNode]
}
trait HasLiteralString() extends AstNode {
  def literalString: String
}
trait HasAccess() extends AstNode {
  def access: Option[AccessModifier]
}
trait HasComment() extends AstNode {
  def comment: Option[String]
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
case class AstLiteralNullImpl() extends AstLiteralNull
case class AstProgramImpl(stmts: List[AstNode]) extends AstProgram
case class AstAwaitImpl(expr: AstNode) extends AstAwait
case class AstLiteralNoneImpl() extends AstLiteralNone
case class AstFunctionDeclImpl(
    name: String,
    parameters: List[AstValDef],
    returnType: TyNode,
    dataframe: Option[Boolean],
    records: Option[Boolean],
    comment: Option[String],
    body: Option[AstNode],
    schema: Option[String],
    language: Option[String],
    overwrite: Option[Boolean]
) extends AstFunctionDecl
case class AstIfImpl(test: AstNode, consequent: Option[AstNode], alternative: Option[AstNode])
    extends AstIf
case class AstFlowControlImpl(flow: Option[FlowControl], value: Option[AstNode])
    extends AstFlowControl
case class AstLiteralUnitImpl() extends AstLiteralUnit
case class AstClassIdentifierImpl(classId: String) extends AstClassIdentifier
case class AstStatementImpl(expr: AstNode) extends AstStatement
case class AstArgumentListImpl(argumentList: List[AstArgument]) extends AstArgumentList
case class AstApplyImpl(applicable: AstNode, arguments: List[AstNode]) extends AstApply
case class AstDeclsImpl(decls: List[AstNode]) extends AstDecls
case class AstLiteralStringImpl(literalString: String) extends AstLiteralString
case class AstLiteralImpl(literalValue: String, ty: TyNode) extends AstLiteral
case class AstUnitImpl() extends AstUnit
case class AstArgumentImpl(name: String, value: Option[AstNode]) extends AstArgument
case class AstDirectiveImpl(directive: String) extends AstDirective
case class AstUndefinedImpl() extends AstUndefined
case class AstNullImpl() extends AstNull
case class AstArgumentListsImpl(argumentLists: List[AstArgumentList]) extends AstArgumentLists
case class AstSelectImpl(qualifier: AstNode, symbol: String) extends AstSelect
case class AstParameterListsImpl(parameterLists: List[AstParameterList]) extends AstParameterLists
case class AstBlockImpl(nodes: Option[List[AstNode]]) extends AstBlock
case class AstClassDeclImpl(
    name: String,
    parameters: List[AstValDef],
    fields: List[AstValDef],
    methods: List[AstNode],
    derived: List[AstClassIdent],
    schema: Option[String],
    dataframe: Option[Boolean],
    classType: Option[String],
    access: Option[AccessModifier]
) extends AstClassDecl
case class AstVariableIdentifierImpl(variableIdentifier: String) extends AstVariableIdentifier
case class AstApplyListsImpl(applicable: AstNode, argumentsLists: AstArgumentLists)
    extends AstApplyLists
case class AstValDefImpl(
    name: String,
    ty: TyNode,
    value: Option[AstNode],
    mutability: Option[Boolean],
    autoIncr: Option[Boolean],
    primaryKey: Option[Boolean]
) extends AstValDef
case class AstTypeImpl(ty: TyNode) extends AstType
case class AstParameterListImpl(parameterList: List[AstValDef]) extends AstParameterList
case class AstLiteralIntImpl(literalInt: Int) extends AstLiteralInt
case class AstRawCodeImpl(code: String, language: Option[String]) extends AstRawCode
trait AstLiteralNull() extends AstNode
trait AstProgram() extends AstNode with HasStmts {
  def stmts: List[AstNode]
}
trait AstAwait() extends AstNode with HasExpr {
  def expr: AstNode
}
trait AstLiteralNone() extends AstNode
trait AstFunctionDecl()
    extends AstNode
    with HasName
    with HasParameters
    with HasReturnType
    with HasDataframe
    with HasRecords
    with HasComment
    with HasBody
    with HasSchema
    with HasLanguage
    with HasOverwrite {
  def name: String
  def parameters: List[AstValDef]
  def returnType: TyNode
  def dataframe: Option[Boolean]
  def records: Option[Boolean]
  def comment: Option[String]
  def body: Option[AstNode]
  def schema: Option[String]
  def language: Option[String]
  def overwrite: Option[Boolean]
}
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
trait AstClassIdentifier() extends AstNode with HasClassId {
  def classId: String
}
trait AstStatement() extends AstNode with HasExpr {
  def expr: AstNode
}
trait AstArgumentList() extends AstNode with HasArgumentList {
  def argumentList: List[AstArgument]
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
trait AstArgument() extends AstNode with HasName with HasValue {
  def name: String
  def value: Option[AstNode]
}
trait AstDirective() extends AstNode with HasDirective {
  def directive: String
}
trait AstUndefined() extends AstNode
trait AstNull() extends AstNode
trait AstArgumentLists() extends AstNode with HasArgumentLists {
  def argumentLists: List[AstArgumentList]
}
trait AstSelect() extends AstNode with HasQualifier with HasSymbol {
  def qualifier: AstNode
  def symbol: String
}
trait AstParameterLists() extends AstNode with HasParameterLists {
  def parameterLists: List[AstParameterList]
}
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
    with HasClassType
    with HasAccess {
  def name: String
  def parameters: List[AstValDef]
  def fields: List[AstValDef]
  def methods: List[AstNode]
  def derived: List[AstClassIdent]
  def schema: Option[String]
  def dataframe: Option[Boolean]
  def classType: Option[String]
  def access: Option[AccessModifier]
}
trait AstVariableIdentifier() extends AstNode with HasVariableIdentifier {
  def variableIdentifier: String
}
trait AstApplyLists() extends AstNode with HasApplicable with HasArgumentsLists {
  def applicable: AstNode
  def argumentsLists: AstArgumentLists
}
trait AstValDef()
    extends AstNode
    with HasName
    with HasTy
    with HasValue
    with HasMutability
    with HasAutoIncr
    with HasPrimaryKey {
  def name: String
  def ty: TyNode
  def value: Option[AstNode]
  def mutability: Option[Boolean]
  def autoIncr: Option[Boolean]
  def primaryKey: Option[Boolean]
}
trait AstType() extends AstNode with HasTy {
  def ty: TyNode
}
trait AstParameterList() extends AstNode with HasParameterList {
  def parameterList: List[AstValDef]
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
class AstProgramBuilder() {
  var stmts: mutable.ArrayBuffer[AstNode] = mutable.ArrayBuffer.empty
  def stmts(stmts: List[AstNode]): AstProgramBuilder = {
    this.stmts ++= stmts
    this
  }
  def stmt(stmt: AstNode): AstProgramBuilder = {
    this.stmts += stmt
    this
  }
  def build(): AstProgramImpl = {
    AstProgramImpl(stmts.toList)
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
class AstFunctionDeclBuilder() {
  var name: Option[String] = None
  var parameters: mutable.ArrayBuffer[AstValDef] = mutable.ArrayBuffer.empty
  var returnType: Option[TyNode] = None
  var dataframe: Option[Boolean] = None
  var records: Option[Boolean] = None
  var comment: Option[String] = None
  var body: Option[AstNode] = None
  var schema: Option[String] = None
  var language: Option[String] = None
  var overwrite: Option[Boolean] = None
  def name(name: String): AstFunctionDeclBuilder = {
    this.name = Some(name)
    this
  }
  def parameters(parameters: List[AstValDef]): AstFunctionDeclBuilder = {
    this.parameters ++= parameters
    this
  }
  def returnType(returnType: TyNode): AstFunctionDeclBuilder = {
    this.returnType = Some(returnType)
    this
  }
  def dataframe(dataframe: Boolean): AstFunctionDeclBuilder = {
    this.dataframe = Some(dataframe)
    this
  }
  def records(records: Boolean): AstFunctionDeclBuilder = {
    this.records = Some(records)
    this
  }
  def comment(comment: String): AstFunctionDeclBuilder = {
    this.comment = Some(comment)
    this
  }
  def body(body: AstNode): AstFunctionDeclBuilder = {
    this.body = Some(body)
    this
  }
  def schema(schema: String): AstFunctionDeclBuilder = {
    this.schema = Some(schema)
    this
  }
  def language(language: String): AstFunctionDeclBuilder = {
    this.language = Some(language)
    this
  }
  def overwrite(overwrite: Boolean): AstFunctionDeclBuilder = {
    this.overwrite = Some(overwrite)
    this
  }
  def dataframe(dataframe: Option[Boolean]): AstFunctionDeclBuilder = {
    this.dataframe = dataframe
    this
  }
  def records(records: Option[Boolean]): AstFunctionDeclBuilder = {
    this.records = records
    this
  }
  def comment(comment: Option[String]): AstFunctionDeclBuilder = {
    this.comment = comment
    this
  }
  def body(body: Option[AstNode]): AstFunctionDeclBuilder = {
    this.body = body
    this
  }
  def schema(schema: Option[String]): AstFunctionDeclBuilder = {
    this.schema = schema
    this
  }
  def language(language: Option[String]): AstFunctionDeclBuilder = {
    this.language = language
    this
  }
  def overwrite(overwrite: Option[Boolean]): AstFunctionDeclBuilder = {
    this.overwrite = overwrite
    this
  }
  def parameter(parameter: AstValDef): AstFunctionDeclBuilder = {
    this.parameters += parameter
    this
  }
  def build(): AstFunctionDeclImpl = {
    AstFunctionDeclImpl(
      name.get,
      parameters.toList,
      returnType.get,
      dataframe,
      records,
      comment,
      body,
      schema,
      language,
      overwrite
    )
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
  def alternative(alternative: AstNode): AstIfBuilder = {
    this.alternative = Some(alternative)
    this
  }
  def consequent(consequent: Option[AstNode]): AstIfBuilder = {
    this.consequent = consequent
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
  def value(value: AstNode): AstFlowControlBuilder = {
    this.value = Some(value)
    this
  }
  def flow(flow: Option[FlowControl]): AstFlowControlBuilder = {
    this.flow = flow
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
class AstClassIdentifierBuilder() {
  var classId: Option[String] = None
  def classId(classId: String): AstClassIdentifierBuilder = {
    this.classId = Some(classId)
    this
  }
  def build(): AstClassIdentifierImpl = {
    AstClassIdentifierImpl(classId.get)
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
class AstArgumentListBuilder() {
  var argumentList: mutable.ArrayBuffer[AstArgument] = mutable.ArrayBuffer.empty
  def argumentList(argumentList: List[AstArgument]): AstArgumentListBuilder = {
    this.argumentList ++= argumentList
    this
  }
  def argumentList(argumentList: AstArgument): AstArgumentListBuilder = {
    this.argumentList += argumentList
    this
  }
  def build(): AstArgumentListImpl = {
    AstArgumentListImpl(argumentList.toList)
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
class AstArgumentBuilder() {
  var name: Option[String] = None
  var value: Option[AstNode] = None
  def name(name: String): AstArgumentBuilder = {
    this.name = Some(name)
    this
  }
  def value(value: AstNode): AstArgumentBuilder = {
    this.value = Some(value)
    this
  }
  def value(value: Option[AstNode]): AstArgumentBuilder = {
    this.value = value
    this
  }
  def build(): AstArgumentImpl = {
    AstArgumentImpl(name.get, value)
  }
}
class AstDirectiveBuilder() {
  var directive: Option[String] = None
  def directive(directive: String): AstDirectiveBuilder = {
    this.directive = Some(directive)
    this
  }
  def build(): AstDirectiveImpl = {
    AstDirectiveImpl(directive.get)
  }
}
class AstUndefinedBuilder() {
  def build(): AstUndefinedImpl = {
    AstUndefinedImpl()
  }
}
class AstNullBuilder() {
  def build(): AstNullImpl = {
    AstNullImpl()
  }
}
class AstArgumentListsBuilder() {
  var argumentLists: mutable.ArrayBuffer[AstArgumentList] = mutable.ArrayBuffer.empty
  def argumentLists(argumentLists: List[AstArgumentList]): AstArgumentListsBuilder = {
    this.argumentLists ++= argumentLists
    this
  }
  def argumentList(argumentList: AstArgumentList): AstArgumentListsBuilder = {
    this.argumentLists += argumentList
    this
  }
  def build(): AstArgumentListsImpl = {
    AstArgumentListsImpl(argumentLists.toList)
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
class AstParameterListsBuilder() {
  var parameterLists: mutable.ArrayBuffer[AstParameterList] = mutable.ArrayBuffer.empty
  def parameterLists(parameterLists: List[AstParameterList]): AstParameterListsBuilder = {
    this.parameterLists ++= parameterLists
    this
  }
  def parameterList(parameterList: AstParameterList): AstParameterListsBuilder = {
    this.parameterLists += parameterList
    this
  }
  def build(): AstParameterListsImpl = {
    AstParameterListsImpl(parameterLists.toList)
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
  var parameters: mutable.ArrayBuffer[AstValDef] = mutable.ArrayBuffer.empty
  var fields: mutable.ArrayBuffer[AstValDef] = mutable.ArrayBuffer.empty
  var methods: mutable.ArrayBuffer[AstNode] = mutable.ArrayBuffer.empty
  var derived: mutable.ArrayBuffer[AstClassIdent] = mutable.ArrayBuffer.empty
  var schema: Option[String] = None
  var dataframe: Option[Boolean] = None
  var classType: Option[String] = None
  var access: Option[AccessModifier] = None
  def name(name: String): AstClassDeclBuilder = {
    this.name = Some(name)
    this
  }
  def parameters(parameters: List[AstValDef]): AstClassDeclBuilder = {
    this.parameters ++= parameters
    this
  }
  def fields(fields: List[AstValDef]): AstClassDeclBuilder = {
    this.fields ++= fields
    this
  }
  def methods(methods: List[AstNode]): AstClassDeclBuilder = {
    this.methods ++= methods
    this
  }
  def derived(derived: List[AstClassIdent]): AstClassDeclBuilder = {
    this.derived ++= derived
    this
  }
  def schema(schema: String): AstClassDeclBuilder = {
    this.schema = Some(schema)
    this
  }
  def dataframe(dataframe: Boolean): AstClassDeclBuilder = {
    this.dataframe = Some(dataframe)
    this
  }
  def classType(classType: String): AstClassDeclBuilder = {
    this.classType = Some(classType)
    this
  }
  def access(access: AccessModifier): AstClassDeclBuilder = {
    this.access = Some(access)
    this
  }
  def schema(schema: Option[String]): AstClassDeclBuilder = {
    this.schema = schema
    this
  }
  def dataframe(dataframe: Option[Boolean]): AstClassDeclBuilder = {
    this.dataframe = dataframe
    this
  }
  def classType(classType: Option[String]): AstClassDeclBuilder = {
    this.classType = classType
    this
  }
  def access(access: Option[AccessModifier]): AstClassDeclBuilder = {
    this.access = access
    this
  }
  def parameter(parameter: AstValDef): AstClassDeclBuilder = {
    this.parameters += parameter
    this
  }
  def field(field: AstValDef): AstClassDeclBuilder = {
    this.fields += field
    this
  }
  def method(method: AstNode): AstClassDeclBuilder = {
    this.methods += method
    this
  }
  def derived(derived: AstClassIdent): AstClassDeclBuilder = {
    this.derived += derived
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
      classType,
      access
    )
  }
}
class AstVariableIdentifierBuilder() {
  var variableIdentifier: Option[String] = None
  def variableIdentifier(variableIdentifier: String): AstVariableIdentifierBuilder = {
    this.variableIdentifier = Some(variableIdentifier)
    this
  }
  def build(): AstVariableIdentifierImpl = {
    AstVariableIdentifierImpl(variableIdentifier.get)
  }
}
class AstApplyListsBuilder() {
  var applicable: Option[AstNode] = None
  var argumentsLists: Option[AstArgumentLists] = None
  def applicable(applicable: AstNode): AstApplyListsBuilder = {
    this.applicable = Some(applicable)
    this
  }
  def argumentsLists(argumentsLists: AstArgumentLists): AstApplyListsBuilder = {
    this.argumentsLists = Some(argumentsLists)
    this
  }
  def build(): AstApplyListsImpl = {
    AstApplyListsImpl(applicable.get, argumentsLists.get)
  }
}
class AstValDefBuilder() {
  var name: Option[String] = None
  var ty: Option[TyNode] = None
  var value: Option[AstNode] = None
  var mutability: Option[Boolean] = None
  var autoIncr: Option[Boolean] = None
  var primaryKey: Option[Boolean] = None
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
  def mutability(mutability: Boolean): AstValDefBuilder = {
    this.mutability = Some(mutability)
    this
  }
  def autoIncr(autoIncr: Boolean): AstValDefBuilder = {
    this.autoIncr = Some(autoIncr)
    this
  }
  def primaryKey(primaryKey: Boolean): AstValDefBuilder = {
    this.primaryKey = Some(primaryKey)
    this
  }
  def value(value: Option[AstNode]): AstValDefBuilder = {
    this.value = value
    this
  }
  def mutability(mutability: Option[Boolean]): AstValDefBuilder = {
    this.mutability = mutability
    this
  }
  def autoIncr(autoIncr: Option[Boolean]): AstValDefBuilder = {
    this.autoIncr = autoIncr
    this
  }
  def primaryKey(primaryKey: Option[Boolean]): AstValDefBuilder = {
    this.primaryKey = primaryKey
    this
  }
  def build(): AstValDefImpl = {
    AstValDefImpl(name.get, ty.get, value, mutability, autoIncr, primaryKey)
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
class AstParameterListBuilder() {
  var parameterList: mutable.ArrayBuffer[AstValDef] = mutable.ArrayBuffer.empty
  def parameterList(parameterList: List[AstValDef]): AstParameterListBuilder = {
    this.parameterList ++= parameterList
    this
  }
  def parameterList(parameterList: AstValDef): AstParameterListBuilder = {
    this.parameterList += parameterList
    this
  }
  def build(): AstParameterListImpl = {
    AstParameterListImpl(parameterList.toList)
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

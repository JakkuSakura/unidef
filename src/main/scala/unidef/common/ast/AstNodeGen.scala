package unidef.common.ast

import unidef.common.ty.*
import scala.collection.mutable


trait HasArguments() extends AstNode {
  def arguments: AstArgumentLists
}
trait HasDerives() extends AstNode {
  def derives: List[AstNode]
}
trait HasTest() extends AstNode {
  def test: AstNode
}
trait HasApplicant() extends AstNode {
  def applicant: AstNode
}
trait HasRecords() extends AstNode {
  def records: Option[Boolean]
}
trait HasPrimaryKey() extends AstNode {
  def primaryKey: Option[Boolean]
}
trait HasFields() extends AstNode {
  def fields: List[AstValDef]
}
trait HasClassType() extends AstNode {
  def classType: Option[String]
}
trait HasFlow() extends AstNode {
  def flow: Option[FlowControl]
}
trait HasStmts() extends AstNode {
  def stmts: List[AstNode]
}
trait HasAutoIncr() extends AstNode {
  def autoIncr: Option[Boolean]
}
trait HasMutability() extends AstNode {
  def mutability: Option[Boolean]
}
trait HasArgumentListsContent() extends AstNode {
  def argumentListsContent: List[AstArgumentList]
}
trait HasCode() extends AstNode {
  def code: String
}
trait HasParameters() extends AstNode {
  def parameters: AstParameterLists
}
trait HasSchema() extends AstNode {
  def schema: Option[String]
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
trait HasOverwrite() extends AstNode {
  def overwrite: Option[Boolean]
}
trait HasDirective() extends AstNode {
  def directive: String
}
trait HasParameterListsContent() extends AstNode {
  def parameterListsContent: List[AstParameterList]
}
trait HasArgumentListContent() extends AstNode {
  def argumentListContent: List[AstArgument]
}
trait HasReturnType() extends AstNode {
  def returnType: TyNode
}
trait HasDataframe() extends AstNode {
  def dataframe: Option[Boolean]
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
trait HasMethods() extends AstNode {
  def methods: List[AstNode]
}
trait HasParameterListContent() extends AstNode {
  def parameterListContent: List[AstValDef]
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
trait HasExpr() extends AstNode {
  def expr: AstNode
}
case class AstLiteralNullImpl() extends AstLiteralNull 
case class AstProgramImpl(stmts: List[AstNode]) extends AstProgram 
case class AstAwaitImpl(expr: AstNode) extends AstAwait 
case class AstLiteralNoneImpl() extends AstLiteralNone 
case class AstIfImpl(test: AstNode, consequent: Option[AstNode], alternative: Option[AstNode]) extends AstIf 
case class AstFlowControlImpl(flow: Option[FlowControl], value: Option[AstNode]) extends AstFlowControl 
case class AstLiteralUnitImpl() extends AstLiteralUnit 
case class AstClassIdentifierImpl(classId: String) extends AstClassIdentifier 
case class AstStatementImpl(expr: AstNode) extends AstStatement 
case class AstArgumentListsImpl(argumentListsContent: List[AstArgumentList]) extends AstArgumentLists 
case class AstArgumentListImpl(argumentListContent: List[AstArgument]) extends AstArgumentList 
case class AstApplyImpl(applicant: AstNode, arguments: AstArgumentLists) extends AstApply 
case class AstDeclsImpl(decls: List[AstNode]) extends AstDecls 
case class AstLiteralStringImpl(literalString: String) extends AstLiteralString 
case class AstArgumentImpl(name: String, value: Option[AstNode]) extends AstArgument 
case class AstDirectiveImpl(directive: String) extends AstDirective 
case class AstFunctionDeclImpl(name: String, parameters: AstParameterLists, returnType: TyNode, dataframe: Option[Boolean], records: Option[Boolean], comment: Option[String], body: Option[AstNode], schema: Option[String], language: Option[String], overwrite: Option[Boolean]) extends AstFunctionDecl 
case class AstLiteralUndefinedImpl() extends AstLiteralUndefined 
case class AstSelectImpl(qualifier: AstNode, symbol: String) extends AstSelect 
case class AstParameterListsImpl(parameterListsContent: List[AstParameterList]) extends AstParameterLists 
case class AstBlockImpl(stmts: List[AstNode]) extends AstBlock 
case class AstClassDeclImpl(name: String, parameters: AstParameterLists, fields: List[AstValDef], methods: List[AstNode], derives: List[AstNode], schema: Option[String], dataframe: Option[Boolean], classType: Option[String], access: Option[AccessModifier]) extends AstClassDecl 
case class AstIdentImpl(name: String) extends AstIdent 
case class AstVariableIdentifierImpl(variableIdentifier: String) extends AstVariableIdentifier 
case class AstValDefImpl(name: String, ty: TyNode, value: Option[AstNode], mutability: Option[Boolean], autoIncr: Option[Boolean], primaryKey: Option[Boolean]) extends AstValDef 
case class AstTypeImpl(ty: TyNode) extends AstType 
case class AstParameterListImpl(parameterListContent: List[AstValDef]) extends AstParameterList 
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
trait AstArgumentLists() extends AstNode with HasArgumentListsContent {
  def argumentListsContent: List[AstArgumentList]
}
trait AstArgumentList() extends AstNode with HasArgumentListContent {
  def argumentListContent: List[AstArgument]
}
trait AstApply() extends AstNode with HasApplicant with HasArguments {
  def applicant: AstNode
  def arguments: AstArgumentLists
}
trait AstDecls() extends AstNode with HasDecls {
  def decls: List[AstNode]
}
trait AstLiteralString() extends AstNode with HasLiteralString {
  def literalString: String
}
trait AstArgument() extends AstNode with HasName with HasValue {
  def name: String
  def value: Option[AstNode]
}
trait AstDirective() extends AstNode with HasDirective {
  def directive: String
}
trait AstFunctionDecl() extends AstNode with HasName with HasParameters with HasReturnType with HasDataframe with HasRecords with HasComment with HasBody with HasSchema with HasLanguage with HasOverwrite {
  def name: String
  def parameters: AstParameterLists
  def returnType: TyNode
  def dataframe: Option[Boolean]
  def records: Option[Boolean]
  def comment: Option[String]
  def body: Option[AstNode]
  def schema: Option[String]
  def language: Option[String]
  def overwrite: Option[Boolean]
}
trait AstLiteralUndefined() extends AstNode 
trait AstSelect() extends AstNode with HasQualifier with HasSymbol {
  def qualifier: AstNode
  def symbol: String
}
trait AstParameterLists() extends AstNode with HasParameterListsContent {
  def parameterListsContent: List[AstParameterList]
}
trait AstBlock() extends AstNode with HasStmts {
  def stmts: List[AstNode]
}
trait AstClassDecl() extends AstNode with HasName with HasParameters with HasFields with HasMethods with HasDerives with HasSchema with HasDataframe with HasClassType with HasAccess {
  def name: String
  def parameters: AstParameterLists
  def fields: List[AstValDef]
  def methods: List[AstNode]
  def derives: List[AstNode]
  def schema: Option[String]
  def dataframe: Option[Boolean]
  def classType: Option[String]
  def access: Option[AccessModifier]
}
trait AstIdent() extends AstNode with HasName {
  def name: String
}
trait AstVariableIdentifier() extends AstNode with HasVariableIdentifier {
  def variableIdentifier: String
}
trait AstValDef() extends AstNode with HasName with HasTy with HasValue with HasMutability with HasAutoIncr with HasPrimaryKey {
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
trait AstParameterList() extends AstNode with HasParameterListContent {
  def parameterListContent: List[AstValDef]
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
  def stmts(stmts: Seq[AstNode]): AstProgramBuilder = {
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
class AstArgumentListsBuilder() {
  var argumentListsContent: mutable.ArrayBuffer[AstArgumentList] = mutable.ArrayBuffer.empty
  def argumentListsContent(argumentListsContent: Seq[AstArgumentList]): AstArgumentListsBuilder = {
    this.argumentListsContent ++= argumentListsContent
    this
  }
  def argumentListsContent(argumentListsContent: AstArgumentList): AstArgumentListsBuilder = {
    this.argumentListsContent += argumentListsContent
    this
  }
  def build(): AstArgumentListsImpl = {
    AstArgumentListsImpl(argumentListsContent.toList)
  }
}
class AstArgumentListBuilder() {
  var argumentListContent: mutable.ArrayBuffer[AstArgument] = mutable.ArrayBuffer.empty
  def argumentListContent(argumentListContent: Seq[AstArgument]): AstArgumentListBuilder = {
    this.argumentListContent ++= argumentListContent
    this
  }
  def argumentListContent(argumentListContent: AstArgument): AstArgumentListBuilder = {
    this.argumentListContent += argumentListContent
    this
  }
  def build(): AstArgumentListImpl = {
    AstArgumentListImpl(argumentListContent.toList)
  }
}
class AstApplyBuilder() {
  var applicant: Option[AstNode] = None
  var arguments: Option[AstArgumentLists] = None
  def applicant(applicant: AstNode): AstApplyBuilder = {
    this.applicant = Some(applicant)
    this
  }
  def arguments(arguments: AstArgumentLists): AstApplyBuilder = {
    this.arguments = Some(arguments)
    this
  }
  def build(): AstApplyImpl = {
    AstApplyImpl(applicant.get, arguments.get)
  }
}
class AstDeclsBuilder() {
  var decls: mutable.ArrayBuffer[AstNode] = mutable.ArrayBuffer.empty
  def decls(decls: Seq[AstNode]): AstDeclsBuilder = {
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
class AstFunctionDeclBuilder() {
  var name: Option[String] = None
  var parameters: Option[AstParameterLists] = None
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
  def parameters(parameters: AstParameterLists): AstFunctionDeclBuilder = {
    this.parameters = Some(parameters)
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
  def build(): AstFunctionDeclImpl = {
    AstFunctionDeclImpl(name.get, parameters.get, returnType.get, dataframe, records, comment, body, schema, language, overwrite)
  }
}
class AstLiteralUndefinedBuilder() {
  def build(): AstLiteralUndefinedImpl = {
    AstLiteralUndefinedImpl()
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
  var parameterListsContent: mutable.ArrayBuffer[AstParameterList] = mutable.ArrayBuffer.empty
  def parameterListsContent(parameterListsContent: Seq[AstParameterList]): AstParameterListsBuilder = {
    this.parameterListsContent ++= parameterListsContent
    this
  }
  def parameterListsContent(parameterListsContent: AstParameterList): AstParameterListsBuilder = {
    this.parameterListsContent += parameterListsContent
    this
  }
  def build(): AstParameterListsImpl = {
    AstParameterListsImpl(parameterListsContent.toList)
  }
}
class AstBlockBuilder() {
  var nodes: mutable.ArrayBuffer[AstNode] = mutable.ArrayBuffer.empty
  def nodes(nodes: Seq[AstNode]): AstBlockBuilder = {
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
  var parameters: Option[AstParameterLists] = None
  var fields: mutable.ArrayBuffer[AstValDef] = mutable.ArrayBuffer.empty
  var methods: mutable.ArrayBuffer[AstNode] = mutable.ArrayBuffer.empty
  var derives: mutable.ArrayBuffer[AstNode] = mutable.ArrayBuffer.empty
  var schema: Option[String] = None
  var dataframe: Option[Boolean] = None
  var classType: Option[String] = None
  var access: Option[AccessModifier] = None
  def name(name: String): AstClassDeclBuilder = {
    this.name = Some(name)
    this
  }
  def parameters(parameters: AstParameterLists): AstClassDeclBuilder = {
    this.parameters = Some(parameters)
    this
  }
  def fields(fields: Seq[AstValDef]): AstClassDeclBuilder = {
    this.fields ++= fields
    this
  }
  def methods(methods: Seq[AstNode]): AstClassDeclBuilder = {
    this.methods ++= methods
    this
  }
  def derives(derives: Seq[AstNode]): AstClassDeclBuilder = {
    this.derives ++= derives
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
  def field(field: AstValDef): AstClassDeclBuilder = {
    this.fields += field
    this
  }
  def method(method: AstNode): AstClassDeclBuilder = {
    this.methods += method
    this
  }
  def derive(derive: AstNode): AstClassDeclBuilder = {
    this.derives += derive
    this
  }
  def build(): AstClassDeclImpl = {
    AstClassDeclImpl(name.get, parameters.get, fields.toList, methods.toList, derives.toList, schema, dataframe, classType, access)
  }
}
class AstIdentBuilder() {
  var name: Option[String] = None
  def name(name: String): AstIdentBuilder = {
    this.name = Some(name)
    this
  }
  def build(): AstIdentImpl = {
    AstIdentImpl(name.get)
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
  var parameterListContent: mutable.ArrayBuffer[AstValDef] = mutable.ArrayBuffer.empty
  def parameterListContent(parameterListContent: Seq[AstValDef]): AstParameterListBuilder = {
    this.parameterListContent ++= parameterListContent
    this
  }
  def parameterListContent(parameterListContent: AstValDef): AstParameterListBuilder = {
    this.parameterListContent += parameterListContent
    this
  }
  def build(): AstParameterListImpl = {
    AstParameterListImpl(parameterListContent.toList)
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
package unidef.common.ty

import scala.collection.mutable


trait HasDerives() extends TyNode {
  def derives: Option[List[String]]
}
trait HasRef() extends TyNode {
  def ref: String
}
trait HasPreserveCase() extends TyNode {
  def preserveCase: Option[Boolean]
}
trait HasIsBinary() extends TyNode {
  def isBinary: Boolean
}
trait HasLifetime() extends TyNode {
  def lifetime: Option[LifeTime]
}
trait HasTimeUnit() extends TyNode {
  def timeUnit: Option[java.util.concurrent.TimeUnit]
}
trait HasTimezone() extends TyNode {
  def timezone: Option[java.util.TimeZone]
}
trait HasValues() extends TyNode {
  def values: List[TyNode]
}
trait HasApplicant() extends TyNode {
  def applicant: String
}
trait HasScale() extends TyNode {
  def scale: Option[Int]
}
trait HasSmart() extends TyNode {
  def smart: Option[Boolean]
}
trait HasName() extends TyNode {
  def name: Option[String]
}
trait HasMutable() extends TyNode {
  def mutable: Option[Boolean]
}
trait HasOk() extends TyNode {
  def ok: TyNode
}
trait HasSimpleEnum() extends TyNode {
  def simpleEnum: Option[Boolean]
}
trait HasArguments() extends TyNode {
  def arguments: List[Boolean]
}
trait HasMutability() extends TyNode {
  def mutability: Option[Boolean]
}
trait HasFields() extends TyNode {
  def fields: Option[List[TyField]]
}
trait HasDataframe() extends TyNode {
  def dataframe: Option[Boolean]
}
trait HasComment() extends TyNode {
  def comment: Option[String]
}
trait HasReferee() extends TyNode {
  def referee: TyNode
}
trait HasErr() extends TyNode {
  def err: TyNode
}
trait HasCode() extends TyNode {
  def code: Option[Int]
}
trait HasSigned() extends TyNode {
  def signed: Option[Boolean]
}
trait HasHasTimeZone() extends TyNode {
  def hasTimeZone: Option[Boolean]
}
trait HasNames() extends TyNode {
  def names: List[String]
}
trait HasMapType() extends TyNode {
  def mapType: Option[String]
}
trait HasVariants() extends TyNode {
  def variants: List[TyVariant]
}
trait HasCharset() extends TyNode {
  def charset: Option[String]
}
trait HasAttributes() extends TyNode {
  def attributes: Option[List[String]]
}
trait HasKey() extends TyNode {
  def key: TyNode
}
trait HasPrecision() extends TyNode {
  def precision: Option[Int]
}
trait HasSymbol() extends TyNode {
  def symbol: String
}
trait HasValue() extends TyNode {
  def value: TyNode
}
trait HasPointed() extends TyNode {
  def pointed: TyNode
}
trait HasBitSize() extends TyNode {
  def bitSize: Option[BitSize]
}
trait HasSchema() extends TyNode {
  def schema: Option[String]
}
case class TyReferenceImpl(referee: TyNode, lifetime: Option[LifeTime]) extends TyReference 
case class TyAnyImpl() extends TyAny 
case class TyTimeStampImpl(hasTimeZone: Option[Boolean], timeUnit: Option[java.util.concurrent.TimeUnit]) extends TyTimeStamp 
case class TyTypeVarImpl(name: Option[String]) extends TyTypeVar 
case class TyDecimalImpl(precision: Option[Int], scale: Option[Int]) extends TyDecimal 
case class TyApplyImpl(applicant: String, arguments: List[Boolean]) extends TyApply 
case class TyTupleImpl(values: List[TyNode]) extends TyTuple 
case class TyStructImpl(name: Option[String], fields: Option[List[TyField]], derives: Option[List[String]], attributes: Option[List[String]], dataframe: Option[Boolean], schema: Option[String], comment: Option[String]) extends TyStruct 
case class TyJsonAnyImpl(isBinary: Boolean) extends TyJsonAny 
case class TyByteImpl(signed: Option[Boolean]) extends TyByte 
case class TyRecordImpl() extends TyRecord 
case class TyRealImpl() extends TyReal 
case class TyUnionImpl(values: List[TyNode]) extends TyUnion 
case class TyPointerImpl(pointed: TyNode, mutable: Option[Boolean], smart: Option[Boolean]) extends TyPointer 
case class TyFieldImpl(name: Option[String], value: TyNode, mutability: Option[Boolean]) extends TyField 
case class TyMapImpl(key: TyNode, value: TyNode, mapType: Option[String]) extends TyMap 
case class TyFloatImpl(bitSize: Option[BitSize]) extends TyFloat 
case class TyJsonImpl() extends TyJson 
case class TyUuidImpl() extends TyUuid 
case class TyResultImpl(ok: TyNode, err: TyNode) extends TyResult 
case class TySetImpl(value: TyNode) extends TySet 
case class TyOidImpl() extends TyOid 
case class TyOptionImpl(value: TyNode) extends TyOption 
case class TyVariantImpl(names: List[String], code: Option[Int]) extends TyVariant 
case class TyUnknownImpl() extends TyUnknown 
case class TyStringImpl() extends TyString 
case class TyCharImpl(bitSize: Option[BitSize], charset: Option[String]) extends TyChar 
case class TyNullImpl() extends TyNull 
case class TyNamedImpl(ref: String, preserveCase: Option[Boolean]) extends TyNamed 
case class TyObjectImpl() extends TyObject 
case class TySelectImpl(value: TyNode, symbol: String) extends TySelect 
case class TyByteArrayImpl() extends TyByteArray 
case class TyNothingImpl() extends TyNothing 
case class TyIdentImpl(name: Option[String]) extends TyIdent 
case class TyBooleanImpl() extends TyBoolean 
case class TyEnumImpl(variants: List[TyVariant], simpleEnum: Option[Boolean], name: Option[String], value: TyNode, schema: Option[String]) extends TyEnum 
case class TyInetImpl() extends TyInet 
case class TyDateTimeImpl(timezone: Option[java.util.TimeZone]) extends TyDateTime 
case class TyKeyValueImpl(key: TyNode, value: TyNode) extends TyKeyValue 
case class TyClassImpl() extends TyClass 
case class TySeqImpl(value: TyNode) extends TySeq 
case class TyNumericImpl() extends TyNumeric 
case class TyIntegerImpl(bitSize: Option[BitSize], signed: Option[Boolean]) extends TyInteger 
case class TyUnitImpl() extends TyUnit 
case class TyListImpl(value: TyNode) extends TyList 
case class TyUndefinedImpl() extends TyUndefined 
case class TyJsonObjectImpl(isBinary: Boolean) extends TyJsonObject 
trait TyReference() extends TyNode with HasReferee with HasLifetime {
  def referee: TyNode
  def lifetime: Option[LifeTime]
}
trait TyAny() extends TyNode 
trait TyTimeStamp() extends TyNode with HasHasTimeZone with HasTimeUnit {
  def hasTimeZone: Option[Boolean]
  def timeUnit: Option[java.util.concurrent.TimeUnit]
}
trait TyTypeVar() extends TyNode with HasName {
  def name: Option[String]
}
trait TyDecimal() extends TyNode with TyReal with HasPrecision with HasScale {
  def precision: Option[Int]
  def scale: Option[Int]
}
trait TyApply() extends TyNode with HasApplicant with HasArguments {
  def applicant: String
  def arguments: List[Boolean]
}
trait TyTuple() extends TyNode with HasValues {
  def values: List[TyNode]
}
trait TyStruct() extends TyNode with TyClass with HasName with HasFields with HasDerives with HasAttributes with HasDataframe with HasSchema with HasComment {
  def name: Option[String]
  def fields: Option[List[TyField]]
  def derives: Option[List[String]]
  def attributes: Option[List[String]]
  def dataframe: Option[Boolean]
  def schema: Option[String]
  def comment: Option[String]
}
trait TyJsonAny() extends TyNode with HasIsBinary {
  def isBinary: Boolean
}
trait TyByte() extends TyNode with HasSigned {
  def signed: Option[Boolean]
}
trait TyRecord() extends TyNode 
trait TyReal() extends TyNode with TyNumeric 
trait TyUnion() extends TyNode with HasValues {
  def values: List[TyNode]
}
trait TyPointer() extends TyNode with HasPointed with HasMutable with HasSmart {
  def pointed: TyNode
  def mutable: Option[Boolean]
  def smart: Option[Boolean]
}
trait TyField() extends TyNode with HasName with HasValue with HasMutability {
  def name: Option[String]
  def value: TyNode
  def mutability: Option[Boolean]
}
trait TyMap() extends TyNode with HasKey with HasValue with HasMapType {
  def key: TyNode
  def value: TyNode
  def mapType: Option[String]
}
trait TyFloat() extends TyNode with TyReal with HasBitSize {
  def bitSize: Option[BitSize]
}
trait TyJson() extends TyNode 
trait TyUuid() extends TyNode 
trait TyResult() extends TyNode with HasOk with HasErr {
  def ok: TyNode
  def err: TyNode
}
trait TySet() extends TyNode with HasValue {
  def value: TyNode
}
trait TyOid() extends TyNode 
trait TyOption() extends TyNode with HasValue {
  def value: TyNode
}
trait TyVariant() extends TyNode with HasNames with HasCode {
  def names: List[String]
  def code: Option[Int]
}
trait TyUnknown() extends TyNode 
trait TyString() extends TyNode 
trait TyChar() extends TyNode with HasBitSize with HasCharset {
  def bitSize: Option[BitSize]
  def charset: Option[String]
}
trait TyNull() extends TyNode 
trait TyNamed() extends TyNode with HasRef with HasPreserveCase {
  def ref: String
  def preserveCase: Option[Boolean]
}
trait TyObject() extends TyNode 
trait TySelect() extends TyNode with HasValue with HasSymbol {
  def value: TyNode
  def symbol: String
}
trait TyByteArray() extends TyNode 
trait TyNothing() extends TyNode 
trait TyIdent() extends TyNode with HasName {
  def name: Option[String]
}
trait TyBoolean() extends TyNode 
trait TyEnum() extends TyNode with HasVariants with HasSimpleEnum with HasName with HasValue with HasSchema {
  def variants: List[TyVariant]
  def simpleEnum: Option[Boolean]
  def name: Option[String]
  def value: TyNode
  def schema: Option[String]
}
trait TyInet() extends TyNode 
trait TyDateTime() extends TyNode with HasTimezone {
  def timezone: Option[java.util.TimeZone]
}
trait TyKeyValue() extends TyNode with HasKey with HasValue {
  def key: TyNode
  def value: TyNode
}
trait TyClass() extends TyNode 
trait TySeq() extends TyNode with HasValue {
  def value: TyNode
}
trait TyNumeric() extends TyNode 
trait TyInteger() extends TyNode with TyNumeric with HasBitSize with HasSigned {
  def bitSize: Option[BitSize]
  def signed: Option[Boolean]
}
trait TyUnit() extends TyNode 
trait TyList() extends TyNode with HasValue {
  def value: TyNode
}
trait TyUndefined() extends TyNode 
trait TyJsonObject() extends TyNode with HasIsBinary {
  def isBinary: Boolean
}
class TyReferenceBuilder() {
  var referee: Option[TyNode] = None
  var lifetime: Option[LifeTime] = None
  def referee(referee: TyNode): TyReferenceBuilder = {
    this.referee = Some(referee)
    this
  }
  def lifetime(lifetime: LifeTime): TyReferenceBuilder = {
    this.lifetime = Some(lifetime)
    this
  }
  def lifetime(lifetime: Option[LifeTime]): TyReferenceBuilder = {
    this.lifetime = lifetime
    this
  }
  def build(): TyReferenceImpl = {
    TyReferenceImpl(referee.get, lifetime)
  }
}
class TyAnyBuilder() {
  def build(): TyAnyImpl = {
    TyAnyImpl()
  }
}
class TyTimeStampBuilder() {
  var hasTimeZone: Option[Boolean] = None
  var timeUnit: Option[java.util.concurrent.TimeUnit] = None
  def hasTimeZone(hasTimeZone: Boolean): TyTimeStampBuilder = {
    this.hasTimeZone = Some(hasTimeZone)
    this
  }
  def timeUnit(timeUnit: java.util.concurrent.TimeUnit): TyTimeStampBuilder = {
    this.timeUnit = Some(timeUnit)
    this
  }
  def hasTimeZone(hasTimeZone: Option[Boolean]): TyTimeStampBuilder = {
    this.hasTimeZone = hasTimeZone
    this
  }
  def timeUnit(timeUnit: Option[java.util.concurrent.TimeUnit]): TyTimeStampBuilder = {
    this.timeUnit = timeUnit
    this
  }
  def build(): TyTimeStampImpl = {
    TyTimeStampImpl(hasTimeZone, timeUnit)
  }
}
class TyTypeVarBuilder() {
  var name: Option[String] = None
  def name(name: String): TyTypeVarBuilder = {
    this.name = Some(name)
    this
  }
  def name(name: Option[String]): TyTypeVarBuilder = {
    this.name = name
    this
  }
  def build(): TyTypeVarImpl = {
    TyTypeVarImpl(name)
  }
}
class TyDecimalBuilder() {
  var precision: Option[Int] = None
  var scale: Option[Int] = None
  def precision(precision: Int): TyDecimalBuilder = {
    this.precision = Some(precision)
    this
  }
  def scale(scale: Int): TyDecimalBuilder = {
    this.scale = Some(scale)
    this
  }
  def precision(precision: Option[Int]): TyDecimalBuilder = {
    this.precision = precision
    this
  }
  def scale(scale: Option[Int]): TyDecimalBuilder = {
    this.scale = scale
    this
  }
  def build(): TyDecimalImpl = {
    TyDecimalImpl(precision, scale)
  }
}
class TyApplyBuilder() {
  var applicant: Option[String] = None
  var arguments: mutable.ArrayBuffer[Boolean] = mutable.ArrayBuffer.empty
  def applicant(applicant: String): TyApplyBuilder = {
    this.applicant = Some(applicant)
    this
  }
  def arguments(arguments: Seq[Boolean]): TyApplyBuilder = {
    this.arguments ++= arguments
    this
  }
  def argument(argument: Boolean): TyApplyBuilder = {
    this.arguments += argument
    this
  }
  def build(): TyApplyImpl = {
    TyApplyImpl(applicant.get, arguments.toList)
  }
}
class TyTupleBuilder() {
  var values: mutable.ArrayBuffer[TyNode] = mutable.ArrayBuffer.empty
  def values(values: Seq[TyNode]): TyTupleBuilder = {
    this.values ++= values
    this
  }
  def value(value: TyNode): TyTupleBuilder = {
    this.values += value
    this
  }
  def build(): TyTupleImpl = {
    TyTupleImpl(values.toList)
  }
}
class TyStructBuilder() {
  var name: Option[String] = None
  var fields: Option[List[TyField]] = None
  var derives: Option[List[String]] = None
  var attributes: Option[List[String]] = None
  var dataframe: Option[Boolean] = None
  var schema: Option[String] = None
  var comment: Option[String] = None
  def name(name: String): TyStructBuilder = {
    this.name = Some(name)
    this
  }
  def fields(fields: List[TyField]): TyStructBuilder = {
    this.fields = Some(fields)
    this
  }
  def derives(derives: List[String]): TyStructBuilder = {
    this.derives = Some(derives)
    this
  }
  def attributes(attributes: List[String]): TyStructBuilder = {
    this.attributes = Some(attributes)
    this
  }
  def dataframe(dataframe: Boolean): TyStructBuilder = {
    this.dataframe = Some(dataframe)
    this
  }
  def schema(schema: String): TyStructBuilder = {
    this.schema = Some(schema)
    this
  }
  def comment(comment: String): TyStructBuilder = {
    this.comment = Some(comment)
    this
  }
  def name(name: Option[String]): TyStructBuilder = {
    this.name = name
    this
  }
  def fields(fields: Option[List[TyField]]): TyStructBuilder = {
    this.fields = fields
    this
  }
  def derives(derives: Option[List[String]]): TyStructBuilder = {
    this.derives = derives
    this
  }
  def attributes(attributes: Option[List[String]]): TyStructBuilder = {
    this.attributes = attributes
    this
  }
  def dataframe(dataframe: Option[Boolean]): TyStructBuilder = {
    this.dataframe = dataframe
    this
  }
  def schema(schema: Option[String]): TyStructBuilder = {
    this.schema = schema
    this
  }
  def comment(comment: Option[String]): TyStructBuilder = {
    this.comment = comment
    this
  }
  def build(): TyStructImpl = {
    TyStructImpl(name, fields, derives, attributes, dataframe, schema, comment)
  }
}
class TyJsonAnyBuilder() {
  var isBinary: Option[Boolean] = None
  def isBinary(isBinary: Boolean): TyJsonAnyBuilder = {
    this.isBinary = Some(isBinary)
    this
  }
  def build(): TyJsonAnyImpl = {
    TyJsonAnyImpl(isBinary.get)
  }
}
class TyByteBuilder() {
  var signed: Option[Boolean] = None
  def signed(signed: Boolean): TyByteBuilder = {
    this.signed = Some(signed)
    this
  }
  def signed(signed: Option[Boolean]): TyByteBuilder = {
    this.signed = signed
    this
  }
  def build(): TyByteImpl = {
    TyByteImpl(signed)
  }
}
class TyRecordBuilder() {
  def build(): TyRecordImpl = {
    TyRecordImpl()
  }
}
class TyRealBuilder() {
  def build(): TyRealImpl = {
    TyRealImpl()
  }
}
class TyUnionBuilder() {
  var values: mutable.ArrayBuffer[TyNode] = mutable.ArrayBuffer.empty
  def values(values: Seq[TyNode]): TyUnionBuilder = {
    this.values ++= values
    this
  }
  def value(value: TyNode): TyUnionBuilder = {
    this.values += value
    this
  }
  def build(): TyUnionImpl = {
    TyUnionImpl(values.toList)
  }
}
class TyPointerBuilder() {
  var pointed: Option[TyNode] = None
  var mutable: Option[Boolean] = None
  var smart: Option[Boolean] = None
  def pointed(pointed: TyNode): TyPointerBuilder = {
    this.pointed = Some(pointed)
    this
  }
  def mutable(mutable: Boolean): TyPointerBuilder = {
    this.mutable = Some(mutable)
    this
  }
  def smart(smart: Boolean): TyPointerBuilder = {
    this.smart = Some(smart)
    this
  }
  def mutable(mutable: Option[Boolean]): TyPointerBuilder = {
    this.mutable = mutable
    this
  }
  def smart(smart: Option[Boolean]): TyPointerBuilder = {
    this.smart = smart
    this
  }
  def build(): TyPointerImpl = {
    TyPointerImpl(pointed.get, mutable, smart)
  }
}
class TyFieldBuilder() {
  var name: Option[String] = None
  var value: Option[TyNode] = None
  var mutability: Option[Boolean] = None
  def name(name: String): TyFieldBuilder = {
    this.name = Some(name)
    this
  }
  def value(value: TyNode): TyFieldBuilder = {
    this.value = Some(value)
    this
  }
  def mutability(mutability: Boolean): TyFieldBuilder = {
    this.mutability = Some(mutability)
    this
  }
  def name(name: Option[String]): TyFieldBuilder = {
    this.name = name
    this
  }
  def mutability(mutability: Option[Boolean]): TyFieldBuilder = {
    this.mutability = mutability
    this
  }
  def build(): TyFieldImpl = {
    TyFieldImpl(name, value.get, mutability)
  }
}
class TyMapBuilder() {
  var key: Option[TyNode] = None
  var value: Option[TyNode] = None
  var mapType: Option[String] = None
  def key(key: TyNode): TyMapBuilder = {
    this.key = Some(key)
    this
  }
  def value(value: TyNode): TyMapBuilder = {
    this.value = Some(value)
    this
  }
  def mapType(mapType: String): TyMapBuilder = {
    this.mapType = Some(mapType)
    this
  }
  def mapType(mapType: Option[String]): TyMapBuilder = {
    this.mapType = mapType
    this
  }
  def build(): TyMapImpl = {
    TyMapImpl(key.get, value.get, mapType)
  }
}
class TyFloatBuilder() {
  var bitSize: Option[BitSize] = None
  def bitSize(bitSize: BitSize): TyFloatBuilder = {
    this.bitSize = Some(bitSize)
    this
  }
  def bitSize(bitSize: Option[BitSize]): TyFloatBuilder = {
    this.bitSize = bitSize
    this
  }
  def build(): TyFloatImpl = {
    TyFloatImpl(bitSize)
  }
}
class TyJsonBuilder() {
  def build(): TyJsonImpl = {
    TyJsonImpl()
  }
}
class TyUuidBuilder() {
  def build(): TyUuidImpl = {
    TyUuidImpl()
  }
}
class TyResultBuilder() {
  var ok: Option[TyNode] = None
  var err: Option[TyNode] = None
  def ok(ok: TyNode): TyResultBuilder = {
    this.ok = Some(ok)
    this
  }
  def err(err: TyNode): TyResultBuilder = {
    this.err = Some(err)
    this
  }
  def build(): TyResultImpl = {
    TyResultImpl(ok.get, err.get)
  }
}
class TySetBuilder() {
  var value: Option[TyNode] = None
  def value(value: TyNode): TySetBuilder = {
    this.value = Some(value)
    this
  }
  def build(): TySetImpl = {
    TySetImpl(value.get)
  }
}
class TyOidBuilder() {
  def build(): TyOidImpl = {
    TyOidImpl()
  }
}
class TyOptionBuilder() {
  var value: Option[TyNode] = None
  def value(value: TyNode): TyOptionBuilder = {
    this.value = Some(value)
    this
  }
  def build(): TyOptionImpl = {
    TyOptionImpl(value.get)
  }
}
class TyVariantBuilder() {
  var names: mutable.ArrayBuffer[String] = mutable.ArrayBuffer.empty
  var code: Option[Int] = None
  def names(names: Seq[String]): TyVariantBuilder = {
    this.names ++= names
    this
  }
  def code(code: Int): TyVariantBuilder = {
    this.code = Some(code)
    this
  }
  def code(code: Option[Int]): TyVariantBuilder = {
    this.code = code
    this
  }
  def name(name: String): TyVariantBuilder = {
    this.names += name
    this
  }
  def build(): TyVariantImpl = {
    TyVariantImpl(names.toList, code)
  }
}
class TyUnknownBuilder() {
  def build(): TyUnknownImpl = {
    TyUnknownImpl()
  }
}
class TyStringBuilder() {
  def build(): TyStringImpl = {
    TyStringImpl()
  }
}
class TyCharBuilder() {
  var bitSize: Option[BitSize] = None
  var charset: Option[String] = None
  def bitSize(bitSize: BitSize): TyCharBuilder = {
    this.bitSize = Some(bitSize)
    this
  }
  def charset(charset: String): TyCharBuilder = {
    this.charset = Some(charset)
    this
  }
  def bitSize(bitSize: Option[BitSize]): TyCharBuilder = {
    this.bitSize = bitSize
    this
  }
  def charset(charset: Option[String]): TyCharBuilder = {
    this.charset = charset
    this
  }
  def build(): TyCharImpl = {
    TyCharImpl(bitSize, charset)
  }
}
class TyNullBuilder() {
  def build(): TyNullImpl = {
    TyNullImpl()
  }
}
class TyNamedBuilder() {
  var ref: Option[String] = None
  var preserveCase: Option[Boolean] = None
  def ref(ref: String): TyNamedBuilder = {
    this.ref = Some(ref)
    this
  }
  def preserveCase(preserveCase: Boolean): TyNamedBuilder = {
    this.preserveCase = Some(preserveCase)
    this
  }
  def preserveCase(preserveCase: Option[Boolean]): TyNamedBuilder = {
    this.preserveCase = preserveCase
    this
  }
  def build(): TyNamedImpl = {
    TyNamedImpl(ref.get, preserveCase)
  }
}
class TyObjectBuilder() {
  def build(): TyObjectImpl = {
    TyObjectImpl()
  }
}
class TySelectBuilder() {
  var value: Option[TyNode] = None
  var symbol: Option[String] = None
  def value(value: TyNode): TySelectBuilder = {
    this.value = Some(value)
    this
  }
  def symbol(symbol: String): TySelectBuilder = {
    this.symbol = Some(symbol)
    this
  }
  def build(): TySelectImpl = {
    TySelectImpl(value.get, symbol.get)
  }
}
class TyByteArrayBuilder() {
  def build(): TyByteArrayImpl = {
    TyByteArrayImpl()
  }
}
class TyNothingBuilder() {
  def build(): TyNothingImpl = {
    TyNothingImpl()
  }
}
class TyIdentBuilder() {
  var name: Option[String] = None
  def name(name: String): TyIdentBuilder = {
    this.name = Some(name)
    this
  }
  def name(name: Option[String]): TyIdentBuilder = {
    this.name = name
    this
  }
  def build(): TyIdentImpl = {
    TyIdentImpl(name)
  }
}
class TyBooleanBuilder() {
  def build(): TyBooleanImpl = {
    TyBooleanImpl()
  }
}
class TyEnumBuilder() {
  var variants: mutable.ArrayBuffer[TyVariant] = mutable.ArrayBuffer.empty
  var simpleEnum: Option[Boolean] = None
  var name: Option[String] = None
  var value: Option[TyNode] = None
  var schema: Option[String] = None
  def variants(variants: Seq[TyVariant]): TyEnumBuilder = {
    this.variants ++= variants
    this
  }
  def simpleEnum(simpleEnum: Boolean): TyEnumBuilder = {
    this.simpleEnum = Some(simpleEnum)
    this
  }
  def name(name: String): TyEnumBuilder = {
    this.name = Some(name)
    this
  }
  def value(value: TyNode): TyEnumBuilder = {
    this.value = Some(value)
    this
  }
  def schema(schema: String): TyEnumBuilder = {
    this.schema = Some(schema)
    this
  }
  def simpleEnum(simpleEnum: Option[Boolean]): TyEnumBuilder = {
    this.simpleEnum = simpleEnum
    this
  }
  def name(name: Option[String]): TyEnumBuilder = {
    this.name = name
    this
  }
  def schema(schema: Option[String]): TyEnumBuilder = {
    this.schema = schema
    this
  }
  def variant(variant: TyVariant): TyEnumBuilder = {
    this.variants += variant
    this
  }
  def build(): TyEnumImpl = {
    TyEnumImpl(variants.toList, simpleEnum, name, value.get, schema)
  }
}
class TyInetBuilder() {
  def build(): TyInetImpl = {
    TyInetImpl()
  }
}
class TyDateTimeBuilder() {
  var timezone: Option[java.util.TimeZone] = None
  def timezone(timezone: java.util.TimeZone): TyDateTimeBuilder = {
    this.timezone = Some(timezone)
    this
  }
  def timezone(timezone: Option[java.util.TimeZone]): TyDateTimeBuilder = {
    this.timezone = timezone
    this
  }
  def build(): TyDateTimeImpl = {
    TyDateTimeImpl(timezone)
  }
}
class TyKeyValueBuilder() {
  var key: Option[TyNode] = None
  var value: Option[TyNode] = None
  def key(key: TyNode): TyKeyValueBuilder = {
    this.key = Some(key)
    this
  }
  def value(value: TyNode): TyKeyValueBuilder = {
    this.value = Some(value)
    this
  }
  def build(): TyKeyValueImpl = {
    TyKeyValueImpl(key.get, value.get)
  }
}
class TyClassBuilder() {
  def build(): TyClassImpl = {
    TyClassImpl()
  }
}
class TySeqBuilder() {
  var value: Option[TyNode] = None
  def value(value: TyNode): TySeqBuilder = {
    this.value = Some(value)
    this
  }
  def build(): TySeqImpl = {
    TySeqImpl(value.get)
  }
}
class TyNumericBuilder() {
  def build(): TyNumericImpl = {
    TyNumericImpl()
  }
}
class TyIntegerBuilder() {
  var bitSize: Option[BitSize] = None
  var signed: Option[Boolean] = None
  def bitSize(bitSize: BitSize): TyIntegerBuilder = {
    this.bitSize = Some(bitSize)
    this
  }
  def signed(signed: Boolean): TyIntegerBuilder = {
    this.signed = Some(signed)
    this
  }
  def bitSize(bitSize: Option[BitSize]): TyIntegerBuilder = {
    this.bitSize = bitSize
    this
  }
  def signed(signed: Option[Boolean]): TyIntegerBuilder = {
    this.signed = signed
    this
  }
  def build(): TyIntegerImpl = {
    TyIntegerImpl(bitSize, signed)
  }
}
class TyUnitBuilder() {
  def build(): TyUnitImpl = {
    TyUnitImpl()
  }
}
class TyListBuilder() {
  var value: Option[TyNode] = None
  def value(value: TyNode): TyListBuilder = {
    this.value = Some(value)
    this
  }
  def build(): TyListImpl = {
    TyListImpl(value.get)
  }
}
class TyUndefinedBuilder() {
  def build(): TyUndefinedImpl = {
    TyUndefinedImpl()
  }
}
class TyJsonObjectBuilder() {
  var isBinary: Option[Boolean] = None
  def isBinary(isBinary: Boolean): TyJsonObjectBuilder = {
    this.isBinary = Some(isBinary)
    this
  }
  def build(): TyJsonObjectImpl = {
    TyJsonObjectImpl(isBinary.get)
  }
}
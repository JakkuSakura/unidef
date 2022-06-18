package unidef.common.ty

import scala.collection.mutable


trait HasValues() extends TyNode {
  def values: List[TyNode]
}
trait HasDerives() extends TyNode {
  def derives: Option[List[String]]
}
trait HasOk() extends TyNode {
  def ok: TyNode
}
trait HasSimpleEnum() extends TyNode {
  def simpleEnum: Option[Boolean]
}
trait HasRef() extends TyNode {
  def ref: String
}
trait HasAttributes() extends TyNode {
  def attributes: Option[List[String]]
}
trait HasIsBinary() extends TyNode {
  def isBinary: Boolean
}
trait HasTimeUnit() extends TyNode {
  def timeUnit: Option[java.util.concurrent.TimeUnit]
}
trait HasTimezone() extends TyNode {
  def timezone: Option[java.util.TimeZone]
}
trait HasScale() extends TyNode {
  def scale: Option[Int]
}
trait HasName() extends TyNode {
  def name: Option[String]
}
trait HasComment() extends TyNode {
  def comment: Option[String]
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
trait HasVariants() extends TyNode {
  def variants: List[TyVariant]
}
trait HasKey() extends TyNode {
  def key: TyNode
}
trait HasPrecision() extends TyNode {
  def precision: Option[Int]
}
trait HasValue() extends TyNode {
  def value: TyNode
}
trait HasBitSize() extends TyNode {
  def bitSize: Option[BitSize]
}
trait HasSchema() extends TyNode {
  def schema: Option[String]
}
case class TyAnyImpl() extends TyAny 
case class TyTimeStampImpl(hasTimeZone: Option[Boolean], timeUnit: Option[java.util.concurrent.TimeUnit]) extends TyTimeStamp 
case class TyDecimalImpl(precision: Option[Int], scale: Option[Int]) extends TyDecimal 
case class TyByteArrayImpl() extends TyByteArray 
case class TyTupleImpl(values: List[TyNode]) extends TyTuple 
case class TyStructImpl(name: Option[String], fields: Option[List[TyField]], derives: Option[List[String]], attributes: Option[List[String]], dataframe: Option[Boolean], schema: Option[String], comment: Option[String]) extends TyStruct 
case class TyClassImpl() extends TyClass 
case class TyJsonAnyImpl(isBinary: Boolean) extends TyJsonAny 
case class TyRecordImpl() extends TyRecord 
case class TyRealImpl() extends TyReal 
case class TyUnionImpl(values: List[TyNode]) extends TyUnion 
case class TyFieldImpl(name: Option[String], value: TyNode, mutability: Option[Boolean]) extends TyField 
case class TyMapImpl(key: TyNode, value: TyNode) extends TyMap 
case class TyFloatImpl(bitSize: Option[BitSize]) extends TyFloat 
case class TyJsonImpl() extends TyJson 
case class TyUuidImpl() extends TyUuid 
case class TyResultImpl(ok: TyNode, err: TyNode) extends TyResult 
case class TySetImpl(value: TyNode) extends TySet 
case class TyOidImpl() extends TyOid 
case class TyTypeVarImpl(name: Option[String]) extends TyTypeVar 
case class TyOptionalImpl(value: TyNode) extends TyOptional 
case class TyVariantImpl(names: List[String], code: Option[Int]) extends TyVariant 
case class TyStringImpl() extends TyString 
case class TyCharImpl() extends TyChar 
case class TyNullImpl() extends TyNull 
case class TyNamedImpl(ref: String) extends TyNamed 
case class TyObjectImpl() extends TyObject 
case class TyNothingImpl() extends TyNothing 
case class TyBooleanImpl() extends TyBoolean 
case class TyEnumImpl(variants: List[TyVariant], simpleEnum: Option[Boolean], name: Option[String], value: TyNode, schema: Option[String]) extends TyEnum 
case class TyInetImpl() extends TyInet 
case class TyDateTimeImpl(timezone: Option[java.util.TimeZone]) extends TyDateTime 
case class TyKeyValueImpl(key: TyNode, value: TyNode) extends TyKeyValue 
case class TyNumericImpl() extends TyNumeric 
case class TyIntegerImpl(bitSize: Option[BitSize], signed: Option[Boolean]) extends TyInteger 
case class TyUnitImpl() extends TyUnit 
case class TyListImpl(value: TyNode) extends TyList 
case class TyUndefinedImpl() extends TyUndefined 
case class TyJsonObjectImpl(isBinary: Boolean) extends TyJsonObject 
trait TyAny() extends TyNode 
trait TyTimeStamp() extends TyNode with HasHasTimeZone with HasTimeUnit {
  def hasTimeZone: Option[Boolean]
  def timeUnit: Option[java.util.concurrent.TimeUnit]
}
trait TyDecimal() extends TyNode with TyReal with HasPrecision with HasScale {
  def precision: Option[Int]
  def scale: Option[Int]
}
trait TyByteArray() extends TyNode 
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
trait TyClass() extends TyNode 
trait TyJsonAny() extends TyNode with HasIsBinary {
  def isBinary: Boolean
}
trait TyRecord() extends TyNode 
trait TyReal() extends TyNode with TyNumeric 
trait TyUnion() extends TyNode with HasValues {
  def values: List[TyNode]
}
trait TyField() extends TyNode with HasName with HasValue with HasMutability {
  def name: Option[String]
  def value: TyNode
  def mutability: Option[Boolean]
}
trait TyMap() extends TyNode with HasKey with HasValue {
  def key: TyNode
  def value: TyNode
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
trait TyTypeVar() extends TyNode with HasName {
  def name: Option[String]
}
trait TyOptional() extends TyNode with HasValue {
  def value: TyNode
}
trait TyVariant() extends TyNode with HasNames with HasCode {
  def names: List[String]
  def code: Option[Int]
}
trait TyString() extends TyNode 
trait TyChar() extends TyNode 
trait TyNull() extends TyNode 
trait TyNamed() extends TyNode with HasRef {
  def ref: String
}
trait TyObject() extends TyNode 
trait TyNothing() extends TyNode 
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
class TyByteArrayBuilder() {
  def build(): TyByteArrayImpl = {
    TyByteArrayImpl()
  }
}
class TyTupleBuilder() {
  var values: mutable.ArrayBuffer[TyNode] = mutable.ArrayBuffer.empty
  def values(values: List[TyNode]): TyTupleBuilder = {
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
class TyClassBuilder() {
  def build(): TyClassImpl = {
    TyClassImpl()
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
  def values(values: List[TyNode]): TyUnionBuilder = {
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
  def key(key: TyNode): TyMapBuilder = {
    this.key = Some(key)
    this
  }
  def value(value: TyNode): TyMapBuilder = {
    this.value = Some(value)
    this
  }
  def build(): TyMapImpl = {
    TyMapImpl(key.get, value.get)
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
class TyOptionalBuilder() {
  var value: Option[TyNode] = None
  def value(value: TyNode): TyOptionalBuilder = {
    this.value = Some(value)
    this
  }
  def build(): TyOptionalImpl = {
    TyOptionalImpl(value.get)
  }
}
class TyVariantBuilder() {
  var names: mutable.ArrayBuffer[String] = mutable.ArrayBuffer.empty
  var code: Option[Int] = None
  def names(names: List[String]): TyVariantBuilder = {
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
class TyStringBuilder() {
  def build(): TyStringImpl = {
    TyStringImpl()
  }
}
class TyCharBuilder() {
  def build(): TyCharImpl = {
    TyCharImpl()
  }
}
class TyNullBuilder() {
  def build(): TyNullImpl = {
    TyNullImpl()
  }
}
class TyNamedBuilder() {
  var ref: Option[String] = None
  def ref(ref: String): TyNamedBuilder = {
    this.ref = Some(ref)
    this
  }
  def build(): TyNamedImpl = {
    TyNamedImpl(ref.get)
  }
}
class TyObjectBuilder() {
  def build(): TyObjectImpl = {
    TyObjectImpl()
  }
}
class TyNothingBuilder() {
  def build(): TyNothingImpl = {
    TyNothingImpl()
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
  def variants(variants: List[TyVariant]): TyEnumBuilder = {
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
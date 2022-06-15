package unidef.common.ty

trait HasFields() extends TyNode {
  def getFields: Option[List[TyField]]
}
trait HasPrecision() extends TyNode {
  def getPrecision: Option[Int]
}
trait HasComment() extends TyNode {
  def getComment: Option[String]
}
trait HasAttributes() extends TyNode {
  def getAttributes: Option[List[String]]
}
trait HasErr() extends TyNode {
  def getErr: TyNode
}
trait HasDefaultNone() extends TyNode {
  def getDefaultNone: Option[Boolean]
}
trait HasMutability() extends TyNode {
  def getMutability: Option[Boolean]
}
trait HasScale() extends TyNode {
  def getScale: Option[Int]
}
trait HasDerives() extends TyNode {
  def getDerives: Option[List[String]]
}
trait HasValues() extends TyNode {
  def getValues: List[TyNode]
}
trait HasValue() extends TyNode {
  def getValue: TyNode
}
trait HasSchema() extends TyNode {
  def getSchema: Option[String]
}
trait HasKey() extends TyNode {
  def getKey: TyNode
}
trait HasBitSize() extends TyNode {
  def getBitSize: Option[BitSize]
}
trait HasOk() extends TyNode {
  def getOk: TyNode
}
trait HasName() extends TyNode {
  def getName: Option[String]
}
trait HasSized() extends TyNode {
  def getSized: Option[Boolean]
}
trait HasDataframe() extends TyNode {
  def getDataframe: Option[Boolean]
}
trait HasContent() extends TyNode {
  def getContent: TyNode
}
class TyAnyImpl() extends TyAny 
class TyRealImpl() extends TyReal 
class TyFloatImpl(val bitSize: Option[BitSize]) extends TyFloat {
  override def getBitSize: Option[BitSize] = {
    bitSize
  }
}
class TySetImpl(val content: TyNode) extends TySet {
  override def getContent: TyNode = {
    content
  }
}
class TyOptionalImpl(val content: TyNode) extends TyOptional {
  override def getContent: TyNode = {
    content
  }
}
class TyStringImpl() extends TyString 
class TyObjectImpl() extends TyObject 
class TyDecimalImpl(val precision: Option[Int], val scale: Option[Int]) extends TyDecimal {
  override def getPrecision: Option[Int] = {
    precision
  }
  override def getScale: Option[Int] = {
    scale
  }
}
class TyByteArrayImpl() extends TyByteArray 
class TyInetImpl() extends TyInet 
class TyTupleImpl(val values: List[TyNode]) extends TyTuple {
  override def getValues: List[TyNode] = {
    values
  }
}
class TyStructImpl(val name: Option[String], val fields: Option[List[TyField]], val derives: Option[List[String]], val attributes: Option[List[String]], val dataframe: Option[Boolean], val schema: Option[String], val comment: Option[String]) extends TyStruct {
  override def getName: Option[String] = {
    name
  }
  override def getFields: Option[List[TyField]] = {
    fields
  }
  override def getDerives: Option[List[String]] = {
    derives
  }
  override def getAttributes: Option[List[String]] = {
    attributes
  }
  override def getDataframe: Option[Boolean] = {
    dataframe
  }
  override def getSchema: Option[String] = {
    schema
  }
  override def getComment: Option[String] = {
    comment
  }
}
class TyClassImpl() extends TyClass 
class TyRecordImpl() extends TyRecord 
class TyFieldImpl(val name: Option[String], val value: TyNode, val mutability: Option[Boolean], val defaultNone: Option[Boolean]) extends TyField {
  override def getName: Option[String] = {
    name
  }
  override def getValue: TyNode = {
    value
  }
  override def getMutability: Option[Boolean] = {
    mutability
  }
  override def getDefaultNone: Option[Boolean] = {
    defaultNone
  }
}
class TyMapImpl(val key: TyNode, val value: TyNode) extends TyMap {
  override def getKey: TyNode = {
    key
  }
  override def getValue: TyNode = {
    value
  }
}
class TyUuidImpl() extends TyUuid 
class TyResultImpl(val ok: TyNode, val err: TyNode) extends TyResult {
  override def getOk: TyNode = {
    ok
  }
  override def getErr: TyNode = {
    err
  }
}
class TyCharImpl() extends TyChar 
class TyNullImpl() extends TyNull 
class TyNothingImpl() extends TyNothing 
class TyBooleanImpl() extends TyBoolean 
class TyNumericImpl() extends TyNumeric 
class TyIntegerImpl(val bitSize: Option[BitSize], val sized: Option[Boolean]) extends TyInteger {
  override def getBitSize: Option[BitSize] = {
    bitSize
  }
  override def getSized: Option[Boolean] = {
    sized
  }
}
class TyUnitImpl() extends TyUnit 
class TyListImpl(val content: TyNode) extends TyList {
  override def getContent: TyNode = {
    content
  }
}
class TyUndefinedImpl() extends TyUndefined 
trait TyAny() extends TyNode 
trait TyReal() extends TyNode with TyNumeric 
trait TyFloat() extends TyNode with TyReal with HasBitSize {
  def getBitSize: Option[BitSize]
}
trait TySet() extends TyNode with HasContent {
  def getContent: TyNode
}
trait TyOptional() extends TyNode with HasContent {
  def getContent: TyNode
}
trait TyString() extends TyNode 
trait TyObject() extends TyNode 
trait TyDecimal() extends TyNode with TyReal with HasPrecision with HasScale {
  def getPrecision: Option[Int]
  def getScale: Option[Int]
}
trait TyByteArray() extends TyNode 
trait TyInet() extends TyNode 
trait TyTuple() extends TyNode with HasValues {
  def getValues: List[TyNode]
}
trait TyStruct() extends TyNode with TyClass with HasName with HasFields with HasDerives with HasAttributes with HasDataframe with HasSchema with HasComment {
  def getName: Option[String]
  def getFields: Option[List[TyField]]
  def getDerives: Option[List[String]]
  def getAttributes: Option[List[String]]
  def getDataframe: Option[Boolean]
  def getSchema: Option[String]
  def getComment: Option[String]
}
trait TyClass() extends TyNode 
trait TyRecord() extends TyNode 
trait TyField() extends TyNode with HasName with HasValue with HasMutability with HasDefaultNone {
  def getName: Option[String]
  def getValue: TyNode
  def getMutability: Option[Boolean]
  def getDefaultNone: Option[Boolean]
}
trait TyMap() extends TyNode with HasKey with HasValue {
  def getKey: TyNode
  def getValue: TyNode
}
trait TyUuid() extends TyNode 
trait TyResult() extends TyNode with HasOk with HasErr {
  def getOk: TyNode
  def getErr: TyNode
}
trait TyChar() extends TyNode 
trait TyNull() extends TyNode 
trait TyNothing() extends TyNode 
trait TyBoolean() extends TyNode 
trait TyNumeric() extends TyNode 
trait TyInteger() extends TyNode with TyNumeric with HasBitSize with HasSized {
  def getBitSize: Option[BitSize]
  def getSized: Option[Boolean]
}
trait TyUnit() extends TyNode 
trait TyList() extends TyNode with HasContent {
  def getContent: TyNode
}
trait TyUndefined() extends TyNode 
class TyAnyBuilder() {
  def build(): TyAnyImpl = {
    TyAnyImpl()
  }
}
class TyRealBuilder() {
  def build(): TyRealImpl = {
    TyRealImpl()
  }
}
class TyFloatBuilder() {
  var bitSize: Option[BitSize] = None
  def bitSize(bitSize: BitSize): TyFloatBuilder = {
    this.bitSize = Some(bitSize)
    this
  }
  def build(): TyFloatImpl = {
    TyFloatImpl(bitSize)
  }
}
class TySetBuilder() {
  var content: Option[TyNode] = None
  def content(content: TyNode): TySetBuilder = {
    this.content = Some(content)
    this
  }
  def build(): TySetImpl = {
    TySetImpl(content.get)
  }
}
class TyOptionalBuilder() {
  var content: Option[TyNode] = None
  def content(content: TyNode): TyOptionalBuilder = {
    this.content = Some(content)
    this
  }
  def build(): TyOptionalImpl = {
    TyOptionalImpl(content.get)
  }
}
class TyStringBuilder() {
  def build(): TyStringImpl = {
    TyStringImpl()
  }
}
class TyObjectBuilder() {
  def build(): TyObjectImpl = {
    TyObjectImpl()
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
  def build(): TyDecimalImpl = {
    TyDecimalImpl(precision, scale)
  }
}
class TyByteArrayBuilder() {
  def build(): TyByteArrayImpl = {
    TyByteArrayImpl()
  }
}
class TyInetBuilder() {
  def build(): TyInetImpl = {
    TyInetImpl()
  }
}
class TyTupleBuilder() {
  var values: Option[List[TyNode]] = None
  def values(values: List[TyNode]): TyTupleBuilder = {
    this.values = Some(values)
    this
  }
  def build(): TyTupleImpl = {
    TyTupleImpl(values.get)
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
  def build(): TyStructImpl = {
    TyStructImpl(name, fields, derives, attributes, dataframe, schema, comment)
  }
}
class TyClassBuilder() {
  def build(): TyClassImpl = {
    TyClassImpl()
  }
}
class TyRecordBuilder() {
  def build(): TyRecordImpl = {
    TyRecordImpl()
  }
}
class TyFieldBuilder() {
  var name: Option[String] = None
  var value: Option[TyNode] = None
  var mutability: Option[Boolean] = None
  var defaultNone: Option[Boolean] = None
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
  def defaultNone(defaultNone: Boolean): TyFieldBuilder = {
    this.defaultNone = Some(defaultNone)
    this
  }
  def build(): TyFieldImpl = {
    TyFieldImpl(name, value.get, mutability, defaultNone)
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
class TyNumericBuilder() {
  def build(): TyNumericImpl = {
    TyNumericImpl()
  }
}
class TyIntegerBuilder() {
  var bitSize: Option[BitSize] = None
  var sized: Option[Boolean] = None
  def bitSize(bitSize: BitSize): TyIntegerBuilder = {
    this.bitSize = Some(bitSize)
    this
  }
  def sized(sized: Boolean): TyIntegerBuilder = {
    this.sized = Some(sized)
    this
  }
  def build(): TyIntegerImpl = {
    TyIntegerImpl(bitSize, sized)
  }
}
class TyUnitBuilder() {
  def build(): TyUnitImpl = {
    TyUnitImpl()
  }
}
class TyListBuilder() {
  var content: Option[TyNode] = None
  def content(content: TyNode): TyListBuilder = {
    this.content = Some(content)
    this
  }
  def build(): TyListImpl = {
    TyListImpl(content.get)
  }
}
class TyUndefinedBuilder() {
  def build(): TyUndefinedImpl = {
    TyUndefinedImpl()
  }
}
package unidef.common.ty

trait HasValue() extends TyNode {
  def getValue: TyNode
}
trait HasPrecision() extends TyNode {
  def getPrecision: Option[Int]
}
trait HasAttributes() extends TyNode {
  def getAttributes: Option[List[String]]
}
trait HasErr() extends TyNode {
  def getErr: TyNode
}
trait HasDerives() extends TyNode {
  def getDerives: Option[List[String]]
}
trait HasContent() extends TyNode {
  def getContent: TyNode
}
trait HasValues() extends TyNode {
  def getValues: List[TyNode]
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
trait HasFields() extends TyNode {
  def getFields: Option[List[TyField]]
}
trait HasDataframe() extends TyNode {
  def getDataframe: Option[Boolean]
}
trait HasName() extends TyNode {
  def getName: Option[String]
}
trait HasSized() extends TyNode {
  def getSized: Option[Boolean]
}
trait HasSchema() extends TyNode {
  def getSchema: Option[String]
}
trait HasScale() extends TyNode {
  def getScale: Option[Int]
}
class TyAnyImpl() extends TyAny
class TyRealImpl() extends TyReal
class TyFloatImpl(val bit_size: Option[BitSize]) extends TyFloat {
  override def getBitSize: Option[BitSize] = {
    bit_size
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
class TyStructImpl(
    val name: Option[String],
    val fields: Option[List[TyField]],
    val derives: Option[List[String]],
    val attributes: Option[List[String]],
    val dataframe: Option[Boolean],
    val schema: Option[String],
    var comment: String
) extends TyStruct
    with TyCommentable {
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
  override def getComment: String = {
    comment
  }
  override def setComment(comment: String): this.type = {
    this.comment = comment
    this
  }
}
class TyClassImpl() extends TyClass
class TyRecordImpl() extends TyRecord
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
class TyIntegerImpl(val bit_size: Option[BitSize], val sized: Option[Boolean]) extends TyInteger {
  override def getBitSize: Option[BitSize] = {
    bit_size
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
trait TyStruct()
    extends TyNode
    with TyClass
    with HasName
    with HasFields
    with HasDerives
    with HasAttributes
    with HasDataframe
    with HasSchema {
  def getName: Option[String]
  def getFields: Option[List[TyField]]
  def getDerives: Option[List[String]]
  def getAttributes: Option[List[String]]
  def getDataframe: Option[Boolean]
  def getSchema: Option[String]
}
trait TyClass() extends TyNode
trait TyRecord() extends TyNode
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

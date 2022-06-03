package unidef.common.ty

trait HasOk extends TyNode {
  def getOk: Option[TyNode]
}


trait HasFields extends TyNode {
  def getFields: Option[List[TyField]]
}


trait HasKey extends TyNode {
  def getKey: Option[TyNode]
}


trait HasValues extends TyNode {
  def getValues: Option[List[TyNode]]
}


trait HasAttributes extends TyNode {
  def getAttributes: Option[List[String]]
}


trait HasContent extends TyNode {
  def getContent: Option[TyNode]
}


trait HasDataframe extends TyNode {
  def getDataframe: Option[Boolean]
}


trait HasSized extends TyNode {
  def getSized: Option[Boolean]
}


trait HasSchema extends TyNode {
  def getSchema: Option[String]
}


trait HasDerives extends TyNode {
  def getDerives: Option[List[String]]
}


trait HasErr extends TyNode {
  def getErr: Option[TyNode]
}


trait HasPrecision extends TyNode {
  def getPrecision: Option[Int]
}


trait HasName extends TyNode {
  def getName: Option[String]
}


trait HasValue extends TyNode {
  def getValue: Option[TyNode]
}


trait HasScale extends TyNode {
  def getScale: Option[Int]
}


trait HasBitSize extends TyNode {
  def getBitSize: Option[BitSize]
}


class TyAnyImpl() extends TyAny {
}


class TyRealImpl() extends TyReal {
}


class TyFloatImpl(val bit_size: Option[BitSize]) extends TyFloat {

  override def getBitSize: Option[BitSize] = {
    bit_size
  }

}


class TySetImpl(val content: Option[TyNode]) extends TySet {

  override def getContent: Option[TyNode] = {
    content
  }

}


class TyOptionalImpl(val content: Option[TyNode]) extends TyOptional {

  override def getContent: Option[TyNode] = {
    content
  }

}


class TyStringImpl() extends TyString {
}


class TyObjectImpl() extends TyObject {
}


class TyDecimalImpl(val precision: Option[Int], val scale: Option[Int]) extends TyDecimal {

  override def getPrecision: Option[Int] = {
    precision
  }


  override def getScale: Option[Int] = {
    scale
  }

}


class TyByteArrayImpl() extends TyByteArray {
}


class TyInetImpl() extends TyInet {
}


class TyTupleImpl(val values: Option[List[TyNode]]) extends TyTuple {

  override def getValues: Option[List[TyNode]] = {
    values
  }

}


class TyStructImpl(val name: Option[String], val fields: Option[List[TyField]], val derives: Option[List[String]], val attributes: Option[List[String]], val dataframe: Option[Boolean] = None, val schema: Option[String] = None, var comment: Option[String] = None) extends TyStruct with TyCommentable {

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


  override def setComment(comment: String): this.type = {
    this.comment = Some(comment)
    this
  }

}


class TyClassImpl() extends TyClass {
}


class TyRecordImpl() extends TyRecord {
}


class TyMapImpl(val key: Option[TyNode], val value: Option[TyNode]) extends TyMap {

  override def getKey: Option[TyNode] = {
    key
  }


  override def getValue: Option[TyNode] = {
    value
  }

}


class TyUuidImpl() extends TyUuid {
}


class TyResultImpl(val ok: Option[TyNode], val err: Option[TyNode]) extends TyResult {

  override def getOk: Option[TyNode] = {
    ok
  }


  override def getErr: Option[TyNode] = {
    err
  }

}


class TyCharImpl() extends TyChar {
}


class TyNullImpl() extends TyNull {
}


class TyNothingImpl() extends TyNothing {
}


class TyBooleanImpl() extends TyBoolean {
}


class TyNumericImpl() extends TyNumeric {
}


class TyIntegerImpl(val bit_size: Option[BitSize], val sized: Option[Boolean]) extends TyInteger {

  override def getBitSize: Option[BitSize] = {
    bit_size
  }


  override def getSized: Option[Boolean] = {
    sized
  }

}


class TyUnitImpl() extends TyUnit {
}


class TyListImpl(val content: Option[TyNode]) extends TyList {

  override def getContent: Option[TyNode] = {
    content
  }

}


class TyUndefinedImpl() extends TyUndefined {
}


trait TyAny extends TyNode {
}


trait TyReal extends TyNode with TyNumeric {
}


trait TyFloat extends TyNode with TyReal with HasBitSize {

  def getBitSize: Option[BitSize]

}


trait TySet extends TyNode with HasContent {

  def getContent: Option[TyNode]

}


trait TyOptional extends TyNode with HasContent {

  def getContent: Option[TyNode]

}


trait TyString extends TyNode {
}


trait TyObject extends TyNode {
}


trait TyDecimal extends TyNode with TyReal with HasPrecision with HasScale {

  def getPrecision: Option[Int]


  def getScale: Option[Int]

}


trait TyByteArray extends TyNode {
}


trait TyInet extends TyNode {
}


trait TyTuple extends TyNode with HasValues {

  def getValues: Option[List[TyNode]]

}


trait TyStruct extends TyNode with TyClass with HasName with HasFields with HasDerives with HasAttributes with HasDataframe with HasSchema {

  def getName: Option[String]


  def getFields: Option[List[TyField]]


  def getDerives: Option[List[String]]


  def getAttributes: Option[List[String]]


  def getDataframe: Option[Boolean]


  def getSchema: Option[String]

}


trait TyClass extends TyNode {
}


trait TyRecord extends TyNode {
}



trait TyMap extends TyNode with HasKey with HasValue {

  def getKey: Option[TyNode]


  def getValue: Option[TyNode]

}


trait TyUuid extends TyNode {
}


trait TyResult extends TyNode with HasOk with HasErr {

  def getOk: Option[TyNode]


  def getErr: Option[TyNode]

}


trait TyChar extends TyNode {
}


trait TyNull extends TyNode {
}


trait TyNothing extends TyNode {
}


trait TyBoolean extends TyNode {
}


trait TyNumeric extends TyNode {
}


trait TyInteger extends TyNode with TyNumeric with HasBitSize with HasSized {

  def getBitSize: Option[BitSize]


  def getSized: Option[Boolean]

}


trait TyUnit extends TyNode {
}


trait TyList extends TyNode with HasContent {

  def getContent: Option[TyNode]

}


trait TyUndefined extends TyNode {
}

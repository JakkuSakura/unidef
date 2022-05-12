package unidef.languages.common

trait TyObject extends TyNode {}

trait HasAttributes extends TyNode {
  def getAttributes: Option[List[String]]
}

trait HasSized extends TyNode {
  def getSized: Option[Boolean]
}

case class TyInetImpl() extends TyInet {}

trait TyFloat extends TyNode with TyReal with HasBitSize {

  def getBitSize: Option[BitSize]

}

trait TyChar extends TyNode {}

case class TySetImpl(content: Option[TyNode]) extends TySet {

  override def getContent: Option[TyNode] = {
    content
  }

}

case object KeyName extends KeywordString {}

trait HasErr extends TyNode {
  def getErr: Option[TyNode]
}

trait HasOk extends TyNode {
  def getOk: Option[TyNode]
}

trait TyBoolean extends TyNode {}

trait TyString extends TyNode {}

trait TyUuid extends TyNode {}

trait HasFields extends TyNode {
  def getFields: Option[List[TyField]]
}

trait TyNothing extends TyNode {}

case object KeyDerives extends Keyword {
  override type V = List[String]
}

case class TyTupleImpl(values: Option[List[TyNode]]) extends TyTuple {

  override def getValues: Option[List[TyNode]] = {
    values
  }

}

trait HasScale extends TyNode {
  def getScale: Option[Int]
}

trait HasBitSize extends TyNode {
  def getBitSize: Option[BitSize]
}

case class TyNullImpl() extends TyNull {}

trait TyByteArray extends TyNode {}

case object KeyValue extends Keyword {
  override type V = TyNode
}

case object KeySized extends KeywordBoolean {}

case object KeyAttributes extends Keyword {
  override type V = List[String]
}

trait HasKey extends TyNode {
  def getKey: Option[TyNode]
}

case object KeyOk extends Keyword {
  override type V = TyNode
}

trait HasValues extends TyNode {
  def getValues: Option[List[TyNode]]
}

trait TyUnit extends TyNode {}

trait TyInteger extends TyNode with TyNumeric with HasBitSize with HasSized {

  def getBitSize: Option[BitSize]

  def getSized: Option[Boolean]

}

case class TyNumericImpl() extends TyNumeric {}

case class TyCharImpl() extends TyChar {}

trait TyInet extends TyNode {}

case object KeyBitSize extends Keyword {
  override type V = BitSize
}

trait TyAny extends TyNode {}

case class TyListImpl(content: Option[TyNode]) extends TyList {

  override def getContent: Option[TyNode] = {
    content
  }

}

case object KeyScale extends KeywordInt {}

trait HasContent extends TyNode {
  def getContent: Option[TyNode]
}

case object KeyContent extends Keyword {
  override type V = TyNode
}

trait TySet extends TyNode with HasContent {

  def getContent: Option[TyNode]

}


case class TyUuidImpl() extends TyUuid {}

case class TyObjectImpl() extends TyObject {}

case class TyByteArrayImpl() extends TyByteArray {}

trait TyNumeric extends TyNode {}

trait TyResult extends TyNode with HasOk with HasErr {

  def getOk: Option[TyNode]

  def getErr: Option[TyNode]

}

case class TyRecordImpl() extends TyRecord {}

case class TyRealImpl() extends TyReal {}

case class TyResultImpl(ok: Option[TyNode], err: Option[TyNode]) extends TyResult {

  override def getOk: Option[TyNode] = {
    ok
  }

  override def getErr: Option[TyNode] = {
    err
  }

}

trait TyMap extends TyNode with HasKey with HasValue {

  def getKey: Option[TyNode]

  def getValue: Option[TyNode]

}

trait TyDecimal extends TyNode with TyReal with HasPrecision with HasScale {

  def getPrecision: Option[Int]

  def getScale: Option[Int]

}

trait TyUndefined extends TyNode {}

case class TyDecimalImpl(precision: Option[Int], scale: Option[Int]) extends TyDecimal {

  override def getPrecision: Option[Int] = {
    precision
  }

  override def getScale: Option[Int] = {
    scale
  }

}

case class TyIntegerImpl(bit_size: Option[BitSize], sized: Option[Boolean]) extends TyInteger {

  override def getBitSize: Option[BitSize] = {
    bit_size
  }

  override def getSized: Option[Boolean] = {
    sized
  }

}

trait TyNull extends TyNode {}

case object KeyErr extends Keyword {
  override type V = TyNode
}

case class TyFloatImpl(bit_size: Option[BitSize]) extends TyFloat {

  override def getBitSize: Option[BitSize] = {
    bit_size
  }

}

trait TyClass extends TyNode {}

case object KeyValues extends Keyword {
  override type V = List[TyNode]
}

trait TyRecord extends TyNode {}


trait HasDerives extends TyNode {
  def getDerives: Option[List[String]]
}

case class TyStructImpl(
    name: Option[String],
    fields: Option[List[TyField]],
    derives: Option[List[String]],
    attributes: Option[List[String]],
    dataframe: Option[Boolean] = None,
    schema: Option[String] = None
) extends TyStruct {

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

  override def getSchema: Option[String] = {
    schema
  }

}

case class TyNothingImpl() extends TyNothing {}

trait TyTuple extends TyNode with HasValues {

  def getValues: Option[List[TyNode]]

}

case object KeyFields extends Keyword {
  override type V = List[TyField]
}

trait TyOptional extends TyNode with HasContent {

  def getContent: Option[TyNode]

}

case class TyMapImpl(key: Option[TyNode], value: Option[TyNode]) extends TyMap {

  override def getKey: Option[TyNode] = {
    key
  }

  override def getValue: Option[TyNode] = {
    value
  }

}

case class TyAnyImpl() extends TyAny {}

case class TyUnitImpl() extends TyUnit {}

trait TyList extends TyNode with HasContent {

  def getContent: Option[TyNode]

}

case object KeyPrecision extends KeywordInt {}

trait HasPrecision extends TyNode {
  def getPrecision: Option[Int]
}

case class TyClassImpl() extends TyClass {}

case class TyOptionalImpl(content: Option[TyNode]) extends TyOptional {

  override def getContent: Option[TyNode] = {
    content
  }

}

trait TyReal extends TyNode with TyNumeric {}

trait HasName extends TyNode {
  def getName: Option[String]
}

case class TyBooleanImpl() extends TyBoolean {}

case class TyUndefinedImpl() extends TyUndefined {}

trait TyStruct
    extends TyNode
    with TyClass
    with HasName
    with HasFields
    with HasDerives
    with HasAttributes {

  def getName: Option[String]

  def getFields: Option[List[TyField]]

  def getDerives: Option[List[String]]

  def getAttributes: Option[List[String]]
  
  def getSchema: Option[String]

}

case class TyStringImpl() extends TyString {}

trait HasValue extends TyNode {
  def getValue: Option[TyNode]
}

case object KeyKey extends Keyword {
  override type V = TyNode
}

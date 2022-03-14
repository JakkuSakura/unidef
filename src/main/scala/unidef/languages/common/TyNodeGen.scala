package unidef.languages.common

trait HasFields extends TyNode {
  def getFields: Option[List[TyField]]
}

trait TyObject extends TyNode {}

trait HasSized extends TyNode {
  def getSized: Option[Boolean]
}

case class TyListImpl(content: Option[TyNode]) extends Extendable with TyList {

  override def getContent: Option[TyNode] = {
    content
  }

}

trait TyList extends TyNode with HasContent {

  def getContent: Option[TyNode]

}

trait HasName extends TyNode {
  def getName: Option[String]
}

trait HasOk extends TyNode {
  def getOk: Option[TyNode]
}

trait TyBoolean extends TyNode {}

trait TyString extends TyNode {}

case object KeyDerives extends Keyword {
  override type V = List[String]
}

trait HasScale extends TyNode {
  def getScale: Option[Int]
}

trait HasBitSize extends TyNode {
  def getBitSize: Option[BitSize]
}

trait TyByteArray extends TyNode {}

case object KeyValue extends Keyword {
  override type V = TyNode
}

trait TyStruct extends TyNode with HasName with HasFields with HasDerives with HasAttributes {

  def getName: Option[String]

  def getFields: Option[List[TyField]]

  def getDerives: Option[List[String]]

  def getAttributes: Option[List[String]]

}

case class TyObjectImpl() extends Extendable with TyObject {}

case object KeySized extends KeywordBoolean {}

trait TyDecimal extends TyNode with HasPrecision with HasScale {

  def getPrecision: Option[Int]

  def getScale: Option[Int]

}

trait HasKey extends TyNode {
  def getKey: Option[TyNode]
}

trait TyInteger extends TyNode with HasBitSize with HasSized {

  def getBitSize: Option[BitSize]

  def getSized: Option[Boolean]

}

case object KeyAttributes extends Keyword {
  override type V = List[String]
}

case object KeyOk extends Keyword {
  override type V = TyNode
}

trait HasValues extends TyNode {
  def getValues: Option[List[TyNode]]
}

case object KeyBitSize extends Keyword {
  override type V = BitSize
}

trait HasAttributes extends TyNode {
  def getAttributes: Option[List[String]]
}

trait TyReal extends TyNode {}

case class TyFloatImpl(bit_size: Option[BitSize]) extends Extendable with TyFloat {

  override def getBitSize: Option[BitSize] = {
    bit_size
  }

}

//case class TyEnumImpl(variants: Option[List[String]]) extends Extendable with TyEnum {
//
//  override def getVariants: Option[List[String]] = {
//    variants
//  }
//
//}

case object KeyScale extends KeywordInt {}

case object KeyVariants extends Keyword {
  override type V = List[String]
}

trait HasContent extends TyNode {
  def getContent: Option[TyNode]
}

case object KeyContent extends Keyword {
  override type V = TyNode
}

trait TySet extends TyNode with HasContent {

  def getContent: Option[TyNode]

}

case class TyDecimalImpl(precision: Option[Int], scale: Option[Int])
    extends Extendable
    with TyDecimal {

  override def getPrecision: Option[Int] = {
    precision
  }

  override def getScale: Option[Int] = {
    scale
  }

}

case class TyTupleImpl(values: Option[List[TyNode]]) extends Extendable with TyTuple {

  override def getValues: Option[List[TyNode]] = {
    values
  }

}

trait TyNumeric extends TyNode {}

case class TyStructImpl(
    name: Option[String],
    fields: Option[List[TyField]],
    derives: Option[List[String]],
    attributes: Option[List[String]]
) extends Extendable
    with TyStruct {

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

}

trait TyResult extends TyNode with HasOk with HasErr {

  def getOk: Option[TyNode]

  def getErr: Option[TyNode]

}

case class TyResultImpl(ok: Option[TyNode], err: Option[TyNode]) extends Extendable with TyResult {

  override def getOk: Option[TyNode] = {
    ok
  }

  override def getErr: Option[TyNode] = {
    err
  }

}

case class TyOptionalImpl(content: Option[TyNode]) extends Extendable with TyOptional {

  override def getContent: Option[TyNode] = {
    content
  }

}

trait TyMap extends TyNode with HasKey with HasValue {

  def getKey: Option[TyNode]

  def getValue: Option[TyNode]

}

case class TyStringImpl() extends Extendable with TyString {}

//trait TyEnum extends TyNode with HasVariants {
//
//  def getVariants: Option[List[String]]
//
//}

case object KeyErr extends Keyword {
  override type V = TyNode
}

trait TyClass extends TyNode {}

case object KeyValues extends Keyword {
  override type V = List[TyNode]
}

case class TySetImpl(content: Option[TyNode]) extends Extendable with TySet {

  override def getContent: Option[TyNode] = {
    content
  }

}

case object KeyName extends KeywordString {}

case class TyClassImpl() extends Extendable with TyClass {}

trait HasDerives extends TyNode {
  def getDerives: Option[List[String]]
}

trait HasErr extends TyNode {
  def getErr: Option[TyNode]
}

trait TyFloat extends TyNode with HasBitSize {

  def getBitSize: Option[BitSize]

}

case class TyRealImpl() extends Extendable with TyReal {}

trait TyTuple extends TyNode with HasValues {

  def getValues: Option[List[TyNode]]

}

case object KeyFields extends Keyword {
  override type V = List[TyField]
}

trait TyOptional extends TyNode with HasContent {

  def getContent: Option[TyNode]

}

case class TyByteArrayImpl() extends Extendable with TyByteArray {}

case class TyIntegerImpl(bit_size: Option[BitSize], sized: Option[Boolean])
    extends Extendable
    with TyInteger {

  override def getBitSize: Option[BitSize] = {
    bit_size
  }

  override def getSized: Option[Boolean] = {
    sized
  }

}

case object KeyPrecision extends KeywordInt {}

trait HasPrecision extends TyNode {
  def getPrecision: Option[Int]
}

trait HasVariants extends TyNode {
  def getVariants: Option[List[String]]
}

case class TyNumericImpl() extends Extendable with TyNumeric {}

trait HasValue extends TyNode {
  def getValue: Option[TyNode]
}

case class TyBooleanImpl() extends Extendable with TyBoolean {}

case object KeyKey extends Keyword {
  override type V = TyNode
}

case class TyMapImpl(key: Option[TyNode], value: Option[TyNode]) extends Extendable with TyMap {

  override def getKey: Option[TyNode] = {
    key
  }

  override def getValue: Option[TyNode] = {
    value
  }

}

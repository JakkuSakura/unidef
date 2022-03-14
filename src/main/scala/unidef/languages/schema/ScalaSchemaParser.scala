package unidef.languages.schema

import com.typesafe.scalalogging.Logger
import io.circe.yaml.parser
import io.circe.{Json, JsonNumber, JsonObject}
import unidef.languages.common.*
import unidef.languages.javascript.{JsonSchemaCommon, JsonSchemaParser}
import unidef.languages.scala.{ScalaCodeGen, ScalaCommon}
import unidef.utils.FileUtils.readFile
import unidef.utils.{ParseCodeException, TextTool, TypeDecodeException, TypeEncodeException}

import java.io.PrintWriter
import scala.collection.mutable

case class ScalaSchemaParser() {
  val logger: Logger = Logger[this.type]
  val common = JsonSchemaCommon(true)

}
object ScalaSchemaParser {
  def main(args: Array[String]): Unit = {
    val types = mutable.Map[String, TypeBuilder]()
    types += "list" -> TypeBuilder("list")
      .field("content", Type)
    types += "string" -> TypeBuilder("string")
    types += "enum" -> TypeBuilder("enum")
      .field("values", TyList(TyString))
    types += "tuple" -> TypeBuilder("tuple")
      .field("values", TyList(Type))
    types += "option" -> TypeBuilder("optional")
      .field("content", Type)
    types += "result" -> TypeBuilder("result")
      .field("ok", Type)
      .field("err", Type)
    types += "numeric" -> TypeBuilder("numeric")
    types += "integer" -> TypeBuilder("integer")
      .field("bit_size", TyNamed("bit_size"))
      .field("sized", TyBoolean)
      .is(TyNamed("numeric"))

    types += "real" -> TypeBuilder("real")
      .is(TyNamed("numeric"))

    types += "decimal" -> TypeBuilder("decimal")
      .field("precision", TyInteger())
      .field("scale", TyInteger())
      .is(TyNamed("real"))

    types += "float" -> TypeBuilder("float")
      .field("bis_size", TyNamed("bit_size"))
      .is(TyNamed("real"))
    types += "class" -> TypeBuilder("class")

    types += "struct" -> TypeBuilder("struct")
      .field("fields", TyList(Type))
      .field("derives", TyList(TyString))
      .field("attributes", TyList(TyString))
      .is(TyNamed("class"))

    types += "object" -> TypeBuilder("object")

    types += "dict" -> TypeBuilder("map")
      .field("key", Type)
      .field("value", Type)

    types += "set" -> TypeBuilder("set")
      .field("content", Type)

    types += "byte" -> TypeBuilder("set")
      .field("content", Type)
      .is(TyInteger(BitSize.B8, false))
    types += "byte_array" -> TypeBuilder("byte_array")
      .is(TyList(TyInteger(BitSize.B8, false)))

  }
}

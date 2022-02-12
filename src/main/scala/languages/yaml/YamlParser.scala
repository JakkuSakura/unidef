package com.jeekrs.unidef
package languages.yaml

import languages.common.FieldType.{AutoIncr, PrimaryKey}
import languages.common.{FieldType, IrNode, StructType, TyNode, TypeParser}
import languages.common.JsonUtils.{getBool, getJson, getObject, getString}

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.IntType
import io.circe.parser.decode
import io.circe.syntax.*
import io.circe.yaml.parser
import io.circe.{Json, JsonObject, ParsingFailure}
import jdk.internal.org.objectweb.asm.tree.FieldNode

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

enum YamlType(ident: String):
    case Model extends YamlType("model")
    case Enum extends YamlType("enum")

object YamlType:
    def parse(name: String): Option[YamlType] = name match
        case "model" => Some(YamlType.Model)
        case "enum" => Some(YamlType.Enum)
        case _ => None

class YamlParser:
    @throws[ParsingFailure]
    def parseFile(content: String): List[IrNode] =
        val agg = mutable.ArrayBuffer[IrNode]()
        for (v <- parser.parseDocuments(content))
            agg += parseIrNode(v.toTry.get.asObject.toRight(ParsingFailure("is not object", null)).toTry.get)

        agg.toList

    @throws[ParsingFailure]
    def parseIrNode(content: JsonObject): IrNode =
        val ty1 = getString(content, "type")
        val ty = YamlType.parse(ty1).toRight(ParsingFailure("`type` is not one of YamlType", null)).toTry.get
        ty match
            case YamlType.Model => parseIrNodeModel(content)
            case YamlType.Enum => ???

    @throws[ParsingFailure]
    def parseIrNodeModel(content: JsonObject): StructType =
        val fields = content("fields").toRight(ParsingFailure("Missing `fields`", null)).toTry.get
        val fields_arr = fields.asArray.toRight(ParsingFailure("`fields` is not array", null)).toTry.get
        val name = getString(content, "name")

        StructType(name,
                   fields_arr
                     .map(_.asObject.toRight(ParsingFailure("is not Object", null)).toTry.get)
                     .map(parseFieldType).toList
                   )

    @throws[ParsingFailure]
    def parseFieldType(content: JsonObject): FieldType =
        val name = getString(content, "name")
        val ty = TypeParser.parse(getString(content, "type")).toTry.get
        val field = FieldType(name, ty)

        for (key <- content.keys) do
            key match
                case "name" =>
                case "type" =>
                case "primary" =>
                    field.setValue(PrimaryKey(getBool(content, "primary")))
                case "auto_incr" =>
                    field.setValue(AutoIncr(getBool(content, "auto_incr")))
                case _ =>


        field


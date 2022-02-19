package unidef.languages.sql

import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.statement.create.function.CreateFunction
import net.sf.jsqlparser.statement.create.table.{
  ColDataType,
  ColumnDefinition,
  CreateTable
}
import org.apache.commons.lang3.StringUtils
import unidef.languages.common
import unidef.languages.common._
import unidef.languages.sql.SqlCommon.{Records, Schema, convertTypeFromSql}
import unidef.utils.TextTool.finds

import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters._
import scala.util.matching.Regex

case class SqlParser() {
  // does not support default value yet

  def parse(sql: String): Seq[AstNode] = {
    val collected = ArrayBuffer[AstNode]()
    collected ++= extractEnums(sql).map(AstTyped)
    val cleaned = stripUnsurpported(sql)
    val stmts = CCJSqlParserUtil.parseStatements(cleaned)
    stmts.getStatements.asScala.foreach {
      case table: CreateTable => collected += parseCreateTable(table)
      case func: CreateFunction =>
        val declarations = List("CREATE", "FUNCTION") ++ func.getFunctionDeclarationParts.asScala
        var pos = 0
        while (pos < declarations.length) {
          parseCreateFunction(declarations.slice(pos, declarations.length)) match {
            case Some((func, p)) =>
              collected += func
              pos += p + 1
            case None => pos = declarations.length
          }
        }
      case _ =>
    }
    collected.toSeq
  }
  private def extractEnums(sql: String): Seq[TyEnum] = {
    new Regex("CREATE\\s+TYPE\\s+(.+)\\s+AS\\s+enum\\s+\\((.+?)\\);")
      .findAllMatchIn(sql)
      .map(m => m.group(1) -> m.group(2))
      .map {
        case (k, v) =>
          k -> v
            .split(",")
            .map(StringUtils.strip(_, "'"))
            .map(x => TyVariant(Seq(x)))
      }
      .map {
        case (k, v) => TyEnum(k, v)
      }
      .toSeq
  }
  private def stripUnsurpported(sql: String): String = {
    sql
      .replaceAll("(DEFAULT|default).+?(?=,|\\n|not|NOT)", "")
      .replaceAll("CREATE SCHEMA.+?;", "")
      .replaceAll("CREATE SEQUENCE(.|\\n)+?;", "")
      .replaceAll("CREATE TYPE(.|\\n)+?;", "")
      .replaceAll("NOT DEFERRABLE", "")
      .replaceAll("INITIALLY IMMEDIATE", "")
      .replaceAll("CREATE (UNIQUE)? INDEX(.|\\n)+?;", "")
  }

  private def parseParam(args: Seq[String]): (Boolean, TyField) = {
    var cursor = 0
    val input = args.head.toUpperCase match {
      case "OUT" =>
        cursor += 1
        false
      case "IN" =>
        cursor += 1
        true
      case _ => true
    }
    val name = args(cursor)
    val ty = convertTypeFromSql(
      args.slice(cursor + 1, args.length).mkString(" ")
    )
    (input, TyField(name, ty))
  }

  private def parseParams(args: Seq[String]): Seq[TyField] = {
    var cursor = 0
    val params = ArrayBuffer[TyField]()
    while (cursor < args.length) {
      val (input, param) = parseParam(args.slice(cursor, args.length))
      params += param
      cursor += 1 + param.name.length
    }
    params.toSeq
  }
  def parseCreateFunction(
    parts: Seq[String]
  ): Option[(AstFunctionDecl, Int)] = {
    val slice = parts.slice(0, 0 + 5)
    val name = slice match {
      case "CREATE" +: "OR" +: "REPLACE" +: "FUNCTION" +: name +: Nil => name
      case "CREATE" +: "FUNCTION" +: name +: _                        => name
      case _                                                          => return None
    }
    val first_left_paren = finds(parts, "(").get
    val first_right_paren = finds(parts, ")").get
    val params = parts.slice(first_left_paren + 1, first_right_paren)

    var cursor = 0
    val inputs = ArrayBuffer[TyField]()
    val outputs = ArrayBuffer[TyField]()
    var outputOnly: Option[TyNode] = None
    for (i <- 0 to params.length) {
      if (i == params.length || params(i) == ",") {
        if (cursor < i) {
          parseParam(params.slice(cursor, i)) match {
            case (true, ty)  => inputs += ty
            case (false, ty) => outputs += ty
          }
        }
        cursor = i + 1
      }
    }
    val as = finds(parts, "AS").get

    val lq =
      finds(parts, "$$").orElse(finds(parts, "$func$")).get
    val rq =
      finds(parts, "$$", lq + 1).orElse(finds(parts, "$func$", lq + 1)).get

    val body = parts.slice(lq + 1, rq).mkString(" ")
    val languagePos = finds(parts, "LANGUAGE").get
    val language = parts(languagePos + 1)
    val end = finds(parts, ";", rq + 1).get
    finds(parts, "RETURNS").filter(_ < end).foreach { i =>
      parts.slice(i + 1, as.min(languagePos)) match {
        case "TABLE" +: "(" +: fields =>
          cursor = 0
          for (i <- 0 to fields.length) {
            if (i < fields.length && (fields(i) == ")" || fields(i) == ",")) {
              if (cursor < i) {
                parseParam(fields.slice(cursor, i)) match {
                  case (_, ty) => outputs += ty
                }
              }
              cursor = i + 1
            }
          }
        case ty =>
          val ret = ty.mkString(" ")
          outputOnly = Some(convertTypeFromSql(ret))
      }

    }

    val func = AstFunctionDecl(
      AstLiteralString(name),
      inputs.toSeq,
      if (outputOnly.isDefined) AstTyped(outputOnly.get)
      else {
        AstClassDecl(AstLiteralString("unnamed"), outputs.toSeq)
      },
      AstRawCode(body).setValue(Language, language),
    )
    if (outputOnly.isEmpty)
      func.setValue(Records, true)
    Some((func, end))
  }
  def parseCreateTable(tbl: CreateTable): AstClassDecl = {
    common
      .AstClassDecl(
        AstLiteralString(tbl.getTable.getName),
        tbl.getColumnDefinitions.asScala.map(parseParseColumn).toSeq
      )
      .setValue(Schema, tbl.getTable.getSchemaName)
  }
  def parseParseColumn(column: ColumnDefinition): TyField = {
    val ty = column.getColDataType
    val name = column.getColumnName
    TyField(name, parseColDataType(ty))
  }

  def parseColDataType(ty: ColDataType): TyNode =
    convertTypeFromSql(ty.getDataType)

  def parseFunc(sql: String): Seq[AstNode] = {
    ???
  }

  def parseTable(sql: String): Seq[AstNode] = {
    ???
  }

}

package unidef.languages.sql

import com.typesafe.scalalogging.Logger
import net.sf.jsqlparser.parser.{CCJSqlParserUtil, ParseException}
import net.sf.jsqlparser.statement.create.function.CreateFunction
import net.sf.jsqlparser.statement.create.table.{
  ColDataType,
  ColumnDefinition,
  CreateTable
}
import org.apache.commons.lang3.StringUtils
import unidef.languages.common
import unidef.languages.common._
import unidef.languages.sql.SqlCommon.{
  KeyRecords,
  KeySchema,
  convertTypeFromSql
}
import unidef.utils.TextTool.{finds, findss}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters._
import scala.util.matching.Regex

object SqlParser {
  val logger: Logger = Logger[this.type]

  // does not support default value yet

  def parse(sql: String)(implicit resolver: TypeRegistry): Seq[AstNode] = {
    val collected = ArrayBuffer[AstNode]()
    extractEnums(sql).foreach { x =>
      x.getValue(KeyName).foreach { nm =>
        resolver.add(nm, x, "sql")
        collected += AstTyped(x)
      }

    }

    val cleaned = stripUnsurpported(sql)
    if (isEmpty(cleaned)) return {
      logger.debug(s"No table or function declaration: ${sql.slice(0, 100)}")
      collected.toSeq
    }
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
            .map(StringUtils.strip(_, " '"))
            .map(x => TyVariant(Seq(x)))
      }
      .map {
        case (s"$schema.$name", v) =>
          TyEnum(v.toSeq).setValue(KeySchema, schema).setValue(KeyName, name)
        case (k, v) => TyEnum(v.toSeq).setValue(KeyName, k)
      }
      .toSeq
  }
  private def stripUnsurpported(sql: String): String =
    sql
      .replaceAll("(DEFAULT|default).+?(?=,|\\n|not|NOT)", "")
      .replaceAll("CREATE SCHEMA.+?;", "")
      .replaceAll("CREATE SEQUENCE(.|\\n)+?;", "")
      .replaceAll("CREATE TYPE(.|\\n)+?;", "")
      .replaceAll("NOT DEFERRABLE", "")
      .replaceAll("INITIALLY IMMEDIATE", "")
      .replaceAll("CREATE (UNIQUE)? INDEX(.|\\n)+?;", "")
  private def isEmpty(s: String): Boolean =
    s.replaceAll("--.+", "").replaceAll("\\s+", "").isEmpty
  private def compactDot(args: Seq[String]): String = {
    val sb = new mutable.StringBuilder
    for (i <- args.indices) {
      if (i > 0)
        if (args(i) != "." && args(i - 1) != "." && args(i) != "[" && args(i) != "]")
          sb ++= " "
      sb ++= args(i)
    }
    sb.toString()
  }

  private def parseParam(
    args: Seq[String]
  )(implicit resolver: TypeResolver): (Boolean, TyField) = {
    logger.info("parseParam: " + args.mkString(", "))
    var nameCursor = 0
    var typeCursor = 1
    val input = args.slice(0, 2).map(_.toUpperCase) match {
      case "OUT" +: _ =>
        nameCursor = 1
        typeCursor = 2
        false
      case _ +: "OUT" +: Nil =>
        nameCursor = 0
        typeCursor = 2
        false
      case "IN" +: _ =>
        nameCursor = 1
        typeCursor = 2
        true
      case _ +: "IN" +: Nil =>
        nameCursor = 0
        typeCursor = 2
        true
      case _ => true
    }
    val name = args(nameCursor).replaceAll("\"", "")

    val ty = convertTypeFromSql(compactDot(args.slice(typeCursor, args.length)))
    (input, TyField(name, ty))
  }

  def parseCreateFunction(
    parts: Seq[String]
  )(implicit resolver: TypeResolver): Option[(AstFunctionDecl, Int)] = {
    val (schema, name) = parts match {
      case "CREATE" +: "OR" +: "REPLACE" +: "FUNCTION" +: schema +: "." +: name +: _ =>
        (schema, name)
      case "CREATE" +: "FUNCTION" +: schema +: "." +: name +: _ =>
        (schema, name)

      case "CREATE" +: "OR" +: "REPLACE" +: "FUNCTION" +: name +: _ =>
        ("", name)
      case "CREATE" +: "FUNCTION" +: name +: _ => ("", name)
      case _                                   => return None
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
    val delimiters = Seq("$$", "$func$", "$fun$", "$EOF$")
    val lq =
      findss(parts, delimiters)
        .toRight(new ParseException("No delimiter found"))
        .toTry
        .get
    val rq =
      findss(parts, delimiters, lq + 1)
        .toRight(new ParseException("No delimiter found"))
        .toTry
        .get

    val body = parts.slice(lq + 1, rq).mkString(" ")
    val languagePos = finds(parts, "LANGUAGE").get
    val language = parts(languagePos + 1)
    val end = finds(parts, ";", rq + 1).get
    finds(parts, "RETURNS").filter(_ < end).foreach { i =>
      parts.slice(i + 1, as.min(languagePos)) match {
        case ("TABLE" | "table") +: "(" +: fields =>
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
      if (outputs.nonEmpty)
        TyStruct().setValue(KeyFields, outputs.toSeq)
      else if (outputOnly.isDefined)
        outputOnly.get
      else
        TyStruct().setValue(KeyFields, Nil)
    ).setValue(KeySchema, schema)
    func.setValue(KeyBody, AstRawCode(body).setValue(KeyLanguage, language))
    if (outputOnly.isEmpty)
      func.setValue(KeyRecords, true)
    logger.debug(s"Parsed function: ${func.literalName.get}")
    Some((func, end))
  }
  def parseCreateTable(
    tbl: CreateTable
  )(implicit resolver: TypeResolver): AstClassDecl = {
    common
      .AstClassDecl(
        AstLiteralString(tbl.getTable.getName),
        tbl.getColumnDefinitions.asScala.map(parseParseColumn).toSeq
      )
      .trySetValue(KeySchema, Option(tbl.getTable.getSchemaName))
  }
  def parseParseColumn(
    column: ColumnDefinition
  )(implicit resolver: TypeResolver): TyField = {
    val ty = column.getColDataType
    val name = column.getColumnName
    TyField(name, parseColDataType(ty))
  }

  def parseColDataType(
    ty: ColDataType
  )(implicit resolver: TypeResolver): TyNode =
    convertTypeFromSql(ty.getDataType)

}

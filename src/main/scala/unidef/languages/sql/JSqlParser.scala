package unidef.languages.sql

import com.typesafe.scalalogging.Logger
import net.sf.jsqlparser.JSQLParserException
import net.sf.jsqlparser.parser.{CCJSqlParserUtil, ParseException}
import net.sf.jsqlparser.statement.create.function.CreateFunction
import net.sf.jsqlparser.statement.create.table.{ColDataType, ColumnDefinition, CreateTable}
import org.apache.commons.lang3.StringUtils
import unidef.common
import unidef.common.ty.*
import unidef.common.ast.*
import unidef.languages.sql.SqlCommon.{KeyRecords, KeySchema}
import unidef.utils.TextTool.{finds, findss}
import unidef.utils.{ParseCodeException, TypeDecodeException}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters.*
import scala.util.matching.Regex

class JSqlParser() {
  val logger: Logger = Logger[this.type]
  val sqlCommon: SqlCommon = SqlCommon()
  // does not support default value yet

  def parse(sql: String)(implicit resolver: TypeRegistry): List[AstNode] = {
    val collected = ArrayBuffer[AstNode]()
    val enums = extractEnums(sql)
    enums.foreach { x =>
      x.name.foreach { nm =>
        resolver.add(nm, x, "sql")
        collected += common.ast.AstTypeImpl(x)
      }

    }

    val cleaned = stripUnsurpported(sql)
    if (isEmpty(cleaned)) return {
      logger.debug(s"No table or function declaration: ${sql.slice(0, 100)}")
      collected.toList
    }
    val stmts =
      try {
        CCJSqlParserUtil.parseStatements(cleaned)
      } catch {
        case t: JSQLParserException =>
          logger.info("Errr while parsing sql: " + cleaned)
          throw t
        case t =>
          throw t
      }
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
    collected.toList
  }
  private def extractEnums(sql: String): Seq[TyEnum] = {
    new Regex("(CREATE|create)\\s+(TYPE|type)\\s+(.+)\\s+(AS|as)\\s+(ENUM|enum)\\s*\\((.+?)\\);")
      .findAllMatchIn(sql)
      .map(m => m.group(3) -> m.group(6))
      .map { (k, v) =>
        k -> v
          .split(",")
          .map(StringUtils.strip(_, " '"))
          .map(variantName => TyVariantBuilder().names(List(variantName)).build())
      }
      .map {
        case (s"$schema.$name", v) =>
          TyEnumBuilder()
            .variants(v.toList)
            .name(name)
            .value(TyStringImpl())
            .schema(schema)
            .build()
        case (enumName, v) =>
          TyEnumBuilder()
            .variants(v.toList)
            .name(enumName)
            .value(TyStringImpl())
            .build()
      }
      .toSeq
  }
  private def stripUnsurpported(sql: String): String =
    sql
      .replaceAll("\\b(DEFAULT|default)\\b.+?(?=,|\\n|not|NOT)", "")
      .replaceAll("CREATE SCHEMA.+?;", "")
      .replaceAll("CREATE SEQUENCE(.|\\n)+?;", "")
      .replaceAll("CREATE TYPE(.|\\n)+?;", "")
      .replaceAll("NOT DEFERRABLE", "")
      .replaceAll("INITIALLY IMMEDIATE", "")
      .replaceAll("CREATE (UNIQUE)? INDEX(.|\\n)+?;", "")
  private def isEmpty(s: String): Boolean =
    s.replaceAll("--.+", "").replaceAll("\\s+", "").isEmpty
  private def compactDot(args: Seq[String]): (String, String) = {
    val sb = new mutable.StringBuilder
    for (i <- args.indices) {
      if (i > 0)
        if (args(i) != "." && args(i - 1) != "." && args(i) != "[" && args(i) != "]")
          sb ++= " "
      sb ++= args(i)
    }
    val result = sb.toString().trim
    if (result.contains("=")) {
      val parts = result.split("=")
      (parts(0).trim, parts(1).trim)
    } else {
      (result, "")
    }
  }

  private def parseParam(
      args: Seq[String]
  )(implicit resolver: TypeDecoder[String]): (Boolean, TyField) = {
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

    val (tyName, default) = compactDot(args.slice(typeCursor, args.length))
    val ty = lookUpOrParseType(tyName)
      .getOrElse(throw TypeDecodeException(s"Failed to parse type", tyName))
    if (default == "NULL")
      (input, TyFieldBuilder().name(name).value(TyOptionalImpl(ty)).build())
    else
      (input, TyFieldBuilder().name(name).value(ty).build())

  }

  def parseCreateFunction(
      parts: Seq[String]
  )(implicit resolver: TypeDecoder[String]): Option[(AstFunctionDecl, Int)] = {
    val (schema, name) = parts match {
      case "CREATE" +: "OR" +: "REPLACE" +: "FUNCTION" +: schema +: "." +: name +: _ =>
        (schema, name)
      case "CREATE" +: "FUNCTION" +: schema +: "." +: name +: _ =>
        (schema, name)

      case "CREATE" +: "OR" +: "REPLACE" +: "FUNCTION" +: name +: _ =>
        ("", name)
      case "CREATE" +: "FUNCTION" +: name +: _ => ("", name)
      case _ if parts.length < 10 => return None
      case _ =>
        throw ParseCodeException(s"Failed to parse remaining function: " + parts.mkString(" "))
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
            case (true, ty) => inputs += ty
            case (false, ty) => outputs += ty
          }
        }
        cursor = i + 1
      }
    }
    val as = finds(parts, "AS").get

    val delims = Seq("$$", "$func$", "$fun$", "$EOF$")
    val lq = delims
      .flatMap(delim => finds(parts, delim))
      .minOption
      .getOrElse(throw ParseCodeException("lq not found"))
    val rq = delims
      .flatMap(delim => finds(parts, delim, lq + 1))
      .minOption
      .getOrElse(throw ParseCodeException("rq not found"))

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
          val (ret, _) = compactDot(ty)
          outputOnly = Some(
            lookUpOrParseType(ret)
              .getOrElse(throw TypeDecodeException(s"Failed to parse type", ret))
          )
      }

    }

    val func = AstFunctionDecl(
      name,
      inputs.toList,
      if (outputs.nonEmpty)
        TyStructBuilder().fields(outputs.toList).build()
      else if (outputOnly.isDefined)
        outputOnly.get
      else
        TyStructBuilder().fields(Nil).build()
    ).trySetValue(KeySchema, if (schema.isEmpty) None else Some(schema))
    func.setValue(KeyBody, AstRawCodeImpl(body, Some(language)))
    if (outputOnly.isEmpty)
      func.setValue(KeyRecords, true)
    logger.debug(
      s"Parsed function: ${func.name}(${func.parameters})->${func.returnType}"
    )
    Some((func, end))
  }
  def parseCreateTable(
      tbl: CreateTable
  )(implicit resolver: TypeDecoder[String]): AstClassDecl = {
    AstClassDeclBuilder()
      .name(tbl.getTable.getName)
      .parameters(tbl.getColumnDefinitions.asScala.map(parseParseColumn).toList)
      .schema(Option(tbl.getTable.getSchemaName))
      .build()
  }
  def parseParseColumn(
      column: ColumnDefinition
  )(implicit resolver: TypeDecoder[String]): AstValDef = {
    val ty = column.getColDataType
    val name = column.getColumnName
    AstValDefImpl(
      name,
      lookUpOrParseType(ty.getDataType).getOrElse(
        throw TypeDecodeException("Failed to parse type", ty.getDataType)
      ),
      None,
      None
    )
  }
  def lookUpOrParseType(ty: String)(implicit resolver: TypeDecoder[String]): Option[TyNode] = {
    val x = ty.replaceAll("\\w+?\\.", "").trim
    resolver
      .decode(x)
      .orElse(sqlCommon.decode(ty))
  }

}

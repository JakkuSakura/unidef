package unidef.languages.sql
import com.alibaba.druid.DbType
import com.alibaba.druid.sql.parser.SQLParser
import com.typesafe.scalalogging.Logger
import com.alibaba.druid.sql.SQLUtils
import com.alibaba.druid.sql.ast.SQLParameter.ParameterType
import com.alibaba.druid.sql.ast.{SQLParameter, SQLStatement, SQLTableDataType}
import com.alibaba.druid.sql.ast.statement.{SQLColumnDefinition, SQLCreateFunctionStatement, SQLCreateTableStatement}
import com.alibaba.druid.util.JdbcConstants
import unidef.common.ast.*
import unidef.common.ty.*
import org.apache.commons.lang3.StringUtils
import unidef.utils.TextTool.{finds, findss}
import unidef.common.*
import unidef.languages.sql.SqlCommon.{KeyRecords, KeySchema}
import unidef.utils.TypeDecodeException

import scala.jdk.CollectionConverters.*
import java.util
import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex

class DruidSqlParser {
  val logger: Logger = Logger[this.type]
  val sqlCommon: SqlCommon = SqlCommon()
  def parse(sql: String, dialect: DbType = DbType.postgresql)(implicit
      resolver: TypeRegistry
  ): List[AstNode] = {
    val enums = extractEnums(sql)
    val collected = ArrayBuffer[AstNode]()

    enums.foreach { x =>
      x.name.foreach { nm =>
        resolver.add(nm, x, "sql")
        collected += AstTyped(x)
      }

    }
    val cleaned = stripUnsurpported(sql)
    val stmts = SQLUtils.parseStatements(cleaned, dialect)
    stmts.asScala.foreach {
      case table: SQLCreateTableStatement => collected += parseCreateTable(table)
      case func: SQLCreateFunctionStatement =>
        collected += parseCreateFunction(func)
      case _ =>
    }
    collected.toList
  }

  private def extractEnums(sql: String): List[TyEnum] = {
    new Regex("(CREATE|create)\\s+(TYPE|type)\\s+(.+)\\s+(AS|as)\\s+(ENUM|enum)\\s*\\((.+?)\\);")
      .findAllMatchIn(sql)
      .map(m => m.group(3) -> m.group(6))
      .map { (k, v) =>
        k -> v
          .split(",")
          .map(StringUtils.strip(_, " '"))
          .map(variantName => TyVariant(List(variantName), None, Some(variantName)))
      }
      .map {
        case (s"$schema.$name", v) =>
          TyEnum(v.toList, None, Some(name), Some(TyStringImpl()), Some(schema))
        case (enumName, v) =>
          TyEnum(v.toList, None, Some(enumName), Some(TyStringImpl()))

      }
      .toList
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


  private def parseColumn(
                          arg: SQLColumnDefinition
                        )(implicit resolver: TypeDecoder[String]): AstValDef = {
    logger.debug("parseColumn: " + arg)

    val name = arg.getName.getSimpleName.replaceAll("\"", "")
    val tyName = arg.getDataType.getName
    val ty = lookUpOrParseType(tyName)
      .getOrElse(throw TypeDecodeException(s"Failed to parse type", tyName))
    AstValDefImpl(name, ty, None, None)
  }

  private def parseParam(
                          arg: SQLParameter
  )(implicit resolver: TypeDecoder[String]): (ParameterType, TyField) = {
    logger.debug("parseParam: " + arg)

    val name = arg.getName.getSimpleName.replaceAll("\"", "")
    val tyName = arg.getDataType.getName
    val default = arg.getDefaultValue.toString
    val ty = lookUpOrParseType(tyName)
      .getOrElse(throw TypeDecodeException(s"Failed to parse type", tyName))
    if (default == "NULL")
      (arg.getParamType, TyFieldBuilder().name(name).value(TyOptionalImpl(ty)).build())
    else
      (arg.getParamType, TyFieldBuilder().name(name).value(ty).build())
  }

  def parseCreateFunction(
      stmt: SQLCreateFunctionStatement
  )(implicit resolver: TypeDecoder[String]): AstFunctionDecl = {
    val schema = stmt.getSchema
    val name = stmt.getName.getSimpleName

    val params = stmt.getParameters.asScala.map(parseParam).toList

    val inputs = params.filter(_._1 == ParameterType.IN).map(_._2)
    val outputs = ArrayBuffer[AstValDef]()
    var outputOnly: Option[TyNode] = None

    val body = stmt.getBlock
    val language = stmt.getLanguage
    stmt.getReturnDataType match {
      case x: SQLTableDataType =>
        outputs ++= x.getColumns.asScala.map(parseColumn)
      case x =>
        outputOnly = Some(lookUpOrParseType(x.getName).getOrElse(throw TypeDecodeException(s"Failed to parse type", x.getName)))
    }

    val func = AstFunctionDecl(
      name,
      inputs,
      if (outputs.nonEmpty)
        TyStructBuilder().fields(outputs.map(x => TyFieldBuilder().name(x.getName).value(x.getTy).build()).toList).build()
      else if (outputOnly.isDefined)
        outputOnly.get
      else
        TyStructBuilder().fields(Nil).build()
    ).trySetValue(KeySchema, if (schema.isEmpty) None else Some(schema))
    func.setValue(KeyBody, AstRawCodeImpl(body.toString, Some(language)))
    if (outputOnly.isEmpty)
      func.setValue(KeyRecords, true)
    logger.debug(
      s"Parsed function: ${func.name}(${func.parameters})->${func.returnType}"
    )

    func
  }
  def parseCreateTable(
      tbl: SQLCreateTableStatement
  )(implicit resolver: TypeDecoder[String]): AstClassDecl = {
    AstClassDecl(
      tbl.getTableName,
      tbl.getColumnDefinitions.asScala.map(parseColumn).toList,
      Nil
    )
      .trySetValue(KeySchema, Option(tbl.getSchema))
  }

  def lookUpOrParseType(ty: String)(implicit resolver: TypeDecoder[String]): Option[TyNode] = {
    val x = ty.replaceAll("\\w+?\\.", "").trim
    resolver
      .decode(x)
      .orElse(sqlCommon.decode(ty))
  }

}

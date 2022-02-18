package com.jeekrs.unidef
package languages.sql

import languages.common._
import languages.sql.SqlCommon.{Schema, convertTypeFromSql}

import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.statement.create.function.CreateFunction
import net.sf.jsqlparser.statement.create.table.{
  ColDataType,
  ColumnDefinition,
  CreateTable
}

import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters._

case class SqlParser() {
  // does not support default value yet

  private var collected = ArrayBuffer[AstNode]()
  private def takeBuffer: Seq[AstNode] = {
    val ret = collected
    collected = ArrayBuffer[AstNode]()

    ret.toSeq
  }

  def parse(sql: String): Seq[AstNode] = {
    val stmts = CCJSqlParserUtil.parseStatements(sql)
    stmts.getStatements.asScala.foreach {
      case table: CreateTable   => collected += parseCreateTable(table)
      case func: CreateFunction => // ???
      case _                    =>
    }
    takeBuffer
  }
  def parseCreateTable(tbl: CreateTable): AstClassDecl = {
    AstClassDecl(
      AstLiteralString(tbl.getTable.getName),
      tbl.getColumnDefinitions.asScala.map(parseParseColumn).toSeq
    ).setValue(Schema, tbl.getTable.getSchemaName)
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

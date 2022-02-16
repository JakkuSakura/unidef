package com.jeekrs.unidef
package languages.sql

import languages.common.AstNode

import net.sf.jsqlparser.parser.CCJSqlParserUtil

import scala.collection.mutable.ArrayBuffer

case class SqlParser() {
  private var collected = ArrayBuffer[AstNode]()
  private def takeBuffer: Seq[AstNode] = {
    val ret = collected
    collected = ArrayBuffer[AstNode]()
    ???
    ret.toSeq
  }

  def parse(sql: String): Seq[AstNode] = {
    val stmt = CCJSqlParserUtil.parseStatements(sql)
    ???
    takeBuffer
  }
  def parseFunc(sql: String): Seq[AstNode] = {
    ???
  }

  def parseTable(sql: String): Seq[AstNode] = {
    ???
  }

}

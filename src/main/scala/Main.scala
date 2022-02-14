package com.jeekrs.unidef

import languages.common.{ClassDeclNode, FunctionDeclNode}
import languages.python.PythonSqlCodeGen
import languages.sql.SqlCodeGen
import languages.sql.SqlCodeGen.{generateFunctionDdl, generateTableDdl}
import languages.yaml.YamlParser

import scala.io.Source

object Main {
  def main(args: Array[String]): Unit = {
    val filename = args(0)
    val source = Source.fromFile(filename)
    val fileContents = source.mkString
    source.close
    YamlParser.prepareForExtKeys(PythonSqlCodeGen)
    YamlParser.prepareForExtKeys(SqlCodeGen)
    val parsed = YamlParser.parseFile(fileContents)
    println(parsed)
    for (ty <- parsed) {
      val code = ty match {
        case n: ClassDeclNode    => generateTableDdl(n)
        case n: FunctionDeclNode => generateFunctionDdl(n)
      }
      println(code)
      ty match {
        case func: FunctionDeclNode =>
          val code2 = PythonSqlCodeGen.generateFuncWrapper(func)
          println(code2)
        case _ =>
      }

    }
  }
}

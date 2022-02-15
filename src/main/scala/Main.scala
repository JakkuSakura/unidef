package com.jeekrs.unidef

import languages.common.{AstClassDecl, AstFunctionDecl}
import languages.python.PythonSqlCodeGen
import languages.sql.SqlCodeGen
import languages.sql.SqlCodeGen.{generateFunctionDdl, generateTableDdl}
import languages.yaml.YamlParser

import com.jeekrs.unidef.languages.javascript.JsonSchemaCodeGen

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
        case n: AstClassDecl    => generateTableDdl(n)
        case n: AstFunctionDecl => generateFunctionDdl(n)
      }
      println(code)
      ty match {
        case func: AstFunctionDecl =>
          val code2 = PythonSqlCodeGen.generateFuncWrapper(func)
          println(code2)
          val code3 = JsonSchemaCodeGen.generateFuncDecl(func)
          println(code3)
        case _ =>
      }

    }
  }
}

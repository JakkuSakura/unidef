package com.jeekrs.unidef

import languages.sql.SqlCodeGen
import languages.yaml.YamlParser

import scala.io.Source

object Main {
  def main(args: Array[String]): Unit = {
    val filename = args(0)
    val source = Source.fromFile(filename)
    val fileContents = source.getLines.mkString("\n")
    source.close
    val parsed = YamlParser.parseFile(fileContents)
    println(parsed)
    for (ty <- parsed) {
      val code = SqlCodeGen.generateCode(ty)
      println(code)
    }
  }
}

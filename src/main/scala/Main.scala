package com.jeekrs.unidef

import languages.yaml.YamlParser

import scala.io.Source

object Main {
  def main(args: Array[String]): Unit = {
    val filename = args(0)
    val source = Source.fromFile(filename)
    val fileContents = source.getLines.mkString("\n")
    source.close
    val parser = new YamlParser()
    val parsed = parser.parseFile(fileContents)
    println(parsed)
  }
}

package com.jeekrs.unidef

import languages.yaml.YamlParser

import scala.io.Source

@main
def main(filename: String): Unit = {
    val source = Source.fromFile(filename)
    val fileContents = source.getLines.mkString("\n")
    source.close
    val parser = YamlParser()
    val parsed = parser.parseFile(fileContents)
    println(parsed)
}

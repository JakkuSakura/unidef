package com.jeekrs.unidef
package utils

import scala.io.Source

object FileUtils {
  def openFile(filename: String): String = {
    val source = Source.fromFile(filename)
    val fileContents = source.mkString
    source.close
    fileContents
  }
}

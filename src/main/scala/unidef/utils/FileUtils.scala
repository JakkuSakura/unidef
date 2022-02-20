package unidef.utils

import scala.io.Source

object FileUtils {
  def readFile(filename: String): String = {
    val source = Source.fromFile(filename)
    val fileContents = source.mkString
    source.close
    fileContents
  }
}

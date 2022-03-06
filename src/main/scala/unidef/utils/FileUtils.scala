package unidef.utils

import java.io.FileNotFoundException
import scala.io.Source
import scala.util.Try

object FileUtils {
  def readFile(filename: String): String = {
    val source = Source.fromFile(filename)
    val fileContents = source.mkString
    source.close
    fileContents
  }
}

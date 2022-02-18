package unidef.utils

import org.apache.commons.lang3.StringUtils.splitByCharacterTypeCamelCase

object TextTool {
  def indent(text: String, indent: Int): String = {
    val indentStr = " " * indent
    val spt = text.split("\n")
    if (spt.length == 1)
      spt(0)
    else {
      spt.head + "\n" + (1 until spt.length)
        .map(spt(_))
        .map(indentStr + _)
        .mkString("\n")
    }
  }
  def toSnakeCase(text: String): String =
    splitByCharacterTypeCamelCase(text).mkString("_").toLowerCase

}

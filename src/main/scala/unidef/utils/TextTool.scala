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
  def find(text: String, pattern: String, startPos: Int = 0): Option[Int] = {
    text.indexOf(pattern, startPos) match {
      case -1 => None
      case i  => Some(i)
    }
  }
  def finds(text: Seq[String],
            pattern: String,
            startPos: Int = 0): Option[Int] = {
    text.indexOf(pattern, startPos) match {
      case -1 => None
      case i  => Some(i)
    }
  }
  def rfind(text: String, pattern: String): Option[Int] = {
    text.lastIndexOf(pattern) match {
      case -1 => None
      case i  => Some(i)
    }
  }
  def rfinds(text: Seq[String], pattern: String): Option[Int] = {
    text.lastIndexOf(pattern) match {
      case -1 => None
      case i  => Some(i)
    }
  }
  def toSnakeCase(text: String): String =
    splitByCharacterTypeCamelCase(text).mkString("_").toLowerCase

}

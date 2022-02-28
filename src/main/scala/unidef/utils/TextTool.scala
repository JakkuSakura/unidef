package unidef.utils

import org.apache.commons.lang3.StringUtils.{splitByCharacterTypeCamelCase, splitByWholeSeparator}

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
      case i => Some(i)
    }
  }
  def finds(text: Seq[String], pattern: String, startPos: Int = 0): Option[Int] = {
    text.indexOf(pattern, startPos) match {
      case -1 => None
      case i => Some(i)
    }
  }
  def findss(text: Seq[String], pattern: Seq[String], startPos: Int = 0): Option[Int] = {
    for (p <- pattern)
      finds(text, p, startPos) match {
        case None =>
        case Some(i) => return Some(i)
      }
    None
  }
  def rfind(text: String, pattern: String): Option[Int] = {
    text.lastIndexOf(pattern) match {
      case -1 => None
      case i => Some(i)
    }
  }
  def rfinds(text: Seq[String], pattern: String): Option[Int] = {
    text.lastIndexOf(pattern) match {
      case -1 => None
      case i => Some(i)
    }
  }
  def splitString(text: String): Seq[String] = {
    Some(text)
      .map(_.replaceAll("-", "_"))
      .map(_.replaceAll("/", "_"))
      .map(_.replaceAll("\\.", "_"))
      .map(splitByWholeSeparator(_, null).mkString("_"))
      .map(splitByCharacterTypeCamelCase(_).mkString("_"))
      .map(splitByWholeSeparator(_, "_"))
      .get
  }
  def toSnakeCase(text: String): String =
    splitString(text).mkString("_").toLowerCase
  def toStreamingSnakeCase(text: String): String =
    splitString(text).mkString("_").toUpperCase
  def toPascalCase(text: String): String =
    splitString(text).map(_.capitalize).mkString("")
  def toKebabCase(text: String): String =
    splitString(text).mkString("-").toLowerCase
  def toStreamingKebabCase(text: String): String =
    splitString(text).mkString("-").toUpperCase
  def toCamelCase(text: String): String = {
    val x = splitString(text)
    (x.head.toLowerCase ++ x.slice(1, x.length).map(_.capitalize)).mkString("")
  }
  def replace(text: String, pattern: String, replacement: String): String =
    text.replaceAll(pattern, replacement)

}

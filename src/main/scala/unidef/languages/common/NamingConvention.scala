package unidef.languages.common

import unidef.utils.TextTool

trait NamingConvention {
  def toFunctionParameterName(s: String): String = ???
  def toFunctionName(s: String): String = ???
  def toVariableName(s: String): String = ???
  def toConstantName(s: String): String = ???
  def toClassName(s: String): String = ???
  def toStructName(s: String): String = ???
  def toFieldName(s: String): String = ???
  def toMethodName(s: String): String = ???
  def toEnumKeyName(s: String): String = ???
  def toEnumValueName(s: String): String = ???
}

trait NoopNamingConvention extends NamingConvention {
  override def toFunctionParameterName(s: String): String = s
  override def toFunctionName(s: String): String = s
  override def toVariableName(s: String): String = s
  override def toConstantName(s: String): String = s
  override def toClassName(s: String): String = s
  override def toStructName(s: String): String = s
  override def toFieldName(s: String): String = s
  override def toMethodName(s: String): String = s
  override def toEnumKeyName(s: String): String = s
  override def toEnumValueName(s: String): String = s
}
case object NoopNamingConvention extends NoopNamingConvention

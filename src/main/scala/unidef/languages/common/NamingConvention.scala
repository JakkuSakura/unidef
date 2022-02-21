package unidef.languages.common

import unidef.utils.TextTool

trait NamingConvention {
  def toFunctionParameterName(s: String): String = s
  def toFunctionName(s: String): String = s
  def toVariableName(s: String): String = s
  def toConstantName(s: String): String = s
  def toClassName(s: String): String = s
  def toStructName(s: String): String = s
  def toFieldName(s: String): String = s
  def toMethodName(s: String): String = s
  def toEnumName(s: String): String = s
}

case object NoopNamingConvention extends NamingConvention

case object PythonNamingConvention extends NamingConvention {
  override def toFunctionParameterName(s: String): String =
    s match {
      case s"_$rest"  => TextTool.toSnakeCase(rest)
      case s"a_$rest" => TextTool.toSnakeCase(rest)
      case _          => TextTool.toSnakeCase(s)
    }

  override def toVariableName(s: String): String = s
  override def toConstantName(s: String): String =
    TextTool.toStreamingSnakeCase(s)
  override def toClassName(s: String): String = TextTool.toPascalCase(s)
  override def toStructName(s: String): String = TextTool.toSnakeCase(s)
  override def toFieldName(s: String): String = TextTool.toSnakeCase(s)
  override def toMethodName(s: String): String = TextTool.toSnakeCase(s)
  override def toFunctionName(s: String): String = TextTool.toSnakeCase(s)
  override def toEnumName(s: String): String = TextTool.toStreamingSnakeCase(s)
}

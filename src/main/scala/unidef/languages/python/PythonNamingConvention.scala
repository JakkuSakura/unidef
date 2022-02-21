package unidef.languages.python

import unidef.languages.common.NamingConvention
import unidef.utils.TextTool

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
  override def toEnumValueName(s: String): String =
    TextTool.toStreamingSnakeCase(s)
}

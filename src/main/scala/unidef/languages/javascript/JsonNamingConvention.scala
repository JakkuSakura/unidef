package unidef.languages.javascript

import unidef.common.NamingConvention
import unidef.utils.TextTool

case object JsonNamingConvention extends NamingConvention {
  override def toFunctionParameterName(s: String): String = TextTool.toSnakeCase(s)
  override def toFunctionName(s: String): String = TextTool.toSnakeCase(s)
  override def toVariableName(s: String): String = TextTool.toSnakeCase(s)
  override def toConstantName(s: String): String = TextTool.toSnakeCase(s)
  override def toClassName(s: String): String = TextTool.toSnakeCase(s)
  override def toStructName(s: String): String = TextTool.toSnakeCase(s)
  override def toFieldName(s: String): String = TextTool.toSnakeCase(s)
  override def toMethodName(s: String): String = TextTool.toSnakeCase(s)
  override def toEnumKeyName(s: String): String = TextTool.toSnakeCase(s)
  override def toEnumValueName(s: String): String = TextTool.toSnakeCase(s)
}

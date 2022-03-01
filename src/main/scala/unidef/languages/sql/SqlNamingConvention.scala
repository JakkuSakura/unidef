package unidef.languages.sql

import unidef.languages.common.NamingConvention
import unidef.utils.TextTool

case object SqlNamingConvention extends NamingConvention {
  override def toFunctionParameterName(s: String): String =
    TextTool.toSnakeCase(s)
  override def toVariableName(s: String): String = TextTool.toSnakeCase(s)
  override def toConstantName(s: String): String =
    TextTool.toScreamingSnakeCase(s)
  override def toClassName(s: String): String = s.toLowerCase
  override def toFieldName(s: String): String = TextTool.toSnakeCase(s)
  override def toFunctionName(s: String): String = TextTool.toSnakeCase(s)
  override def toEnumValueName(s: String): String = TextTool.toSnakeCase(s)
}

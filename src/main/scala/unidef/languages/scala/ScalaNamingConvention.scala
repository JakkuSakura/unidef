package unidef.languages.scala

import unidef.common.NoopNamingConvention
import unidef.utils.TextTool

case object ScalaNamingConvention extends NoopNamingConvention {
  override def toFieldName(s: String): String = TextTool.toCamelCase(s)
}

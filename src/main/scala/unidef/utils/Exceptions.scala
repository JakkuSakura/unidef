package unidef.utils

case class ParseCodeException(msg: String, cause: Throwable = null) extends Exception(msg)
case class TypeLookupException(msg: String, cause: Throwable = null) extends Exception(msg)

case class CodegenException(msg: String, cause: Throwable = null) extends Exception(msg)

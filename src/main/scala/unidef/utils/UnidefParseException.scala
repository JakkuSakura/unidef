package unidef.utils

case class UnidefParseException(msg: String, cause: Throwable = null)
    extends Exception(msg)

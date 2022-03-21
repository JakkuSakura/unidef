package unidef.utils

import unidef.languages.common.TyNode

class ExceptionBase(msg: String, cause: Option[Throwable] = None)
    extends Exception(msg, cause.orNull)

case class ParseCodeException(msg: String, code: String = "", cause: Option[Throwable] = None)
    extends ExceptionBase(s"$msg (code: $code)", cause)
case class TypeEncodeException(msg: String, ty: TyNode, cause: Option[Throwable] = None)
    extends ExceptionBase(s"$msg (type: $ty)", cause)
case class TypeDecodeException[I](
    msg: String,
    ty: I,
    lang: String = "",
    cause: Option[Throwable] = None
) extends ExceptionBase(s"$msg (type: $ty $lang)", cause)

case class CodegenException(msg: String, cause: Option[Throwable] = None) extends ExceptionBase(msg)

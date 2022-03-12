package unidef.languages.schema

import io.circe.{Json, JsonNumber, JsonObject}
import io.circe.syntax.*
import unidef.languages.common.{AstLiteral, AstLiteralBoolean, AstLiteralInteger, AstNode, TyEnum}
import unidef.utils.ParseCodeException

import scala.collection.mutable

case class AstTypeRef(name: String) extends AstNode
case class AstTypeApply(ty: AstTypeRef, args: Map[String, AstNode] = Map.empty) extends AstNode
trait AstDecl extends AstNode

case class AstTypeDecl(name: String, params: Map[String, AstTypeApply]) extends AstDecl {}
case class AstEnumDecl(e: TyEnum) extends AstDecl
case class AstBuiltin(name: String) extends AstDecl

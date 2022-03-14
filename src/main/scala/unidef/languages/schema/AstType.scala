package unidef.languages.schema

import io.circe.{Json, JsonNumber, JsonObject}
import io.circe.syntax.*
import unidef.languages.common.{
  AstLiteral,
  AstLiteralBoolean,
  AstLiteralInteger,
  AstNode,
  TyEnum,
  TyField,
  TyNode
}
import unidef.utils.ParseCodeException

import scala.collection.mutable

case class AstTypeRef(name: String) extends AstNode
case class AstTypeApply(ty: AstTypeRef, args: Map[String, AstNode] = Map.empty) extends AstNode
trait AstDecl extends AstNode

case class AstTypeDecl(name: String, params: Map[String, AstTypeApply]) extends AstDecl {}
case class AstEnumDecl(e: TyEnum) extends AstDecl
case class AstBuiltin(name: String) extends AstDecl

class TypeBuilder(val name: String) {
  val fields: mutable.ArrayBuffer[TyField] = mutable.ArrayBuffer.empty
  val equivalent: mutable.ArrayBuffer[TyNode] = mutable.ArrayBuffer.empty

  def field(name: String, ty: TyNode): TypeBuilder = {
    fields += TyField(name, ty)
    this
  }

  def is(ty: TyNode): TypeBuilder = {
    equivalent += ty
    this
  }

}

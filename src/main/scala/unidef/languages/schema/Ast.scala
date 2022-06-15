package unidef.languages.schema

import unidef.common.ty.{TyField, TyNode}

import scala.collection.mutable

class Ast(val name: String) {
  val fields: mutable.ArrayBuffer[TyField] = mutable.ArrayBuffer.empty
  val commentable: Boolean = false
  val equivalent = Nil
  def field(name: String, ty: TyNode, required: Boolean = false): Ast = {
    fields += TyField(name, ty, None, Some(!required))
    this
  }

}

package unidef.languages.schema

import unidef.common.ty.{TyField, TyFieldBuilder, TyNode}

import scala.collection.mutable

class Ast(val name: String) {
  val fields: mutable.ArrayBuffer[TyFieldBuilder] = mutable.ArrayBuffer.empty
  def field(name: String, ty: TyNode, required: Boolean = false): Ast = {
    fields += TyFieldBuilder().name(name).value(ty).defaultNone(!required)
    this
  }

}

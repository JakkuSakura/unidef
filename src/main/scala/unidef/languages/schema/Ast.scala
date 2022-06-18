package unidef.languages.schema

import unidef.common.ast.*
import unidef.common.ty.*
import unidef.utils.TextTool

import scala.collection.mutable

class Ast(val name: String) {
  val fields: mutable.ArrayBuffer[AstValDefBuilder] = mutable.ArrayBuffer.empty
  def field(name: String, ty: TyNode, required: Boolean = false): Ast = {
    val builder = AstValDefBuilder().name(TextTool.toCamelCase(name))
    if (!required) {
      builder.ty(Types.option(ty))
    } else {
      builder.ty(ty)
    }
    fields += builder
    this
  }

}

package unidef.languages.schema

import io.circe.{Json, JsonNumber, JsonObject}
import io.circe.syntax.*
import unidef.common.ty.*
import unidef.common.ast.*
import unidef.utils.{ParseCodeException, TextTool}

import scala.collection.mutable

class Type(val name: String) {
  val fields: mutable.ArrayBuffer[AstValDefBuilder] = mutable.ArrayBuffer.empty
  val equivalent: mutable.ArrayBuffer[TyNode] = mutable.ArrayBuffer.empty
  val derives: mutable.ArrayBuffer[String] = mutable.ArrayBuffer.empty

  def field(name: String, ty: TyNode, required: Boolean = false): Type = {
    val builder = AstValDefBuilder().name(TextTool.toCamelCase(name))
    if (!required) {
      builder.ty(Types.option(ty))
    } else {
      builder.ty(ty)
    }
    
    fields += builder
    this
  }
  def setCommentable(commentable: Boolean): Type = {
    if (commentable) {
      this.field("comment", Types.string(), required = false)
      this.derives += "HasComment"
      this
    } else {
      this
    }
  }
  def is(ty: TyNode): Type = {
    equivalent += ty
    this
  }

}

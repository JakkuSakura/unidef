package unidef.languages.schema

import io.circe.{Json, JsonNumber, JsonObject}
import io.circe.syntax.*
import unidef.common.ty.*
import unidef.common.ast.*
import unidef.utils.ParseCodeException

import scala.collection.mutable

class Type(val name: String) {
  val fields: mutable.ArrayBuffer[TyFieldBuilder] = mutable.ArrayBuffer.empty
  val equivalent: mutable.ArrayBuffer[TyNode] = mutable.ArrayBuffer.empty

  def field(name: String, ty: TyNode, required: Boolean = false): Type = {
    fields += TyFieldBuilder().name(name).value(ty).defaultNone(!required)
    this
  }
  def setCommentable(commentable: Boolean): Type = {
    if (commentable) {
      this.field("comment", TyStringImpl(), required = false)
    } else {
      this
    }
  }
  def is(ty: TyNode): Type = {
    equivalent += ty
    this
  }

}

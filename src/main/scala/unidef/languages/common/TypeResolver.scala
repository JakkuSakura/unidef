package unidef.languages.common

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

trait TypeResolver {
  def decode(language: String, typeName: String)(
    implicit resolver: TypeResolver
  ): Option[TyNode]
  def encode(language: String, node: TyNode)(
    implicit resolver: TypeResolver
  ): Option[String]
}
object EmptyTypeResolver extends TypeResolver {
  override def decode(language: String, typeName: String)(
    implicit resolver: TypeResolver
  ): Option[TyNode] = None

  override def encode(language: String, node: TyNode)(
    implicit resolver: TypeResolver
  ): Option[String] = None
}

class TypeRegistry extends TypeResolver {
  private val registries = ArrayBuffer[TypeResolver]()
  def register(registry: TypeResolver): Unit = {
    registries += registry
  }
  override def decode(language: String, typeName: String)(
    implicit resolver: TypeResolver
  ): Option[TyNode] = {
    for (registry <- registries) {
      registry.decode(language, typeName) match {
        case Some(ty) => return Some(ty)
        case None     =>
      }
    }
    None
  }

  override def encode(language: String, node: TyNode)(
    implicit resolver: TypeResolver
  ): Option[String] = {
    for (registry <- registries) {
      registry.encode(language, node) match {
        case Some(ty) => return Some(ty)
        case None     =>
      }
    }
    None
  }
}

case class RefTypeRegistry(language: String) extends TypeResolver {
  val mapping = new mutable.HashMap[String, TyNode]()

  def add(s: String, ty: TyNode): Unit = mapping += s -> ty

  override def decode(language: String, typeName: String)(
    implicit resolver: TypeResolver
  ): Option[TyNode] = {
    if (language == this.language) {
      mapping.get(typeName)
    } else {
      None
    }
  }

  override def encode(language: String, node: TyNode)(
    implicit resolver: TypeResolver
  ): Option[String] = {
    if (language == this.language) {
      mapping.find(_._2 == node).map(_._1)
    } else {
      None
    }
  }
}

package unidef.common.ast

import scala.collection.mutable

trait AstImport extends AstNode
object AstImport {
  private def spt(s: String): List[String] = s.split(raw"\.|::|/|\\").toList
  def apply(s: String): AstImport = AstImportSimple(spt(s), List(spt(s).last))
}
case class AstImportSimple(obj: List[String], use: List[String]) extends AstImport

case class AstImportRaw(imports: String) extends AstImport
case class ImportManager(imports: mutable.HashSet[AstImport] = mutable.HashSet()) {
  def +=(importNode: AstImport): Unit = imports += importNode
}

case class AstSourceFile(filename: String, imports: List[AstImport], body: List[AstNode])
    extends AstNode {}

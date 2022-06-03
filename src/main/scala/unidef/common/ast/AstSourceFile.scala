package unidef.common.ast

import scala.collection.mutable

trait AstImport extends AstNode
object AstImport {
  private def spt(s: String): Seq[String] = s.split(raw"\.|::|/|\\")
  def apply(s: String): AstImport = AstImportSimple(spt(s), Seq(spt(s).last))
}
case class AstImportSimple(obj: Seq[String], use: Seq[String]) extends AstImport

case class AstImportRaw(imports: String) extends AstImport
case class ImportManager(imports: mutable.HashSet[AstImport] = mutable.HashSet()) {
  def +=(importNode: AstImport): Unit = imports += importNode
}

case class AstSourceFile(filename: String, imports: Seq[AstImport], body: Seq[AstNode])
    extends AstNode {}

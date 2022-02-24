package unidef.languages.common

import scala.collection.mutable

trait AstImport extends AstNode
object AstImport {
  private def spt(s: String): Seq[String] = s.split(raw"\.|::|/|\\")
  def apply(s: String): AstImport = AstImportSingle(spt(s))
  def apply(s: String, objs: Seq[String]): AstImport = AstImportMulti(spt(s), objs)
}
case class AstImportSingle(paths: Seq[String]) extends AstImport {}
case class AstImportMulti(path: Seq[String], objs: Seq[String]) extends AstImport {}

case class ImportManager(imports: mutable.HashSet[AstImport] = mutable.HashSet()) {
  def +=(importNode: AstImport): Unit = imports += importNode
}

case class AstSourceFile(filename: String, imports: Seq[AstImport], body: Seq[AstNode])
    extends AstNode {}

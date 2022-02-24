package unidef.languages.common

import scala.collection.mutable

trait AstImport extends AstNode
object AstImport {
  def apply(s: String): AstImport = AstImportSingle(s.split(raw"\.|::|/|\\"))
}
case class AstImportSingle(paths: Seq[String]) extends AstImport {}

case class ImportManager(imports: mutable.HashSet[AstImport] = mutable.HashSet()) {
  def +=(importNode: AstImport): Unit = imports += importNode
}

case class AstSourceFile(filename: String, imports: Seq[AstImport], body: Seq[AstNode])
    extends AstNode {}

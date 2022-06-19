package unidef.common

trait BaseNode {}

trait HasComment() extends BaseNode {
  def comment: Option[String]
}

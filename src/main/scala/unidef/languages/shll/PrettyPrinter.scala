package unidef.languages.shll

import unidef.common.ast.*
import unidef.languages.scala.{ScalaCodeGen, ScalaNamingConvention}
object PrettyPrinter {
  val codegen = ScalaCodeGen(naming = ScalaNamingConvention) 
  def print(n: AstNode): Unit = {
    println(codegen.generate(n))
  }
  def toString(n: AstNode): String = {
    codegen.generate(n)
  }
}
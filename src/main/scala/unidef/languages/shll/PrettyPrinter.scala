package unidef.languages.shll

import unidef.common.ast.*
import unidef.languages.rust.RustCodeGen
import unidef.languages.scala.{ScalaCodeGen, ScalaNamingConvention}
object PrettyPrinter {
  val codegen = ScalaCodeGen(naming = ScalaNamingConvention)
  val rustCodegen = RustCodeGen(naming = ScalaNamingConvention)
  def printRust(n: AstNode): Unit = {
    println(rustCodegen.generate(n))
  }
  def print(n: AstNode): Unit = {
    println(codegen.generate(n))
  }
  def toString(n: AstNode): String = {
    codegen.generate(n)
  }
}
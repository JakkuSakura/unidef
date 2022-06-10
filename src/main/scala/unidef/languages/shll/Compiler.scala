package unidef.languages.shll

import unidef.common.ast.{AstNode, AstUnit}

import scala.sys.process.Process
import unidef.utils.FileUtils
import unidef.common.ast.*

import java.io.{File, FileWriter}
import java.nio.file.Files
import scala.io.Source
import scala.quoted.runtime.impl.QuotesImpl
import scala.quoted.{Expr, Quotes}
import scala.tasty.inspector.TastyInspector

class Compiler {
  def compileOnly(code: String): File = {
    val tempDir = Files.createTempDirectory("shll").toFile
    val path = File(tempDir, "main.scala")
    val writer = FileWriter(path)
    writer.write(code)
    writer.close()
    Process(Seq("scala3-compiler", path.getAbsolutePath), path.getParentFile).!!
    path
  }
  def compileAndLift(code: String): AstNode = {
    if (code.isEmpty) {
      return AstUnitImpl()
    }
    val path = compileOnly(code)

    val tastyFiles = path.getParentFile.listFiles((dir, name) => name.endsWith(".tasty")).map(_.getAbsolutePath).toList
    val lifter = TastyHelper()
    TastyInspector.inspectTastyFiles(tastyFiles)(lifter)
    lifter.getAstNode
  }
  transparent inline def stage[T](inline code: T): String = {
    ${
      stageImpl('code)
    }
  }

  def lift(code: Expr[Any]): AstNode = {
    import dotty.tools.dotc.core.Contexts.NoContext.given_Context
    given quotes: Quotes = QuotesImpl()

    liftImpl(code)
  }
  transparent inline def liftAndUnlift[T](inline code: T): T = {
    ${
      liftAndUnliftImpl('code)
    }
  }
}

object Compiler {
  def main(args: Array[String]): Unit =
    val source = FileUtils.readFile("examples/scala_parser.scala")
    val compiler = Compiler()
    val compiled = compiler.compileAndLift(source)
    println(compiled)
}

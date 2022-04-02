import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import unidef.languages.common.*
import unidef.languages.python.PythonCommon
import unidef.scalai.ScalaiCompiler

class ScalaiTest {
  @Test def test_hello_world(): Unit = {
    val compiler = ScalaiCompiler()
    val compiled = compiler.compileAndLift(
      """
        |def main(): Unit = {
        |}
        |""".stripMargin)
  }

  @Test def test_active_inlining(): Unit = {
    val compiler = ScalaiCompiler()
    // TODO: change to compile
    val compiled = compiler.compileOnly(
      """
        |import scala.io.StdIn.readInt
        |def foo(x: Int): Int = x * 2
        |def main(): Unit = {
        |  println(foo(1) + foo(readInt()))
        |}
        |""".stripMargin)
  }
  @Test def test_loop_unfolding(): Unit = {
    val compiler = ScalaiCompiler()
    // TODO: change to compile
    val compiled = compiler.compileOnly(
      """
        |def foo(xs: Seq[() => Int]): Unit = for(func <- xs) println(func)
        |def main(): Unit = {
        |  val x1 = () => 1
        |  val x2 = () => 2
        |  val x3 = () => 3
        |  val xs = Seq(x1, x2, x3)
        |  foo(xs)
        |}
        |""".stripMargin)
  }

  @Test def test_specialization(): Unit = {
    val compiler = ScalaiCompiler()
    // TODO: change to compile
    val compiled = compiler.compileOnly(
      """
        |import scala.io.StdIn.readInt
        |@noinline
        |def foo(a: Int)(b: Int): Int = a + b
        |@noinline
        |def bar(a: Int, b: Int): Int = a + b
        |def main(): Unit = {
        |  foo(1)(readInt())
        |  bar(2, readInt()) // auto currying and specialization
        |  bar(readInt(), 3)
        |}
        |""".stripMargin)
  }


  def test_type_specialization1(): Unit = {
    val compiler = ScalaiCompiler()
    // TODO: change to compile
    val compiled = compiler.compileOnly(
      """
        |@noinline
        |def foo[T]() = {
        |  if (T ==  Boolean) {
        |    true
        |  } else {
        |    "not a boolean"
        |  }
        |}
        |
        |def main(): Unit = {
        |  foo[Boolean]
        |  foo[Int]
        |}
        |""".stripMargin)
  }

  @Test def test_type_specialization2(): Unit = {
    val compiler = ScalaiCompiler()
    // TODO: change to compile
    val compiled = compiler.compileOnly(
      """
        |type Type = Any
        |// This syntax is better
        |@noinline
        |def bar(t: Type) = {
        |  if (t == Boolean) {
        |    true
        |  } else {
        |    "not a boolean"
        |  }
        |}
        |def main(): Unit = {
        |  bar(Boolean)
        |  bar(Int)
        |}
        |""".stripMargin)
  }


}

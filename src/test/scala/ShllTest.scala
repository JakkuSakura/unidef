import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import unidef.common.ast.*
import unidef.common.ty.*
import unidef.languages.python.PythonCommon
import unidef.languages.shll.{Compiler, PrettyPrinter, Specializer}

private object ShllTestHelper {
  def compileAndLift(code: String): AstNode = {
    val compiler = new Compiler()
    val lifted = compiler.compileAndLift(code)
    lifted
  }

  inline def lift[T](inline code: T): AstNode = {
    val compiler = new Compiler()
    val staged = compiler.stage(code)
    val lifted = compiler.compileAndLift(s"""
        |class Main {
        |  val value = {
        |    $staged
        |  }
        |}
        |""".stripMargin)
    val extracted = lifted
      .asInstanceOf[AstProgram]
      .stmts
      .head
      .asInstanceOf[AstClassDecl]
      .fields
      .head
      .value
      .get

    extracted
  }

  def assertAstEqual(expected: AstNode, actual: AstNode): Unit = {
    val expectedString = PrettyPrinter.toString(expected)
    val actualString = PrettyPrinter.toString(actual)
    if (expectedString != actualString)
      assertEquals(expectedString, actualString)
    else if (expected != actual)
      assertEquals(expected, actual)
  }

}
class ShllTest {
  @Test def test_simple_math(): Unit = {
    val x = ShllTestHelper.lift {
      2
    }
    val y = ShllTestHelper.lift {
      1 + 1
    }
    assertEquals(x, y)

  }
  @Test def test_function_decls(): Unit = {
    val x = ShllTestHelper.lift {
      def main(): Unit = {}
    }
    assertEquals(x.asInstanceOf[AstDecls].decls.length, 1)
    val y = ShllTestHelper.lift {
      def foo(a: Int): Unit = {}
      def bar(a: Int, b: Int): Unit = {}
    }
    println(y)
    assertEquals(y.asInstanceOf[AstDecls].decls.length, 2)
  }
  @Test def test_simple_specialize(): Unit = {
    val code = ShllTestHelper.lift {
      def foo(a: Int): Int = a
      def bar(): Unit = {
        foo(1)
        foo(2)
      }
    }

    val expected = ShllTestHelper.lift {
      def foo_0(): Int = 1
      def foo_1(): Int = 2
      def foo(a: Int): Int = a
      def bar(): Unit = {
        foo_0()
        foo_1()
      }
    }
    val specialized = Specializer().specialize(code)
    PrettyPrinter.printRust(specialized)
    PrettyPrinter.print(specialized)
    ShllTestHelper.assertAstEqual(expected, specialized)
  }
  @Test def test_let_var(): Unit = {
    val code = ShllTestHelper.lift {
      def bar(): Unit = {
        val a = 1
      }
    }

    val specialized = Specializer().specialize(code)
    PrettyPrinter.print(specialized)
//    ShllTestHelper.assertAstEqual(expected, specialized)
  }
  @Test def test_specialize_let_var(): Unit = {
    val code = ShllTestHelper.lift {
      def foo(a: Int) = a
      def bar(): Unit = {
        val a = 1
        foo(a)
      }
    }

    val specialized = Specializer().specialize(code)
    PrettyPrinter.print(specialized)
    //    ShllTestHelper.assertAstEqual(expected, specialized)
  }
  @Test def test_active_inlining(): Unit = {
    ShllTestHelper.compileAndLift("""
        |import scala.io.StdIn.readInt
        |def foo(x: Int): Int = x * 2
        |def main(): Unit = {
        |  println(foo(1) + foo(readInt()))
        |}
        |""".stripMargin)
  }
  @Test def test_loop_unfolding(): Unit = {
    ShllTestHelper.compileAndLift("""
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
    ShllTestHelper.compileAndLift("""
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
    ShllTestHelper.compileAndLift("""
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
    ShllTestHelper.compileAndLift("""
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

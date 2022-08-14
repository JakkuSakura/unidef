import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import unidef.common.ast.*
import unidef.common.ty.*
import unidef.languages.python.PythonCommon
import unidef.languages.shll.{AntlrAstParser, Compiler, PrettyPrinter, Specializer}

class ShllAstTest {
  @Test def test_parser(): Unit = {
    val t = AntlrAstParser().parse("A()")

  }

}

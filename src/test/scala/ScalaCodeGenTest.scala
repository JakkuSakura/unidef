import org.junit.jupiter.api.Test
import unidef.common.NoopNamingConvention
import unidef.common.ty.*
import unidef.common.ast.*
import unidef.languages.scala.ScalaCodeGen
class ScalaCodeGenTest {
  @Test def testGenerateBuilder(): Unit = {
    val codegen = ScalaCodeGen(NoopNamingConvention)
    val b1 = codegen.generateBuilder(
      "Builder",
      "Target",
      List(AstValDefBuilder().name("a").ty(TyIntegerImpl(None, None)).build())
    )
    val c1 = codegen.generateClass(b1)
    println(c1)
  }
}

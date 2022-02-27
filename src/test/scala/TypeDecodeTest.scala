import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import unidef.languages.common.{TyInteger, TyNode, TypeDecoder}
import unidef.languages.python.PythonCommon

object TestHelper {
  def typeDecode(src: String, expected: TyNode)(using decoder: TypeDecoder): Unit = {
    val actual = decoder.decode(src)
    assertEquals(Some(expected), actual)
  }
}

class TypeDecodeTest {
  @Test def python_decoder(): Unit = {
    def test(using TypeDecoder): Unit = {
      TestHelper.typeDecode("int", TyInteger())
    }
    test(using PythonCommon())
  }

}

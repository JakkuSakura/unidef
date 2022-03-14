import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import unidef.languages.common.*
import unidef.languages.python.PythonCommon

object TestHelper {
  def typeDecode(src: String, expected: TyNode)(using decoder: TypeDecoder[String]): Unit = {
    val actual = decoder.decode(src)
    assertEquals(Some(expected), actual)
  }
}

class TypeDecodeTest {
  @Test def python_decoder(): Unit = {
    def test(using TypeDecoder[String]): Unit = {
      TestHelper.typeDecode("int", TyIntegerImpl(Some(BitSize.Unlimited), Some(true)))
    }
    test(using PythonCommon())
  }

}

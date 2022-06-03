import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import unidef.common.ty.*

import unidef.languages.python.PythonCommon

private object TypeDecoderTestHelper {
  def typeDecode(src: String, expected: TyNode)(using decoder: TypeDecoder[String]): Unit = {
    val actual = decoder.decode(src)
    assertEquals(Some(expected), actual)
  }
}

class TypeDecodeTest {
  @Test def python_decoder(): Unit = {
    def test(using TypeDecoder[String]): Unit = {
      TypeDecoderTestHelper.typeDecode("int", TyIntegerImpl(Some(BitSize.Unlimited), Some(true)))
    }
    test(using PythonCommon())
  }

}

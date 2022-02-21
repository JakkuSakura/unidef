package unidef.languages.python

import unidef.languages.common.{
  KeyName,
  KeywordProvider,
  PythonNamingConvention,
  TyEnum,
  TyInteger,
  TyString,
  TyVariant
}
import unidef.utils.CodeGen

import scala.jdk.CollectionConverters._

class PythonCodeGen extends KeywordProvider {

  protected val TEMPLATE_ENUM_CODEGEN: String =
    """
      |class $name(enum.$enum_type):
      |#foreach ($field in $fields)
      |    $field.name() = $field.orig_name()
      |#end
      |""".stripMargin

  def generateEnum(func: TyEnum): String = {
    val context = CodeGen.createContext
    context.put(
      "name",
      PythonNamingConvention.toClassName(func.getName.get.split("\\.").last)
    )
    func.getValue.getOrElse(TyString) match {
      case TyString     => context.put("enum_type", "StrEnum")
      case _: TyInteger => context.put("enum_type", "IntEnum")
    }
    var counter = -1
    context.put(
      "fields",
      func.variants
        .map(x => x.names.head -> x.code)
        .map {
          case (name, code) =>
            counter += 1
            PythonField(
              PythonNamingConvention.toEnumName(name),
              func.getValue.getOrElse(TyString) match {
                case TyString     => s"'$name'"
                case _: TyInteger => s"${code.getOrElse(counter)}"
              },
              null
            )
        }
        .asJava
    )

    CodeGen.render(TEMPLATE_ENUM_CODEGEN, context)
  }

}
object PythonCodeGen extends PythonCodeGen {
  def main(args: Array[String]): Unit = {
    println(
      generateEnum(
        TyEnum(
          List(TyVariant(List("a", "b", "c")), TyVariant(List("d", "e", "f")))
        ).setValue(KeyName, "the_enum")
      )
    )
  }
}

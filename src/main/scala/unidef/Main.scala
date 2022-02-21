package unidef

import unidef.languages.common.{
  AstClassDecl,
  AstFunctionDecl,
  AstTyped,
  RefTypeRegistry,
  TyApplicable,
  TyClass,
  TyStruct,
  TypeRegistry
}
import unidef.languages.javascript.{JsonSchemaCodeGen, JsonSchemaParser}
import unidef.languages.python.PythonSqlCodeGen
import unidef.languages.sql.SqlCodeGen.{generateFunctionDdl, generateTableDdl}
import unidef.languages.sql.{SqlCodeGen, SqlParser}
import unidef.languages.yaml.YamlParser
import unidef.utils.FileUtils

object Main {
  val parser = JsonSchemaParser(true)
  parser.prepareForExtKeys(PythonSqlCodeGen)
  parser.prepareForExtKeys(SqlCodeGen)

  def main(args: Array[String]): Unit = {
    implicit val resolver: TypeRegistry = new TypeRegistry()
    val sqlResolver: RefTypeRegistry = RefTypeRegistry("sql")
    resolver.register(sqlResolver)

    val filename = args(0)
    val fileContents = FileUtils.readFile(filename)
    val parsed = if (filename.endsWith(".yaml") || filename.endsWith(".yml")) {

      YamlParser.parseFile(fileContents)
    } else if (filename.endsWith(".sql"))
      SqlParser.parse(fileContents)(sqlResolver)
    else {
      throw new RuntimeException("Unsupported file type " + filename)
    }

    println(parsed)
    for (ty <- parsed) {
      val code = ty match {
        case AstTyped(n) if n.isInstanceOf[TyStruct] =>
          generateTableDdl(n.asInstanceOf[TyStruct])
        case AstTyped(n) if n.isInstanceOf[AstFunctionDecl] =>
          generateFunctionDdl(n.asInstanceOf[AstFunctionDecl])
      }
      println(code)
      ty match {
        case func: AstFunctionDecl =>
          val code2 = PythonSqlCodeGen.generateFuncWrapper(func)
          println(code2)
          val code3 = JsonSchemaCodeGen.generateFuncDecl(func)
          println(code3)
        case _ =>
      }

    }
  }
}

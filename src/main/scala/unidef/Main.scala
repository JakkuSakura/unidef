package unidef

import unidef.languages.common.{
  AstClassDecl,
  AstFunctionDecl,
  AstTyped,
  ImportManager,
  TyStruct,
  TypeRegistry
}
import unidef.languages.javascript.{JsonSchemaCodeGen, JsonSchemaParser}
import unidef.languages.python.{PythonCodeGen, PythonSqlCodeGen}
import unidef.languages.sql.{SqlCodeGen, SqlParser}
import unidef.languages.yaml.YamlParser
import unidef.utils.FileUtils

object Main {
  val pythonSqlCodeGen = PythonSqlCodeGen()
  val sqlCodegen = SqlCodeGen()
  val pyCodeGen = PythonCodeGen()
  val parser: JsonSchemaParser = JsonSchemaParser(true)
  parser.prepareForExtKeys(pythonSqlCodeGen)
  parser.prepareForExtKeys(sqlCodegen)
  val yamlParser: YamlParser = YamlParser(parser)
  def main(args: Array[String]): Unit = {
    implicit val sqlResolver: TypeRegistry = TypeRegistry()

    val filename = args(0)
    val fileContents = FileUtils.readFile(filename)
    val parsed = if (filename.endsWith(".yaml") || filename.endsWith(".yml")) {

      yamlParser.parseFile(fileContents)
    } else if (filename.endsWith(".sql"))
      SqlParser.parse(fileContents)(sqlResolver)
    else {
      throw new RuntimeException("Unsupported file type " + filename)
    }

    println(parsed)
    for (ty <- parsed) {
      ty match {
        case a @ AstClassDecl(name, fields, methods, derived) =>
          val code = sqlCodegen.generateTableDdl(a)
          println(code)
        case AstTyped(n) if n.isInstanceOf[TyStruct] =>
          val code = sqlCodegen.generateTableDdl(n.asInstanceOf[TyStruct])
          println(code)
        case n: AstFunctionDecl =>
          val code = sqlCodegen.generateFunctionDdl(n)
          println(code)
          val importManager = ImportManager()
          val code2 = pythonSqlCodeGen.generateFuncWrapper(n, importManager = Some(importManager))
          println(code2)
          val code3 =
            JsonSchemaCodeGen().generateFuncDecl(n)
          println(code3)
      }
    }
  }
}

package unidef

import unidef.languages.common.{
  AstClassDecl,
  AstFunctionDecl,
  AstTyped,
  ImportManager,
  TyEnum,
  TyStruct,
  TypeRegistry
}
import unidef.languages.javascript.{JsonSchemaCodeGen, JsonSchemaParser}
import unidef.languages.python.{PythonCodeGen, PythonSqlCodeGen}
import unidef.languages.sql.{SqlCodeGen, SqlParser}
import unidef.languages.yaml.YamlParser
import unidef.utils.{VirtualFileSystem, FileUtils}

import java.io.PrintWriter

@main def main(filename: String): Unit = {
  val pythonSqlCodeGen = PythonSqlCodeGen()
  val sqlCodegen = SqlCodeGen()
  val pyCodeGen = PythonCodeGen()
  val parser: JsonSchemaParser = JsonSchemaParser()
  parser.prepareForExtKeys(pythonSqlCodeGen)
  parser.prepareForExtKeys(sqlCodegen)
  val yamlParser: YamlParser = YamlParser(parser)
  implicit val sqlResolver: TypeRegistry = TypeRegistry()
  val fs = new VirtualFileSystem()
  val fileContents = FileUtils.readFile(filename)
  val parsed = if (filename.endsWith(".yaml") || filename.endsWith(".yml")) {
    yamlParser.parseFile(fileContents)
  } else if (filename.endsWith(".sql"))
    SqlParser.parse(fileContents)(sqlResolver)
  else {
    throw new RuntimeException("Unsupported file type " + filename)
  }
  val parsedWriter = fs.newWriterAt("parsed.txt")
  parsedWriter.println(parsed)
  for (ty <- parsed) {
    ty match {
      case a @ AstClassDecl(name, fields, methods, derived) =>
        val code = sqlCodegen.generateTableDdl(a)
        fs.newWriterAt("AstClassDecl.txt").println(code)
      case AstTyped(n) if n.isInstanceOf[TyStruct] =>
        val code = sqlCodegen.generateTableDdl(n.asInstanceOf[TyStruct])
        fs.newWriterAt("TyStruct.txt").println(code)

      case AstTyped(en: TyEnum) =>
        fs.newWriterAt("TyEnum.txt").println(en)
      case n: AstFunctionDecl =>
        val code = sqlCodegen.generateFunctionDdl(n)
        fs.newWriterAt("AstFunctionDeclSqlCodeGen.txt").println(code)

        val importManager = ImportManager()
        val code2 = pythonSqlCodeGen.generateFuncWrapper(n, importManager = Some(importManager))
        fs.newWriterAt("AstFunctionDeclPySqlCodeGen.txt").println(code2)

        val code3 =
          JsonSchemaCodeGen().generateFuncDecl(n)
        fs.newWriterAt("AstFunctionDeclJsonSchemaCodeGen.txt").println(code2)

    }
  }
  fs.showAsString(new PrintWriter(System.out))
  fs.closeFiles()
}

package unidef

import unidef.languages.common.{
  AstClassDecl,
  AstFunctionDecl,
  AstTyped,
  Extendable,
  ImportManager,
  TyEnum,
  TyStruct,
  TypeRegistry
}
import unidef.languages.javascript.{JsonSchemaCodeGen, JsonSchemaParser}
import unidef.languages.python.PythonCodeGen
import unidef.languages.sql.{SqlCodeGen, SqlParser}
import unidef.languages.yaml.YamlParser
import unidef.utils.{FileUtils, VirtualFileSystem}

import java.io.PrintWriter

@main def main(filename: String): Unit = {
  val sqlCodegen = SqlCodeGen()
  val pyCodeGen = PythonCodeGen()
  val json_schema = JsonSchemaCodeGen()
  val parser: JsonSchemaParser = JsonSchemaParser()
  parser.prepareForExtKeys(sqlCodegen)
  val yamlParser: YamlParser = YamlParser(parser)
  implicit val sqlResolver: TypeRegistry = TypeRegistry()
  val fs = new VirtualFileSystem()
  val fileContents = FileUtils.readFile(filename)
  val parsed = Array.from(if (filename.endsWith(".yaml") || filename.endsWith(".yml")) {
    yamlParser.parseFile(fileContents)
  } else if (filename.endsWith(".sql")) {
    SqlParser.parse(fileContents)(sqlResolver)
  } else if (filename.endsWith(".json")) {
    val parsed = JsonSchemaParser().parse(io.circe.parser.parse(fileContents).toTry.get)
    Seq(AstTyped(parsed))
  } else {
    throw new RuntimeException("Unsupported file type " + filename)
  })

  val parsedWriter = fs.newWriterAt("parsed.txt")
  parsedWriter.println(parsed.mkString("\n"))
  parsed
    .foreach {
      case a : AstClassDecl =>
        val code = sqlCodegen.generateTableDdl(a)
        fs.getWriterAt("AstClassDecl.txt").println(code)
      case AstTyped(struct: TyStruct) =>
        if (struct.getName.isDefined) {
          val code = sqlCodegen.generateTableDdl(struct)
          fs.getWriterAt("TyStruct.txt").println(code)
        }
        val js = json_schema.generateType(struct)
        fs.getWriterAt("JsonSchema.json").println(js.spaces2)

      case AstTyped(en: TyEnum) =>
        fs.getWriterAt("TyEnum.txt").println(en)
      case n: AstFunctionDecl =>
        val code = sqlCodegen.generateFunctionDdl(n)
        fs.getWriterAt("AstFunctionDeclSqlCodeGen.txt").println(code)

        val importManager = ImportManager()
        val js =
          json_schema.generateFuncDecl(n)
        fs.getWriterAt("JsonSchema.txt").println(js.spaces2)

    }
  fs.showAsString(new PrintWriter(System.out))
  fs.closeFiles()
}

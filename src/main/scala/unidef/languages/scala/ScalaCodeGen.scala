package unidef.languages.scala

import unidef.languages.common.{AstNode, AstRawCode, NamingConvention, TyField}
import unidef.utils.CodeGen

import scala.jdk.CollectionConverters.*
case class AstMethod(name: String, args: Seq[TyField], body: AstNode)
case class AstTrait(name: String, derive: Seq[String], methods: Seq[AstMethod])
class ScalaCodeGen(naming: NamingConvention) {
  val TEMPLATE_METHOD: String =
    """
      |def $name($args): %s = {
      |  #indent(2, $body)
      |}
    """.stripMargin
  def generateMethod(method: AstMethod): String = {
    val context = CodeGen.createContext
    context.put("name", naming.toMethodName(method.name))
    context.put(
      "args",
      method.args.map(x => x.name + ": " + ScalaCommon().encode(x)).mkString(", ")
    )
    context.put("body", method.body.asInstanceOf[AstRawCode].raw)
    CodeGen.render(TEMPLATE_METHOD, context)
  }
  val TEMPLATE_TRAIT: String =
    """
      |trait %s
      |#if($derive.nonEmpty) extends
      |#foreach($d in $derive) $d #if($foreach.hasNext) with #end
      |#end
      |{
      |#foreach($m in $methods)
      |  #indent(2, #m)
      |#end
      |}
    """.stripMargin
  def generateTrait(trt: AstTrait): String = {
    val context = CodeGen.createContext
    context.put("name", naming.toClassName(trt.name))
    context.put("derive", trt.derive.map(naming.toClassName).asJava)
    context.put("methods", trt.methods.map(generateMethod).asJava)
    CodeGen.render(TEMPLATE_TRAIT, context)
  }
}

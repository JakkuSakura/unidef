package unidef.languages.shll

import antlr4.SHLLParser.*
import unidef.common.ast.*
import antlr4.{SHLLLexer, SHLLParser}
import org.antlr.v4.runtime.tree.{ParseTree, TerminalNode}
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}

case class AntlrAstParser() {
  def convertChar(ctx: TerminalNode): AstNode = {
    val char = ctx.getText
    Asts.string(char)
  }
  def convertString(ctx: TerminalNode): AstLiteralString = {
    val string = ctx.getText
    Asts.string(string)
  }
  def convertIdentifier(ctx: TerminalNode): AstIdent = {
    val ident = ctx.getText
    AstIdentImpl(ident)
  }
  def convertInteger(ctx: TerminalNode): AstLiteralString = {
    val integer = ctx.getText
//    Asts.string(integer.toIntOption.getOrElse(throw IllegalArgumentException(s"Invalid integer at ${ctx.getSymbol}: $integer")))
    Asts.string(integer)
  }
  def convertDecimal(ctx: TerminalNode): AstLiteralString = {
    val decimal = ctx.getText
//    Asts.string(decimal.toDoubleOption.getOrElse(throw IllegalArgumentException(s"Invalid decimal at ${ctx.start}: $decimal")))
    Asts.string(decimal)
  }
  def convertApply(ctx: ApplyContext): AstApply = {
    val term = ctx.term()
    val args = ctx.pos_args()
    AstApplyImpl(convertTerm(term), null)
  }
  def convertTerm(ctx: TermContext): AstNode = {
    ctx match {
      case _ if ctx.CHAR() != null =>
        convertChar(ctx.CHAR())
      case _ if ctx.IDENT() != null =>
        convertIdentifier(ctx.IDENT())
      case _ if ctx.INTEGER() != null =>
        convertInteger(ctx.INTEGER())
      case _ if ctx.DECIMAL() != null =>
        convertDecimal(ctx.DECIMAL())
      case _ if ctx.STRING() != null =>
        convertString(ctx.STRING())
      case _ if ctx.apply() != null =>
        convertApply(ctx.apply())
    }
  }
  def parse(s: String): AstNode = {
    val lexer = SHLLLexer(CharStreams.fromString(s))
    val parser = SHLLParser(CommonTokenStream(lexer))
    val term = parser.term()
    convertTerm(term)
  }
}

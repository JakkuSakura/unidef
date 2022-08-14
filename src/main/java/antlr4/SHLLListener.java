// Generated from src/main/antlr4/SHLL.g4 by ANTLR 4.10.1
package antlr4;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SHLLParser}.
 */
public interface SHLLListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SHLLParser#term}.
	 * @param ctx the parse tree
	 */
	void enterTerm(SHLLParser.TermContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHLLParser#term}.
	 * @param ctx the parse tree
	 */
	void exitTerm(SHLLParser.TermContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHLLParser#kw_arg}.
	 * @param ctx the parse tree
	 */
	void enterKw_arg(SHLLParser.Kw_argContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHLLParser#kw_arg}.
	 * @param ctx the parse tree
	 */
	void exitKw_arg(SHLLParser.Kw_argContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHLLParser#kw_args}.
	 * @param ctx the parse tree
	 */
	void enterKw_args(SHLLParser.Kw_argsContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHLLParser#kw_args}.
	 * @param ctx the parse tree
	 */
	void exitKw_args(SHLLParser.Kw_argsContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHLLParser#pos_args}.
	 * @param ctx the parse tree
	 */
	void enterPos_args(SHLLParser.Pos_argsContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHLLParser#pos_args}.
	 * @param ctx the parse tree
	 */
	void exitPos_args(SHLLParser.Pos_argsContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHLLParser#apply}.
	 * @param ctx the parse tree
	 */
	void enterApply(SHLLParser.ApplyContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHLLParser#apply}.
	 * @param ctx the parse tree
	 */
	void exitApply(SHLLParser.ApplyContext ctx);
}
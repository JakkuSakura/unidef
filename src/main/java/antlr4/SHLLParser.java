package antlr4;// Generated from /home/jack/Dev/unidef/src/main/antlr4/SHLL.g4 by ANTLR 4.8

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class SHLLParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, IDENT=5, INTEGER=6, DECIMAL=7, STRING=8, 
		CHAR=9, WS=10;
	public static final int
		RULE_term = 0, RULE_kw_arg = 1, RULE_kw_args = 2, RULE_pos_args = 3, RULE_apply = 4;
	private static String[] makeRuleNames() {
		return new String[] {
			"term", "kw_arg", "kw_args", "pos_args", "apply"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'='", "','", "'('", "')'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, "IDENT", "INTEGER", "DECIMAL", "STRING", 
			"CHAR", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "SHLL.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public SHLLParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class TermContext extends ParserRuleContext {
		public ApplyContext apply() {
			return getRuleContext(ApplyContext.class,0);
		}
		public TerminalNode IDENT() { return getToken(SHLLParser.IDENT, 0); }
		public TerminalNode INTEGER() { return getToken(SHLLParser.INTEGER, 0); }
		public TerminalNode DECIMAL() { return getToken(SHLLParser.DECIMAL, 0); }
		public TerminalNode STRING() { return getToken(SHLLParser.STRING, 0); }
		public TerminalNode CHAR() { return getToken(SHLLParser.CHAR, 0); }
		public TermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_term; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHLLListener) ((SHLLListener)listener).enterTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHLLListener) ((SHLLListener)listener).exitTerm(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_term);
		try {
			setState(16);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(10);
				apply();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(11);
				match(IDENT);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(12);
				match(INTEGER);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(13);
				match(DECIMAL);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(14);
				match(STRING);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(15);
				match(CHAR);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Kw_argContext extends ParserRuleContext {
		public TerminalNode IDENT() { return getToken(SHLLParser.IDENT, 0); }
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public Kw_argContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_kw_arg; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHLLListener) ((SHLLListener)listener).enterKw_arg(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHLLListener) ((SHLLListener)listener).exitKw_arg(this);
		}
	}

	public final Kw_argContext kw_arg() throws RecognitionException {
		Kw_argContext _localctx = new Kw_argContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_kw_arg);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(18);
			match(IDENT);
			setState(19);
			match(T__0);
			setState(20);
			term();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Kw_argsContext extends ParserRuleContext {
		public List<Kw_argContext> kw_arg() {
			return getRuleContexts(Kw_argContext.class);
		}
		public Kw_argContext kw_arg(int i) {
			return getRuleContext(Kw_argContext.class,i);
		}
		public Kw_argsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_kw_args; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHLLListener) ((SHLLListener)listener).enterKw_args(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHLLListener) ((SHLLListener)listener).exitKw_args(this);
		}
	}

	public final Kw_argsContext kw_args() throws RecognitionException {
		Kw_argsContext _localctx = new Kw_argsContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_kw_args);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(34);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__3:
				{
				}
				break;
			case IDENT:
				{
				setState(23);
				kw_arg();
				setState(28);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
				while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(24);
						match(T__1);
						setState(25);
						kw_arg();
						}
						} 
					}
					setState(30);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
				}
				setState(32);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__1) {
					{
					setState(31);
					match(T__1);
					}
				}

				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Pos_argsContext extends ParserRuleContext {
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public Pos_argsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pos_args; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHLLListener) ((SHLLListener)listener).enterPos_args(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHLLListener) ((SHLLListener)listener).exitPos_args(this);
		}
	}

	public final Pos_argsContext pos_args() throws RecognitionException {
		Pos_argsContext _localctx = new Pos_argsContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_pos_args);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(48);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__1:
			case T__3:
				{
				}
				break;
			case IDENT:
			case INTEGER:
			case DECIMAL:
			case STRING:
			case CHAR:
				{
				setState(37);
				term();
				setState(42);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
				while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(38);
						match(T__1);
						setState(39);
						term();
						}
						} 
					}
					setState(44);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
				}
				setState(46);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
				case 1:
					{
					setState(45);
					match(T__1);
					}
					break;
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ApplyContext extends ParserRuleContext {
		public TerminalNode IDENT() { return getToken(SHLLParser.IDENT, 0); }
		public Pos_argsContext pos_args() {
			return getRuleContext(Pos_argsContext.class,0);
		}
		public Kw_argsContext kw_args() {
			return getRuleContext(Kw_argsContext.class,0);
		}
		public ApplyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_apply; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHLLListener) ((SHLLListener)listener).enterApply(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHLLListener) ((SHLLListener)listener).exitApply(this);
		}
	}

	public final ApplyContext apply() throws RecognitionException {
		ApplyContext _localctx = new ApplyContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_apply);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(50);
			match(IDENT);
			setState(51);
			match(T__2);
			setState(59);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				{
				}
				break;
			case 2:
				{
				setState(53);
				pos_args();
				}
				break;
			case 3:
				{
				setState(54);
				kw_args();
				}
				break;
			case 4:
				{
				setState(55);
				pos_args();
				setState(56);
				match(T__1);
				setState(57);
				kw_args();
				}
				break;
			}
			setState(61);
			match(T__3);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\fB\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\3\2\3\2\3\2\3\2\3\2\3\2\5\2\23\n\2\3\3\3\3"+
		"\3\3\3\3\3\4\3\4\3\4\3\4\7\4\35\n\4\f\4\16\4 \13\4\3\4\5\4#\n\4\5\4%\n"+
		"\4\3\5\3\5\3\5\3\5\7\5+\n\5\f\5\16\5.\13\5\3\5\5\5\61\n\5\5\5\63\n\5\3"+
		"\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\5\6>\n\6\3\6\3\6\3\6\2\2\7\2\4\6\b"+
		"\n\2\2\2J\2\22\3\2\2\2\4\24\3\2\2\2\6$\3\2\2\2\b\62\3\2\2\2\n\64\3\2\2"+
		"\2\f\23\5\n\6\2\r\23\7\7\2\2\16\23\7\b\2\2\17\23\7\t\2\2\20\23\7\n\2\2"+
		"\21\23\7\13\2\2\22\f\3\2\2\2\22\r\3\2\2\2\22\16\3\2\2\2\22\17\3\2\2\2"+
		"\22\20\3\2\2\2\22\21\3\2\2\2\23\3\3\2\2\2\24\25\7\7\2\2\25\26\7\3\2\2"+
		"\26\27\5\2\2\2\27\5\3\2\2\2\30%\3\2\2\2\31\36\5\4\3\2\32\33\7\4\2\2\33"+
		"\35\5\4\3\2\34\32\3\2\2\2\35 \3\2\2\2\36\34\3\2\2\2\36\37\3\2\2\2\37\""+
		"\3\2\2\2 \36\3\2\2\2!#\7\4\2\2\"!\3\2\2\2\"#\3\2\2\2#%\3\2\2\2$\30\3\2"+
		"\2\2$\31\3\2\2\2%\7\3\2\2\2&\63\3\2\2\2\',\5\2\2\2()\7\4\2\2)+\5\2\2\2"+
		"*(\3\2\2\2+.\3\2\2\2,*\3\2\2\2,-\3\2\2\2-\60\3\2\2\2.,\3\2\2\2/\61\7\4"+
		"\2\2\60/\3\2\2\2\60\61\3\2\2\2\61\63\3\2\2\2\62&\3\2\2\2\62\'\3\2\2\2"+
		"\63\t\3\2\2\2\64\65\7\7\2\2\65=\7\5\2\2\66>\3\2\2\2\67>\5\b\5\28>\5\6"+
		"\4\29:\5\b\5\2:;\7\4\2\2;<\5\6\4\2<>\3\2\2\2=\66\3\2\2\2=\67\3\2\2\2="+
		"8\3\2\2\2=9\3\2\2\2>?\3\2\2\2?@\7\6\2\2@\13\3\2\2\2\n\22\36\"$,\60\62"+
		"=";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
package antlr4;// Generated from /home/jack/Dev/unidef/src/main/antlr4/SHLL.g4 by ANTLR 4.8

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class SHLLLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, IDENT=5, INTEGER=6, DECIMAL=7, STRING=8, 
		CHAR=9, WS=10;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "IDENT", "INTEGER", "DECIMAL", "STRING", 
			"CHAR", "WS"
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


	public SHLLLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "SHLL.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\fy\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\7\6\"\n\6\f\6\16\6%\13\6\3"+
		"\7\3\7\3\7\6\7*\n\7\r\7\16\7+\3\7\3\7\3\7\6\7\61\n\7\r\7\16\7\62\3\7\3"+
		"\7\3\7\6\78\n\7\r\7\16\79\3\7\5\7=\n\7\3\7\3\7\5\7A\n\7\3\7\3\7\7\7E\n"+
		"\7\f\7\16\7H\13\7\5\7J\n\7\3\b\5\bM\n\b\3\b\6\bP\n\b\r\b\16\bQ\3\b\3\b"+
		"\6\bV\n\b\r\b\16\bW\3\t\3\t\3\t\3\t\7\t^\n\t\f\t\16\ta\13\t\3\t\3\t\3"+
		"\n\3\n\3\n\3\n\3\n\3\n\6\nk\n\n\r\n\16\nl\5\no\n\n\3\n\3\n\3\13\6\13t"+
		"\n\13\r\13\16\13u\3\13\3\13\3l\2\f\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n"+
		"\23\13\25\f\3\2\17\5\2C\\aac|\6\2\62;C\\aac|\4\2ZZzz\5\2\62;C\\c|\4\2"+
		"QQqq\3\2\629\4\2DDdd\3\2\62\63\4\2--//\3\2\63;\3\2\62;\4\2$$``\4\2\13"+
		"\f\"\"\2\u008c\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3"+
		"\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2"+
		"\3\27\3\2\2\2\5\31\3\2\2\2\7\33\3\2\2\2\t\35\3\2\2\2\13\37\3\2\2\2\rI"+
		"\3\2\2\2\17L\3\2\2\2\21Y\3\2\2\2\23d\3\2\2\2\25s\3\2\2\2\27\30\7?\2\2"+
		"\30\4\3\2\2\2\31\32\7.\2\2\32\6\3\2\2\2\33\34\7*\2\2\34\b\3\2\2\2\35\36"+
		"\7+\2\2\36\n\3\2\2\2\37#\t\2\2\2 \"\t\3\2\2! \3\2\2\2\"%\3\2\2\2#!\3\2"+
		"\2\2#$\3\2\2\2$\f\3\2\2\2%#\3\2\2\2&\'\7\62\2\2\')\t\4\2\2(*\t\5\2\2)"+
		"(\3\2\2\2*+\3\2\2\2+)\3\2\2\2+,\3\2\2\2,J\3\2\2\2-.\7\62\2\2.\60\t\6\2"+
		"\2/\61\t\7\2\2\60/\3\2\2\2\61\62\3\2\2\2\62\60\3\2\2\2\62\63\3\2\2\2\63"+
		"J\3\2\2\2\64\65\7\62\2\2\65\67\t\b\2\2\668\t\t\2\2\67\66\3\2\2\289\3\2"+
		"\2\29\67\3\2\2\29:\3\2\2\2:J\3\2\2\2;=\t\n\2\2<;\3\2\2\2<=\3\2\2\2=>\3"+
		"\2\2\2>J\7\62\2\2?A\t\n\2\2@?\3\2\2\2@A\3\2\2\2AB\3\2\2\2BF\t\13\2\2C"+
		"E\t\f\2\2DC\3\2\2\2EH\3\2\2\2FD\3\2\2\2FG\3\2\2\2GJ\3\2\2\2HF\3\2\2\2"+
		"I&\3\2\2\2I-\3\2\2\2I\64\3\2\2\2I<\3\2\2\2I@\3\2\2\2J\16\3\2\2\2KM\t\n"+
		"\2\2LK\3\2\2\2LM\3\2\2\2MO\3\2\2\2NP\t\f\2\2ON\3\2\2\2PQ\3\2\2\2QO\3\2"+
		"\2\2QR\3\2\2\2RS\3\2\2\2SU\7\60\2\2TV\t\f\2\2UT\3\2\2\2VW\3\2\2\2WU\3"+
		"\2\2\2WX\3\2\2\2X\20\3\2\2\2Y_\7$\2\2Z^\t\r\2\2[\\\7^\2\2\\^\7$\2\2]Z"+
		"\3\2\2\2][\3\2\2\2^a\3\2\2\2_]\3\2\2\2_`\3\2\2\2`b\3\2\2\2a_\3\2\2\2b"+
		"c\7$\2\2c\22\3\2\2\2dn\7)\2\2eo\t\r\2\2fg\7^\2\2go\7$\2\2hj\7^\2\2ik\13"+
		"\2\2\2ji\3\2\2\2kl\3\2\2\2lm\3\2\2\2lj\3\2\2\2mo\3\2\2\2ne\3\2\2\2nf\3"+
		"\2\2\2nh\3\2\2\2op\3\2\2\2pq\7)\2\2q\24\3\2\2\2rt\t\16\2\2sr\3\2\2\2t"+
		"u\3\2\2\2us\3\2\2\2uv\3\2\2\2vw\3\2\2\2wx\b\13\2\2x\26\3\2\2\2\23\2#+"+
		"\629<@FILQW]_lnu\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
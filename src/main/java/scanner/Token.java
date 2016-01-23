package scanner;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum Token {
	
	// Lexical
	ID("T_IDENTIFIER", "([a-zA-Z][a-zA-Z0-9_]*)", Type.LEXICAL),
//	ALPHANUM("T_ALPHANUM", "(([a-zA-Z])|([0-9])|_)", Type.LEXICAL),
//	NUM("T_NUM", "((((([1-9])([0-9])*)|0)((\\.([0-9])*([1-9]))|(\\.0)))|((([1-9])([0-9])*)|0))", Type.LEXICAL),
	INTEGER("T_INTEGER", "((([1-9])([0-9])*)|0)", Type.LEXICAL),
	FLOAT("T_FLOAT", "(((([1-9])([0-9])*)|0)((\\.([0-9])*([1-9]))|(\\.0)))", Type.LEXICAL),
//	FRACTION("T_FRACTION", "((\\.([0-9])*([1-9]))|(\\.0))", Type.LEXICAL),
//	LETTER("T_LETTER", "([a-zA-Z])", Type.LEXICAL),
//	DIGIT("T_DIGIT", "([0-9])", Type.LEXICAL),
//	NONZERO("T_NONZERO", "([1-9])", Type.LEXICAL),
	
	// Operators
	IS_EQUAL("T_IS_EQUAL", "\"\u003D\u003D\"", Type.OPERATOR),					// ==
	IS_NOT_EQUAL("T_IS_NOT_EQUAL", "\"\u003C\u003E\"", Type.OPERATOR),			// <>
	LESS_THAN("T_LESS_THAN", "\"\u003C\"", Type.OPERATOR),						// <
	GREATER_THAN("T_GREATER_THAN", "\"\u003E\"", Type.OPERATOR),				// >
	LESS_OR_EQUAL("T_LESS_OR_EQUAL", "\"\u003C\u003D\"", Type.OPERATOR),		// <=
	GREATER_OR_EQUAL("T_GREATER_OR_EQUAL", "\"\u003E\u003D\"", Type.OPERATOR),	// >=
	PLUS("T_PLUS", "\"\u002B\"", Type.OPERATOR),								// +
	MINUS("T_MINUS", "\"\u002D\"", Type.OPERATOR),								// -
	MULTIPLY("T_MULTIPLY", "\"\u002A\"", Type.OPERATOR),						// *
	DIVIDE("T_DIVIDE", "\"\u002F\"", Type.OPERATOR),							// /
	ASSIGN("T_ASSIGN", "\"\u003D\"", Type.OPERATOR),							// =
	
	// Punctuation
	SEMICOLON("T_SEMICOLON", "\"\u003B\"", Type.PUNCTUATION),					// ;
	COMMA("T_COMMA", "\"\u002C\"", Type.PUNCTUATION),							// ,
	DOT("T_DOT", "\"\u002E\"", Type.PUNCTUATION),								// .
	OPEN_PAREN("T_OPEN_PAREN", "\"\u0028\"", Type.PUNCTUATION),					// (
	CLOSE_PAREN("T_CLOSE_PAREN", "\"\u0029\"", Type.PUNCTUATION),				// )
	OPEN_CURLY("T_OPEN_CURLY", "\"\u007B\"", Type.PUNCTUATION),					// {
	CLOSE_CURLY("T_CLOSE_CURLY", "\"\u007D\"", Type.PUNCTUATION),				// }
	OPEN_SQUARE("T_OPEN_SQUARE", "\"\u005B\"", Type.PUNCTUATION),				// [
	CLOSE_SQUARE("T_CLOSE_SQUARE", "\"\u005D\"", Type.PUNCTUATION),				// ]
	START_COMMENT("T_START_COMMENT", "\"\u002F\u002A\"", Type.PUNCTUATION),		// /*
	END_COMMENT("T_END_COMMENT", "\"\u002A\u002F\"", Type.PUNCTUATION),			// */
	INLINE_COMMENT("T_INLINE_COMMENT", "\"\u002F\u002F\"", Type.PUNCTUATION),	// //
	
	// Reserved words
	AND("T_AND", "and", Type.RESERVED_WORD),
	NOT("T_NOT", "not", Type.RESERVED_WORD),
	OR("T_OR", "or", Type.RESERVED_WORD),
	IF("T_IF", "if", Type.RESERVED_WORD),
	THEN("T_THEN", "then", Type.RESERVED_WORD),
	ELSE("T_ELSE", "else", Type.RESERVED_WORD),
	FOR("T_FOR", "for", Type.RESERVED_WORD),
	CLASS("T_CLASS", "class", Type.RESERVED_WORD),
	TYPE_INT("T_TYPE_INT", "int", Type.RESERVED_WORD),
	TYPE_FLOAT("T_TYPE_FLOAT", "float", Type.RESERVED_WORD),
	GET("T_GET", "get", Type.RESERVED_WORD),
	PUT("T_PUT", "put", Type.RESERVED_WORD),
	RETURN("T_RETURN", "return", Type.RESERVED_WORD)
	;
	
	/**
	 * END OF INIT
	 */

	/**
	 * Token Type
	 */
	public enum Type {
		LEXICAL,
		OPERATOR,
		PUNCTUATION,
		RESERVED_WORD
	}
	
	// Token
	private String token;
	
	// Type
	private Type type;
	
	// Logger
	private Logger l = LogManager.getFormatterLogger(getClass());
	
	// Pattern
	private String value;
	
	Token(String token, String value, Type type) {
		this.token = token;
		this.type = type;
		this.value = value;
		l.info("%s value: %s", this.token, this.value);
	}
	
	/**
	 * Get get value
	 * @return value
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * Get token
	 * @return token
	 */
	public String getToken() {
		return this.token;
	}
	
	/**
	 * Get type
	 * @return type
	 */
	public Type getType() {
		return this.type;
	}
	
	/**
	 * Get list fo tokens by type
	 * @param type
	 * @return list of tokens of Type type
	 */
	public static List<Token> getTokensByType(Type type) {
		List<Token> tokens = new ArrayList<>();
		Token[] allTokens = values();
		for(Token token : allTokens)
			if(token.getType() == type)
				tokens.add(token);
		return tokens;
	}
	
	/**
	 * Concatenate all regex into one usin OR
	 * @return regex
	 */
	public static String getConcatAll() {
		Token[] lexicalTokens = Token.values();
		String concat = "";
		for(Token lexicalToken : lexicalTokens) {
			switch(lexicalToken.getType()) {
			case LEXICAL:
			case OPERATOR:
			case PUNCTUATION:
				concat += concat.isEmpty() ? "(" + lexicalToken.getValue() + ")" : "|(" + lexicalToken.getValue() + ")";
				break;
			default:
				break;
			}
		}
		return concat;
	}
}
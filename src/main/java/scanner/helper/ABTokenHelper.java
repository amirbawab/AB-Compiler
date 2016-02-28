package scanner.helper;

public class ABTokenHelper {
	
	// All terminals
	public static final String T_ASSIGN = "T_ASSIGN";
	public static final String T_IS_EQUAL = "T_IS_EQUAL";
	public static final String T_LESS_THAN = "T_LESS_THAN";
	public static final String T_LESS_OR_EQUAL = "T_LESS_OR_EQUAL";
	public static final String T_IS_NOT_EQUAL = "T_IS_NOT_EQUAL";
	public static final String T_GREATER_THAN = "T_GREATER_THAN";
	public static final String T_GREATER_OR_EQUAL = "T_GREATER_OR_EQUAL";
	public static final String T_SEMICOLON = "T_SEMICOLON";
	public static final String T_COMMA = "T_COMMA";
	public static final String T_DOT = "T_DOT";
	public static final String T_PLUS = "T_PLUS";
	public static final String T_MINUS = "T_MINUS";
	public static final String T_MULTIPLY = "T_MULTIPLY";
	public static final String T_DIVIDE = "T_DIVIDE";
	public static final String T_BLOCK_COMMENT = "T_BLOCK_COMMENT";
	public static final String T_OPEN_PAREN = "T_OPEN_PAREN";
	public static final String T_CLOSE_PAREN = "T_CLOSE_PAREN";
	public static final String T_OPEN_CURLY = "T_OPEN_CURLY";
	public static final String T_CLOSE_CURLY = "T_CLOSE_CURLY";
	public static final String T_OPEN_SQUARE = "T_OPEN_SQUARE";
	public static final String T_CLOSE_SQUARE = "T_CLOSE_SQUARE";
	public static final String T_IDENTIFIER = "T_IDENTIFIER";
	public static final String T_INTEGER = "T_INTEGER";
	public static final String T_FLOAT = "T_FLOAT";
	public static final String T_INLINE_COMMENT = "T_INLINE_COMMENT";
	public static final String T_IF = "T_IF";
	public static final String T_THEN = "T_THEN";
	public static final String T_ELSE = "T_ELSE";
	public static final String T_FOR = "T_FOR";
	public static final String T_CLASS = "T_CLASS";
	public static final String T_INT_TYPE = "T_INT_TYPE";
	public static final String T_FLOAT_TYPE = "T_FLOAT_TYPE";
	public static final String T_GET = "T_GET";
	public static final String T_PUT = "T_PUT";
	public static final String T_RETURN = "T_RETURN";
	public static final String T_AND = "T_AND";
	public static final String T_NOT = "T_NOT";
	public static final String T_OR = "T_OR";
	public static final String T_PROGRAM = "T_PROGRAM";
	
	// Error token prefix
	public static final String ERROR_TOKEN_PREFIX = "T_ERR_";
	
	// Error tokens
	public static final String T_ERR_LEADING_ZERO = "T_ERR_LEADING_ZERO";
	public static final String T_ERR_INVALID_CHAR = "T_ERR_INVALID_CHAR";
	public static final String T_ERR_TRAILING_ZERO = "T_ERR_TRAILING_ZERO";
	public static final String T_ERR_FLOAT_FORMAT = "T_ERR_FLOAT_FORMAT";
	public static final String T_ERR_BLOCK_COMMENT = "T_ERR_BLOCK_COMMENT";
	
	// Non terminals
	public static final String N_SEMICOLON = "semicolon";
	public static final String N_CLASSIDEN = "classIden";
	public static final String N_CLASSBODY = "classBody";
	public static final String N_ARRAYSIZEINT = "arraySizeInt";
	public static final String N_ARRAYSIZECLOSQ = "arraySizeCloSq";
}

package parser.helper;

public class ABParserMessageHelper {
	
	// Generic
	public static final String GENERIC_UNEXPECTED_TOKEN_3 = "Unexpected token '%s' at line %d column %d";
	public static final String GENERIC_UNEXPECTED_CODE_3 = "Unexpected code starting '%s' at line %d column %d";
	public static final String GENERIC_UNEXPECTED_END_OF_FILE = "Unexpected end of file";
	
	// Cell messages: %TOKEN% %ROW% %COL%
	public static final String ERR_DEFAULT = "Error '%s' at line %d column %d";
	
	// Special
	public static final String FAILURE = "Failure";
	public static final String SUCCESS = "Success";
}

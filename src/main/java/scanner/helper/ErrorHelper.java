package scanner.helper;

import java.util.HashMap;
import java.util.Map;

import scanner.helper.IdentifierHelper.ReservedWords;

public class ErrorHelper {

	// Error map
	private Map<String, String> errorComents = new HashMap<>();
	
	// Singleton
	private static ErrorHelper instance = new ErrorHelper();
	
	// Default comment
	private final static String DEFAULT_COMMENT = "Unknown error";
	
	/**
	 * Construct helper
	 */
	private ErrorHelper() {
		
		// Add reserved word to the map
		ErrorToken values[] = ErrorToken.values();
		for(ErrorToken errorToken : values)
			errorComents.put(errorToken.getToken(), errorToken.getComment());
	}
	
	public enum ErrorToken {
		INVALID_CHAR(ABTokenHelper.T_ERR_INVALID_CHAR, "Invalid character '%s' at line %d col %d"),
		FLOAT_FORMAT(ABTokenHelper.T_ERR_FLOAT_FORMAT, "Wrong float numer '%s' at line %d col %d"),
		UNCLOSED_BLOCK_COMMENT(ABTokenHelper.T_ERR_BLOCK_COMMENT, "Block comment '%s' at line %d col %d is not closed"),
		TRAILING_ZERO(ABTokenHelper.T_ERR_TRAILING_ZERO, "Float number '%s' at line %d col %d cannot end with a zero"),
		LEADING_ZERO(ABTokenHelper.T_ERR_LEADING_ZERO, "Number '%s' at line %d col %d cannot start with a zero")
		;
		
		private String token, comment;
		ErrorToken(String token, String comment) {
			this.token = token;
			this.comment = comment;
		}
		
		/**
		 * Get token
		 * @return token
		 */
		public String getToken() {
			return this.token;
		}
		
		/**
		 * Get comment
		 * @return comment
		 */
		public String getComment() {
			return this.comment;
		}
	}
	
	/**
	 * Get comment
	 * @param token
	 * @param args
	 * @return comment
	 */
	public static String getComment(String token, String value, int row, int col) {
		
		// If comment, truncate
		if(token.equals(ErrorToken.UNCLOSED_BLOCK_COMMENT.getToken()))
			value = value.length() > 15 ? value.substring(0, 14) + "..." : value;
		
		String comment = instance.errorComents.get(token);
		return comment == null ? DEFAULT_COMMENT : String.format(comment, value, row, col);
	}
}

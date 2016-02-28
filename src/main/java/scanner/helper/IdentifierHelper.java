package scanner.helper;

import java.util.HashMap;
import java.util.Map;

public class IdentifierHelper {
	
	// Reserved word map
	private Map<String, String> reservedWords = new HashMap<>();
	
	// Singleton
	private static IdentifierHelper instance = new IdentifierHelper();
	
	/**
	 * Construct helper
	 */
	private IdentifierHelper() {
		
		// Add reserved word to the map
		ReservedWords values[] = ReservedWords.values();
		for(ReservedWords reservedWord : values)
			reservedWords.put(reservedWord.getMatch(), reservedWord.getToken());
	}
	
	/**
	 * Reserved words
	 */
	public enum ReservedWords {
		IF("if", ABTokenHelper.T_IF),
		THEN("then", ABTokenHelper.T_THEN),
		ELSE("else", ABTokenHelper.T_ELSE),
		FOR("for", ABTokenHelper.T_FOR),
		CLASS("class", ABTokenHelper.T_CLASS),
		INT("int", ABTokenHelper.T_INT_TYPE),
		FLOAT("float", ABTokenHelper.T_FLOAT_TYPE),
		GET("get", ABTokenHelper.T_GET),
		PUT("put", ABTokenHelper.T_PUT),
		RETURN("return", ABTokenHelper.T_RETURN),
		AND("and", ABTokenHelper.T_AND),
		NOT("not", ABTokenHelper.T_NOT),
		OR("or", ABTokenHelper.T_OR),
		PROGRAM("program", ABTokenHelper.T_PROGRAM)
		;
		
		private String match, token;
		ReservedWords(String match, String token) {
			this.match = match;
			this.token = token;
		}
		
		/**
		 * Get match
		 * @return match
		 */
		public String getMatch() {
			return this.match;
		}
		
		/**
		 * Get token
		 * @return token
		 */
		public String getToken() {
			return this.token;
		}
	}
	
	/**
	 * Get new token if the value is a reserved word
	 * @param value
	 * @param defaultToken
	 * @return token
	 */
	public static String getTokenIfReservedWord(String value, String defaultToken) {
		String token = instance.reservedWords.get(value);
		return token == null ? defaultToken : token;
	}
}

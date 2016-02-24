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
		IF("if", "T_IF"),
		THEN("then", "T_THEN"),
		ELSE("else", "T_ELSE"),
		FOR("for", "T_FOR"),
		CLASS("class", "T_CLASS"),
		INT("int", "T_INT_TYPE"),
		FLOAT("float", "T_FLOAT_TYPE"),
		GET("get", "T_GET"),
		PUT("put", "T_PUT"),
		RETURN("return", "T_RETURN"),
		AND("and", "T_AND"),
		NOT("not", "T_NOT"),
		OR("or", "T_OR"),
		PROGRAM("program", "T_PROGRAM")
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

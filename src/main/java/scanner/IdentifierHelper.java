package scanner;

public class IdentifierHelper {
	
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
		OR("or", "T_OR")
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
		ReservedWords[] words = ReservedWords.values();
		for(int i=0; i<words.length; i++)
			if(words[i].getMatch().equals(value))
				return words[i].getToken();
		return defaultToken;
	}
}

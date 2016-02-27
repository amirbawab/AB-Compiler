package parser.helper;

import java.util.HashMap;
import java.util.Map;

public class ABParserMessageHelper {
	
	// Generic
	public static final String GENERIC_UNEXPECTED_TOKEN_3 = "Unexpected token '%s' at line %d column %d";
	public static final String GENERIC_UNEXPECTED_CODE_3 = "Unexpected code starting '%s' at line %d column %d";
	
	// Default message
	private static final String ERR_DEFAULT = "Error '%s' at line %d column %d";
	
	// Special
	public static final String FAILURE = "Failure";
	public static final String SUCCESS = "Success";
	public static final String EOF = "End Of File";
	
	// Singleton
	private static ABParserMessageHelper instance = new ABParserMessageHelper();
	
	// Hash map
	private Map<String, String> eMap;
	
	// Private constructor
	private ABParserMessageHelper(){
		
		// Init map
		eMap = new HashMap<>();
		
		// Store in map
		for(NonTerminal nonTerminal : NonTerminal.values())
			eMap.put(nonTerminal.getNonTerminal(), nonTerminal.getError());
	};
	
	public enum NonTerminal {
		SEMICOLON("semicolon", "Missing ; before '%s' at line %d column %d")
		;
		
		// Variables
		private String nonTerminal, error;
		
		private NonTerminal(String nonTerminal, String error) {
			this.nonTerminal = nonTerminal;
			this.error = error;
		}

		/**
		 * @return the nonTerminal
		 */
		public String getNonTerminal() {
			return nonTerminal;
		}

		/**
		 * @return the error
		 */
		public String getError() {
			return error;
		}
	}
	
	/**
	 * Get error message
	 * @param nonTerminal
	 * @return error message
	 */
	public static String getErrorMessage(String nonTerminal) {
		if(instance.eMap.containsKey(nonTerminal))
			return instance.eMap.get(nonTerminal);
		return ERR_DEFAULT;
	}
}

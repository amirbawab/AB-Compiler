package parser.helper;

import java.util.HashMap;
import java.util.Map;

import static scanner.helper.ABTokenHelper.*;

public class ABParserMessageHelper {
	
	// Generic
	public static final String GENERIC_UNEXPECTED_TOKEN_3 = "Unexpected value '%s' at line %d column %d";
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
		
		eMap.put(N_SEMICOLON, "Missing ; before '%s' at line %d column %d");
		eMap.put(N_CLASSIDEN, "A class name is expected instead of '%s' at line %d column %d");
		eMap.put(generateKey(N_CLASSIDEN, T_OPEN_CURLY), "Missing class name before '%s' at line %d column %d");
	};
	
	/**
	 * Get error message
	 * @param nonTerminal
	 * @return error message
	 */
	public static String getErrorMessage(String nonTerminal, String terminal) {
		
		// Generate key
		String key = instance.generateKey(nonTerminal, terminal);
		
		// Check if combination found
		if(instance.eMap.containsKey(key))
			return instance.eMap.get(key);
		
		// Check if non terminal found
		if(instance.eMap.containsKey(nonTerminal))
			return instance.eMap.get(nonTerminal);
		
		// Return default message
		return ERR_DEFAULT;
	}
	
	/**
	 * Generate a key
	 * @param nonTerminal
	 * @param terminal
	 * @return key
	 */
	private String generateKey(String nonTerminal, String terminal) {
		return String.format("%s :: %s", nonTerminal, terminal);
	}
}

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
		
		// Non terminal messages
		eMap.put(N_SEMICOLON, "Missing ; before '%s' at line %d column %d");
		eMap.put(N_CLASSIDEN, "Class name expected instead of '%s' at line %d column %d");
		eMap.put(N_CLASSBODY, "Expecting a { before '%s' at line %d column %d");
		eMap.put(N_ARRAYSIZEINT, "Expecting an integer instead of '%s' at line %d column %d");
		eMap.put(N_ARRAYSIZECLOSQ, "Missing ] before '%s' at line %d column %d");
		eMap.put(N_VARFUNCIDEN, "Expecting an identifier instead of '%s' at line %d column %d");
		eMap.put(N_OPENPAREN, "Expecting ( before '%s' at line %d column %d");
		eMap.put(N_CLOSEPAREN, "Expecting ) before '%s' at line %d column %d");
		eMap.put(N_FUNCDEFIDEN, "Expecting an identifier instead of '%s' at line %d column %d");
		eMap.put(N_FPARAMSIDEN, "Expecting an identifier instead of '%s' at line %d column %d");
		eMap.put(N_IDENTIFIER, "Expecting an identifier before '%s' at line %d column %d");
		eMap.put(N_THEN, "Expecting then before '%s' at line %d column %d");
		eMap.put(N_ELSE, "Expecting else before '%s' at line %d column %d");
		eMap.put(N_TYPE, "Expecting a type before '%s' at line %d column %d");
		eMap.put(N_RELOP, "Expecting a relational operator before '%s' at line %d column %d");
		eMap.put(N_ASSIGNOP, "Expecting = before '%s' at line %d column %d");
		eMap.put(N_CLOSESQUARE, "Expecting ] before '%s' at line %d column %d");
		eMap.put(N_SIGN, "Expecting a sign before '%s' at line %d column %d");
		
		// Combination message
		eMap.put(generateKey(N_CLASSIDEN, T_OPEN_CURLY), "Missing class name before '%s' at line %d column %d");
		eMap.put(generateKey(N_ARRAYSIZEINT, T_CLOSE_SQUARE), "Missing integer before '%s' at line %d column %d");
		eMap.put(generateKey(N_VARFUNCIDEN, T_OPEN_SQUARE), "Missing identifier before '%s' at line %d column %d");
		eMap.put(generateKey(N_VARFUNCIDEN, T_OPEN_PAREN), "Missing identifier before '%s' at line %d column %d");
		eMap.put(generateKey(N_FUNCDEFIDEN, T_OPEN_PAREN), "Missing identifier before '%s' at line %d column %d");
		eMap.put(generateKey(N_FPARAMSIDEN, T_OPEN_SQUARE), "Missing identifier before '%s' at line %d column %d");
		eMap.put(generateKey(N_FPARAMSIDEN, T_COMMA), "Missing identifier before '%s' at line %d column %d");
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

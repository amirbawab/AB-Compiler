package parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import parser.grammar.ABGrammar;

public class ABParser {
	
	// Logger
	private Logger l = LogManager.getFormatterLogger(getClass());
		
	public ABParser(String file) {
		
		// Create grammar
		ABGrammar abGrammar = new ABGrammar(file);
		
		// Create parse table
		ABParserTable abParseTable = new ABParserTable(abGrammar);
		
		// Log
		l.info("Parse table: %s", abParseTable);
	}
}

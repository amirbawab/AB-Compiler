package parser;

import parser.grammar.ABGrammar;

public class ABParser {
	
	public ABParser(String file) {
		
		// Create grammar
		ABGrammar abGrammar = new ABGrammar(file);
		
		// Create parse table
		ABParserTable abParseTable = new ABParserTable(abGrammar.getTerminals(), abGrammar.getNonTerminals(), abGrammar.getRules(), abGrammar.getFirstSetMap(), abGrammar.getFollowSetMap());
		
		System.out.println(abParseTable);
	}
}

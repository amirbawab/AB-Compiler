package parser;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import parser.grammar.ABGrammarToken;

public class ABParserTable {
	
	// Variables
	private ABParserTableCell[][] table;
	private String header[];
	private String nonTerminal[];
	private Map<String, List<List<ABGrammarToken>>> rules;
	private Map<String, Set<String>> firstSetMap, followSetMap;
	
	/**
	 * Create table
	 * @param header
	 * @param nonTerminal
	 */
	public ABParserTable(String[] header, String[] nonTerminal, Map<String, List<List<ABGrammarToken>>> rules, Map<String, Set<String>> firstSetMap, Map<String, Set<String>> followSetMap) {
		
		// Init variables
		this.header = header;
		this.nonTerminal = nonTerminal;
		this.table = new ABParserTableCell[nonTerminal.length][header.length];
		this.rules = rules;
		this.firstSetMap = firstSetMap;
		this.followSetMap = followSetMap;
		
		// Populate table
		populate();
	}
	
	/**
	 * Populate table
	 */
	private void populate() {
		
		// Rules iterator
		Iterator<Map.Entry<String, List<List<ABGrammarToken>>>> it = rules.entrySet().iterator();
	    
		// While more rules
		while (it.hasNext()) {
			
			// Cache
	        Map.Entry<String, List<List<ABGrammarToken>>> pair = it.next();
	        
	        // Loop on productions
	        for(List<ABGrammarToken> production : pair.getValue()) {
	        	
	        	// Loop on production tokens
	        	for(ABGrammarToken pToken : production) {
	        		
	        		// If production is epsilon
	        		if(pToken.isEpsilon()) {
	        			
	        			// Get follow of LHS
	        			Set<String> followSet = followSetMap.get(pair.getKey());
	        			
	        			// Loop on strings
	        			for(String terminal : followSet) {
	        				addCell(pair.getKey(), terminal, production);
	        			}
	        		
	        		// If production is not epsilon
	        		} else {
	        			
	        			// Get first set
	        			Set<String> firstSet = firstSetMap.get(pair.getKey());
	        			
	        			// Loop on strings
	        			for(String terminal : firstSet) {
	        				if(!terminal.equals(ABGrammarToken.EPSILON))
	        					addCell(pair.getKey(), terminal, production);
	        			}
	        			
	        			// If doesn't have epsilon
	        			if(!firstSet.contains(ABGrammarToken.EPSILON))
	        				break;
	        			
	        			// If last element
	        			if(production.get(production.size()-1) == pToken) {
	        				
	        				// Get follow of LHS
		        			Set<String> followSet = followSetMap.get(pair.getKey());
		        			
		        			// Loop on strings
		        			for(String terminal : followSet) {
		        				addCell(pair.getKey(), terminal, production);
		        			}
	        			}
	        		}
	        	}
	        }
		}	   
	}
	
	/**
	 * Add cell at specific row col
	 * @param nonTerminal
	 * @param terminal
	 * @param production
	 */
	public void addCell(String nonTerminal, String terminal, List<ABGrammarToken> production) {
		
		// Get index of non terminal
		for(int N=0; N<this.nonTerminal.length; N++) {
			
			// If found
			if(this.nonTerminal[N].equals(nonTerminal)) {
				
				// Get index of terminal
				for(int T=0; T<this.header.length; T++) {
					
					// Set cell
					table[N][T] = new ABParserTableCell();
					table[N][T].setProduction(production);
					
					// Exit loop
					break;
				}
				
				// Exit loop
				break;
			}
		}
		
	}
	
	/**
	 * Formatted table
	 */
	public String toString() {
		String output = "\n";
		
		for(int col = 0; col < header.length; col++)
			output += header[col] + "\t";
		
		output += "\n";
		for(int row = 0; row < nonTerminal.length; row++) {
			for(int col = 0; col < header.length; col++) {
				output += /*table[row][col] +*/ "r1\t";
			}
			output += nonTerminal[row] + "\t";
			output += "\n";
		}
		return output;
	}
	
	/**
	 * Table cell
	 */
	private class ABParserTableCell {

		// Variables
		private List<ABGrammarToken> production;
		
		/**
		 * Set production
		 * @param production
		 */
		public void setProduction(List<ABGrammarToken> production) {
			this.production = production;
		}
	}
}

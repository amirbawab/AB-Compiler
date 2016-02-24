package parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import parser.grammar.ABGrammar;
import parser.grammar.ABGrammarToken;

public class ABParserTable {
	
	// Variables
	private ABParserTableCell[][] table;
	private String terminals[];
	private String nonTerminals[];
	private Map<String, Integer> terminalIndexMap, nonTerminalIndexMap;
	private ABGrammar abGrammar;
	
	/**
	 * Create table
	 * @param header
	 * @param nonTerminal
	 */
	public ABParserTable(ABGrammar abGrammar) {
		
		// Init variables
		this.terminals = abGrammar.getTerminals();
		this.nonTerminals = abGrammar.getNonTerminals();
		this.table = new ABParserTableCell[nonTerminals.length][terminals.length];
		this.terminalIndexMap = new HashMap<>();
		this.nonTerminalIndexMap = new HashMap<>();
		this.abGrammar = abGrammar;
		
		// Cache terminal index
		for(int i=0; i<terminals.length; i++)
			terminalIndexMap.put(terminals[i], i);
		
		// Cache non-terminal index
		for(int i=0; i<nonTerminals.length; i++)
			nonTerminalIndexMap.put(nonTerminals[i], i);
			
		// Populate table
		populate();
	}
	
	/**
	 * Populate table
	 */
	private void populate() {
		
		// Rules iterator
		Iterator<Map.Entry<String, List<List<ABGrammarToken>>>> it = abGrammar.getRules().entrySet().iterator();
	    
		// While more rules
		while (it.hasNext()) {
			
			// Cache
	        Map.Entry<String, List<List<ABGrammarToken>>> pair = it.next();
	        
	        // Non terminal
	        String nonTerminal = pair.getKey();
	        
	        // Loop on productions
	        for(List<ABGrammarToken> production : pair.getValue()) {
	        	
	        	// Prepare hash set
        		Set<String> terminalsSet = new HashSet<>();
	        	
        		// Set i
        		int i = 0;
        		
        		// Loop on production tokens
	        	for(; i < production.size(); ++i) {
	        		
	        		// Current
	        		ABGrammarToken pToken = production.get(i);
	        		
	        		// Get first set
	        		Set<String> firstSet = abGrammar.getFirstOf(pToken);
	        		
	        		// Copy into terminal set
    				for(String str : firstSet)
    					if(!str.equals(ABGrammarToken.EPSILON))
    						terminalsSet.add(str);
	        		
	        		// If doesn't have epsilon
	        		if(!firstSet.contains(ABGrammarToken.EPSILON))
	        			break;
	        	}
	        	
	        	// If the first(last token) has epsilon
	        	if(i == production.size()) {
	        		Set<String> followSet = abGrammar.getFollowOf(pair.getKey());
	        		terminalsSet.addAll(followSet);
	        	}
	        	
	        	// Add cells
	        	for(String terminal : terminalsSet)
	        		table[nonTerminalIndexMap.get(nonTerminal)][terminalIndexMap.get(terminal)] = new ABParserTableCell(production);
	        }
		}	   
	}
	
	/**
	 * Formatted table
	 */
	public String toString() {
		
		// Map
		Map<List<ABGrammarToken>, Integer> pMap = new HashMap<>();
		
		// Counter
		int rCounter = 0;
		
		// Prepare output
		String output = "\n";
		String rules = "";
		
		for(int col = 0; col < terminals.length; col++)
			output += terminals[col] + "\t";
		
		output += "\n";
		for(int row = 0; row < nonTerminals.length; row++) {
			for(int col = 0; col < terminals.length; col++) {
				
				// If no error
				if(table[row][col] != null) {
					
					String rId = "r";
					
					// If already registered
					if(pMap.containsKey(table[row][col].getProduction())) {
						rId += pMap.get(table[row][col].getProduction());
					
					// If not already registered
					} else {
						pMap.put(table[row][col].getProduction(), ++rCounter);
						rId += rCounter;
						rules += String.format("%s : %s -> %s\n", rId, nonTerminals[row], table[row][col].getProduction());
					}
						
					output += rId + "\t";
					
				// If error
				} else {
					output += "e\t";
				}
				
				
			}
			output += nonTerminals[row] + "\t";
			output += "\n";
		}
		
		output += "\n";
		output += rules;
		return output;
	}
	
	/**
	 * Table cell
	 */
	private class ABParserTableCell {

		// Variables
		private List<ABGrammarToken> production;
		
		/**
		 * Constructor
		 * @param production
		 */
		public ABParserTableCell(List<ABGrammarToken> production) {
			setProduction(production);
		}
		
		/**
		 * Set production
		 * @param production
		 */
		public void setProduction(List<ABGrammarToken> production) {
			this.production = production;
		}
		
		/**
		 * Get production
		 * @return production
		 */
		public List<ABGrammarToken> getProduction() {
			return this.production;
		}
	}
}

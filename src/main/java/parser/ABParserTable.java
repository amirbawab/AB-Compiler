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
	private Map<List<ABGrammarToken>, Integer> rMap;
	private Map<String, Integer> eMap;
	
	
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
		this.rMap = new HashMap<>();
		this.eMap = new HashMap<>();
		
		// Cache terminal index
		for(int i=0; i<terminals.length; i++)
			terminalIndexMap.put(terminals[i], i);
		
		// Cache non-terminal index
		for(int i=0; i<nonTerminals.length; i++)
			nonTerminalIndexMap.put(nonTerminals[i], i);
			
		// Populate table
		populate();
		
		// Apply id
		applyREId();
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
		
		// Put error message for all the remaining cells
		for(int row=0; row < nonTerminals.length; row++) {
			for(int col=0; col < terminals.length; col++) {
				if(table[row][col] == null)
					table[row][col] = new ABParserTableCell("Error found!");
			}
		}
	}
	
	/**
	 * Get table cell at [nonterminal] [terminal]
	 * @param nonTerminal
	 * @param terminal
	 * @return Table cell
	 */
	public ABParserTableCell getTableAt(String nonTerminal, String terminal) {
		return table[nonTerminalIndexMap.get(nonTerminal)][terminalIndexMap.get(terminal)];
	}
	
	/**
	 * Apply 'r' 'e' id
	 */
	public void applyREId() {
		
		// Counter
		int rCounter = 0;
		int eCounter = 0;
		
		// Loop on all cells
		for(int row = 0; row < nonTerminals.length; row++) {
			for(int col = 0; col < terminals.length; col++) {
				
				// If no error
				if(!table[row][col].isError()) {
					
					// If already registered
					if(rMap.containsKey(table[row][col].getProduction())) {
						int id = rMap.get(table[row][col].getProduction());
						table[row][col].setId("r" + id);
						
					// If not already registered
					} else {
						rMap.put(table[row][col].getProduction(), ++rCounter);
						table[row][col].setId("r" + rCounter);
					}
						
				// If error
				} else {
					
					// If already registered
					if(eMap.containsKey(table[row][col].getErrorMessage())) {
						int id = eMap.get(table[row][col].getErrorMessage());
						table[row][col].setId("e" + id);
					
					// If not already registered
					} else {
						eMap.put(table[row][col].getErrorMessage(), ++eCounter);
						table[row][col].setId("e" + eCounter);
					}
				}
			}
		}
	}
	
	/**
	 * Formatted table
	 */
	public String toString() {
		
		// Prepare output
		String output = "\n";
		String rules = "";
		String errors = "";
		
		// Sets
		Set<String> rSet = new HashSet<>();
		Set<String> eSet = new HashSet<>();
		
		for(int col = 0; col < terminals.length; col++)
			output += terminals[col] + "\t";
		
		output += "\n";
		for(int row = 0; row < nonTerminals.length; row++) {
			for(int col = 0; col < terminals.length; col++) {
				
				String id = table[row][col].getId();
				output += id + "\t";
				
				// If no error
				if(!table[row][col].isError()) {
					
					// If not already added
					if(!rSet.contains(id)){
						rSet.add(id);
						rules += String.format("%s : %s -> %s\n", id, nonTerminals[row], table[row][col].getProduction());
					}
						
				// If error
				} else {
					
					// If not already added
					if(!eSet.contains(id)){
						eSet.add(id);
						errors += String.format("%s : %s\n", id, table[row][col].getErrorMessage());
					}
				}
			}
			output += nonTerminals[row] + "\t";
			output += "\n";
		}

		output += "\nRULES:\n";
		output += rules;
		output += "\nERRORS:\n";
		output += errors;
		return output;
	}
	
	/**
	 * Table cell
	 */
	public class ABParserTableCell {

		// Variables
		private List<ABGrammarToken> production;
		private String errorMessage;
		private boolean isError = false;
		private String id;
		
		/**
		 * Constructor
		 * @param production
		 */
		public ABParserTableCell(List<ABGrammarToken> production) {
			setProduction(production);
		}

		/**
		 * Constructor
		 * @param production
		 */
		public ABParserTableCell(String errorMessage) {
			this.errorMessage = errorMessage;
			this.isError = true;
			setProduction(null);
		}
		
		/**
		 * Set production
		 * @param production
		 */
		public void setProduction(List<ABGrammarToken> production) {
			this.production = production;
		}
		
		/**
		 * Check if is an error
		 * @return true if error
		 */
		public boolean isError() {
			return this.isError;
		}
		
		/**
		 * Get error message
		 * @return error message
		 */
		public String getErrorMessage() {
			return this.errorMessage;
		}
		
		/**
		 * Get production
		 * @return production
		 */
		public List<ABGrammarToken> getProduction() {
			return this.production;
		}
		
		/**
		 * Get id
		 * @return id
		 */
		public String getId() {
			return this.id;
		}
		
		/**
		 * Set id
		 * @param id
		 */
		public void setId(String id) {
			this.id = id;
		}
		
		/**
		 * To String
		 * @return formatted value
		 */
		public String toString() {
			if(isError())
				return "e";
			else
				return production.toString();
		}
	}
}

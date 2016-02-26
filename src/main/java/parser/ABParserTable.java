package parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import parser.grammar.ABGrammar;
import parser.grammar.ABGrammarToken;

public class ABParserTable {
	
	// Logger
	private Logger l = LogManager.getFormatterLogger(getClass());
		
	// Variables
	private ABParserTableCell[][] table;
	private String terminals[];
	private String nonTerminals[];
	private Map<String, Integer> terminalIndexMap, nonTerminalIndexMap;
	private LinkedHashMap<List<ABGrammarToken>, ABParserTableRule> rMap;
	private LinkedHashMap<String, ABParserTableError> eMap;
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
		this.rMap = new LinkedHashMap<>();
		this.eMap = new LinkedHashMap<>();
		
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
	        	for(String terminal : terminalsSet){
	        		
	        		// Log
	        		if(!nonTerminalIndexMap.containsKey(nonTerminal))
	        			l.error("Non terminal '%s' not found in table", nonTerminal);
	        		
	        		// Log
	        		if(!terminalIndexMap.containsKey(terminal))
	        			l.error("Terminal '%s' not found in table", terminal);
	        		
	        		table[nonTerminalIndexMap.get(nonTerminal)][terminalIndexMap.get(terminal)] = new ABParserTableCell(production);
	        	}
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
		
		// Log
		if(!nonTerminalIndexMap.containsKey(nonTerminal))
			l.error("Non terminal '%s' not found in table", nonTerminal);
		
		// Log
		if(!terminalIndexMap.containsKey(terminal))
			l.error("Terminal '%s' not found in table", terminal);
		
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
						ABParserTableRule rule = rMap.get(table[row][col].getProduction());
						table[row][col].setId(rule.getId());
						
					// If not already registered
					} else {
						rMap.put(table[row][col].getProduction(), new ABParserTableRule(String.format("r%d", ++rCounter), nonTerminals[row], table[row][col].getProduction()));
					}
						
				// If error
				} else {
					
					// If already registered
					if(eMap.containsKey(table[row][col].getErrorMessage())) {
						ABParserTableError error = eMap.get(table[row][col].getErrorMessage());
						table[row][col].setId(error.getId());
					
					// If not already registered
					} else {
						eMap.put(table[row][col].getErrorMessage(), new ABParserTableError(String.format("e%d", ++eCounter), table[row][col].getErrorMessage()));
					}
				}
			}
		}
	}
	
	/**
	 * Get rules
	 * @return rules
	 */
	public ABParserTableRule[] getRules() {
		ABParserTableRule[] rules = new ABParserTableRule[rMap.size()];
		rMap.values().toArray(rules);
		return rules;
	}
	
	/**
	 * Get errors
	 * @return errors
	 */
	public ABParserTableError[] getErrors() {
		ABParserTableError[] errors = new ABParserTableError[eMap.size()];
		rMap.values().toArray(errors);
		return errors;
	}
	
	/**
	 * Formatted table
	 */
	public String toString() {
		
		// Prepare output
		String output = "\n";
		
		for(int col = 0; col < terminals.length; col++)
			output += terminals[col] + "\t";
		
		output += "\n";
		for(int row = 0; row < nonTerminals.length; row++) {
			for(int col = 0; col < terminals.length; col++) {
				String id = table[row][col].getId();
				output += id + "\t";
			}
			output += nonTerminals[row] + "\t";
			output += "\n";
		}

		output += "\nRULES:\n";
		
		// Add rules to output
		for(ABParserTableRule rule : rMap.values())
			output += String.format("%s: %s -> %s\n", rule.getId(), rule.getLHS(), StringUtils.join(rule.getProduction(), " "));
		
		output += "\nERRORS:\n";
		
		// Add errors to output
		for(ABParserTableError error : eMap.values())
			output += String.format("%s: %s\n", error.getId(), error.getMessage());
		
		return output;
	}
	
	/**
	 * Parser table rule
	 */
	public class ABParserTableRule {
		
		// Variables
		private String id;
		private String LHS;
		private List<ABGrammarToken> production;
		
		/**
		 * @param id
		 * @param production
		 */
		public ABParserTableRule(String id, String LHS, List<ABGrammarToken> production) {
			this.id = id;
			this.LHS = LHS;
			this.production = production;
		}

		/**
		 * @return the id
		 */
		public String getId() {
			return id;
		}

		/**
		 * @return LHS
		 */
		public String getLHS() {
			return this.LHS;
		}
		
		/**
		 * @return the production
		 */
		public List<ABGrammarToken> getProduction() {
			return production;
		}
	}
	
	/**
	 * Parser table rule
	 */
	public class ABParserTableError {
		
		// Variables
		private String id;
		private String message;
		
		/**
		 * @param id
		 * @param message
		 */
		public ABParserTableError(String id, String message) {
			this.id = id;
			this.message = message;
		}

		/**
		 * @return the id
		 */
		public String getId() {
			return id;
		}

		/**
		 * @return the message
		 */
		public String getMessage() {
			return message;
		}
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

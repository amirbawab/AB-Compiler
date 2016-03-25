package parser;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import parser.grammar.ABGrammar;
import parser.grammar.ABGrammarToken;
import parser.helper.ABParserMessageHelper;

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
		this.terminals = abGrammar.getTerminalsAsArray();
		this.nonTerminals = abGrammar.getNonTerminalsAsArray();
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

					// If epsilon, terminal or non terminal
					if(pToken.isEpsilon() || pToken.isNonTerminal() || pToken.isTerminal()) {

						// Get first set
						Set<String> firstSet = abGrammar.getFirstOf(pToken);

						// Copy into terminal set
						for (String str : firstSet)
							if (!str.equals(ABGrammarToken.EPSILON))
								terminalsSet.add(str);

						// If doesn't have epsilon
						if (!firstSet.contains(ABGrammarToken.EPSILON))
							break;
					}
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
					table[row][col] = new ABParserTableCell(nonTerminals[row], terminals[col], ABParserMessageHelper.getErrorMessage(nonTerminals[row], terminals[col]));
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
						String id = String.format("r%d", ++rCounter);
						rMap.put(table[row][col].getProduction(), new ABParserTableRule(id, nonTerminals[row], table[row][col].getProduction()));
						table[row][col].setId(id);
					}
						
				// If error
				} else {
					
					// If already registered
					if(eMap.containsKey(table[row][col].getErrorMessage())) {
						ABParserTableError error = eMap.get(table[row][col].getErrorMessage());
						table[row][col].setId(error.getId());
					
					// If not already registered
					} else {
						String id = String.format("e%d", ++eCounter);
						eMap.put(table[row][col].getErrorMessage(), new ABParserTableError(id, table[row][col].getErrorMessage()));
						table[row][col].setId(id);
					}
				}
			}
		}
	}
	
	/**
	 * Get rules
	 * @return rules
	 */
	public Collection<ABParserTableRule> getRules() {
		return rMap.values();
	}
	
	/**
	 * Get errors
	 * @return errors
	 */
	public Collection<ABParserTableError> getErrors() {
		return eMap.values();
	}
	
	/**
	 * Get index of terminal
	 * @param terminal
	 * @return index
	 */
	public int getIndexOfTerminal(String terminal) {
		return terminalIndexMap.get(terminal);
	}
	
	/**
	 * Get index of non terminal
	 * @param non terminal
	 * @return index
	 */
	public int getIndexOfNonTerminal(String nonTerminal) {
		return nonTerminalIndexMap.get(nonTerminal);
	}
	
	/**
	 * Get table
	 * @return table
	 */
	public ABParserTableCell[][] getTable() {
		return this.table;
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
				output += id;
				
				// If error
				if(table[row][col].isError())
					output += " " + table[row][col].getErrorDecision();
					
				output += "\t";
			}
			output += nonTerminals[row];
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
		private List<ABGrammarToken> productionWithAction;
		private String errorMessage;
		private boolean isError;
		private String errorDecision;
		private String id;
		
		// Constants
		public static final String SCAN = "S";
		public static final String POP = "P";
		
		/**
		 * Constructor
		 * @param production
		 */
		public ABParserTableCell(List<ABGrammarToken> production) {
			this.isError = false;
			this.productionWithAction = production;
			this.production = new ArrayList<>(production.size());

			// Do not include actions
			for(ABGrammarToken token : production)
				if(token.isEpsilon() || token.isNonTerminal() || token.isTerminal())
					this.production.add(token);
		}

		/**
		 * Constructor
		 * @param production
		 */
		public ABParserTableCell(String nonTerminal, String terminal, String errorMessage) {
			this.errorMessage = errorMessage;
			this.isError = true;
			this.production = null;
			this.productionWithAction = null;
			
			// Get follow set of non terminal
			Set<String> followSet = abGrammar.getFollowOf(nonTerminal);
			
			// If terminal is in the follow set, then it's a pop
			if(terminal.equals(ABGrammarToken.END_OF_STACK) || followSet.contains(terminal))
				errorDecision = POP;
			else
				errorDecision = SCAN;
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
		 * Note: Use only if the cell is an error cell
		 * @return error message
		 */
		public String getErrorMessage() {
			return this.errorMessage;
		}
		
		/**
		 * Get production
		 * Note: Use only if the cell is not an error cell
		 * @return production
		 */
		public List<ABGrammarToken> getProduction() {
			return this.production;
		}

		/**
		 * Get production with the action token
		 * @return production including action tokens
         */
		public List<ABGrammarToken> getProductionWithAction() {
			return productionWithAction;
		}

		/**
		 * Get id
		 * @return id
		 */
		public String getId() {
			return this.id;
		}
		
		/**
		 * Get error decision
		 * Note: Use only if the cell is an error cell
		 * @return POP | SCAN | null
		 */
		public String getErrorDecision() {
			return this.errorDecision;
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

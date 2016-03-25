package parser.grammar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ABGrammar {
	
	// Logger
	private Logger l = LogManager.getFormatterLogger(getClass());
	
	// Variables
	private Map<String, List<List<ABGrammarToken>>> rules;
	private Map<String, Set<String>> firstSetMap, followSetMap;
	private String start;
	private Set<String> terminals;
			
	
	/**
	 * Create grammar from file
	 * @param file
	 */
	public ABGrammar(String file) {
		
		try {
			
			// Init variables
			rules = new HashMap<>();
			firstSetMap = new HashMap<>();
			followSetMap = new HashMap<>();
			terminals = new HashSet<>();
			start = null;
			
			// Add $ to terminal
			terminals.add(ABGrammarToken.END_OF_STACK);
			
			// Parse file
			parse(file);
			
			// Computer first
			computeFirst();
			l.info("First sets: %s", firstSetMap.toString());
			
			// Computer follow
			computeFollow();
			l.info("Follow sets: %s", followSetMap.toString());
			
		} catch (IOException e) {
			l.error(e.getMessage());
		}
	}
	
	/**
	 * Get terminals as array
	 * @return terminals array
	 */
	public String[] getTerminalsAsArray() {
		
		// Prepare array
		String[] array = new String[terminals.size()];
		
		// Copy to array
		terminals.toArray(array);
		
		// Return array
		return array;
	}
	
	/**
	 * Get terminals as array
	 * @return terminals array
	 */
	public Set<String> getTerminals() {
		
		// Return array
		return terminals;
	}
	
	/**
	 * Get rules
	 * @return rules
	 */
	public Map<String, List<List<ABGrammarToken>>> getRules() {
		return this.rules;
	}
	
	/**
	 * Get first set map
	 * @return first set map
	 */
	public Map<String, Set<String>> getFirstSetMap() {
		return this.firstSetMap;
	}
	
	/**
	 * Get follow set map
	 * @return follow set map
	 */
	public Map<String, Set<String>> getFollowSetMap() {
		return this.followSetMap;
	}
	
	/**
	 * Get non terminals
	 * @return non terminals
	 */
	public Set<String> getNonTerminals() {
		
		// Prepare list
		return rules.keySet();
	}
	
	/**
	 * Get non terminals as array
	 * @return non terminals array
	 */
	public String[] getNonTerminalsAsArray() {
		
		// Prepare list
		Set<String> nonTerminals = rules.keySet();
		
		// Prepare array
		String[] array = new String[nonTerminals.size()];
		
		// Copy to array
		nonTerminals.toArray(array);
		
		// Return array
		return array;
	}
	
	/**
	 * Compute the First set of all Non-Terminals
	 */
	private void computeFirst() {
		
		// Rules iterator
		Iterator<Map.Entry<String, List<List<ABGrammarToken>>>> it = rules.entrySet().iterator();
	    
		// While more rules
		while (it.hasNext()) {
			
			// Cache
	        Map.Entry<String, List<List<ABGrammarToken>>> pair = it.next();
	        
	        // Compute first
	        first(new ABGrammarToken(pair.getKey()));
	    }
	}
	
	/**
	 * Computer first set for one element and cache it
	 * @param token
	 * @return First set
	 */
	private Set<String> first(ABGrammarToken token) {
		
		// Optimize
		if(token.isNonTerminal() && firstSetMap.containsKey(token.getValue()))
			return firstSetMap.get(token.getValue());
		
		// Prepare set
		Set<String> firstSet = new HashSet<>();
		
		// If token is terminal or epsilon
		if(token.isTerminal() || token.isEpsilon()) {
			firstSet.add(token.getValue());
		
		// If token is non terminal
		} else {
			
			// Get RHS
	        List<List<ABGrammarToken>> RHS = rules.get(token.getValue());
	        
	        // Loop on all productions
	        for(List<ABGrammarToken> production : RHS) {
	        	
	        	// Loop on production tokens
	        	for(ABGrammarToken pToken : production) {

					// If epsilon, non terminal or terminal
					if(pToken.isEpsilon() || pToken.isNonTerminal() || pToken.isTerminal()) {

						// Get first of pToken
						Set<String> pFirstSet = first(pToken);

						// If doesn't have epsilon, or last token in the production
						if(!pFirstSet.contains(ABGrammarToken.EPSILON) || pToken == production.get(production.size()-1)) {

							// Superset
							firstSet.addAll(pFirstSet);

							// Don't try next token
							break;

							// If has epsilon
						} else {

							// Superset minus epsilon
							for(String str : pFirstSet)
								if(!str.equals(ABGrammarToken.EPSILON))
									firstSet.add(str);
						}
					}
	        	}
	        }
	        
	        // Cache first set
			firstSetMap.put(token.getValue(), firstSet);
		}
		
		// Get first set
		return firstSet;
	}
	
	/**
	 * Compute the Follow set of all Non-Terminals
	 */
	private void computeFollow() {
		
		// Loop on non terminals and init empty sets
		for(String nonTerminal : rules.keySet())
			followSetMap.put(nonTerminal, new HashSet<String>());
		
		// Add $ to start token
		followSetMap.get(start).add(ABGrammarToken.END_OF_STACK);
		
		// Apply the follow multiple times
		for (List<List<ABGrammarToken>> productions : rules.values())
			for(int i=0; i<productions.size(); i++)
				follow();
	}
	
	/**
	 * Computer follow set for all elements and cache them
	 */
	private void follow() {

		// Rules iterator
		Iterator<Map.Entry<String, List<List<ABGrammarToken>>>> it = rules.entrySet().iterator();
			   
		// While more rules
		while (it.hasNext()) {
			
			// Cache
	        Map.Entry<String, List<List<ABGrammarToken>>> pair = it.next();
	        
	        // Loop on productions for the same key
	        for(List<ABGrammarToken> production : pair.getValue()) {
	        	
	        	// Loop on production tokens
	        	for(int i = 0; i < production.size(); ++i) {
	        		
	        		// Current
	        		ABGrammarToken pToken = production.get(i);
	        		
	        		// If non terminal
	        		if(pToken.isNonTerminal()) {
	        			
		    			// J
	        			int j = i;
	        			
	        			// While more tokens
	        			while(++j < production.size()) {
	        				
	        				// Get next token
	        				ABGrammarToken nextToken = production.get(j);
	    	        		
	        				// Get the first set
	        				Set<String> firstSet = first(nextToken);
	        				
	        				// Copy into follow set
	        				for(String str : firstSet)
	        					if(!str.equals(ABGrammarToken.EPSILON))
	        						followSetMap.get(pToken.getValue()).add(str);
	        				
	        				// If first set doesn't contain epsilon stop
	        				if(!firstSet.contains(ABGrammarToken.EPSILON))
	        					break;
	        			}
	        			
	        			// If no more next tokens
	        			if(j == production.size()) {
	        				
        					// Include follow set of LHS into target
	        				followSetMap.get(pToken.getValue()).addAll(followSetMap.get(pair.getKey()));
	        			}
	        		}
	        	}
	        }
	    }
	}
	
	/**
	 * Get first of a token
	 * @param token
	 * @return first set 
	 */
	public Set<String> getFirstOf(ABGrammarToken token) {
		return first(token);
	}
	
	/**
	 * Get first of a token
	 * @param token Terminal should be inside single quote
	 * @return first set
	 */
	public Set<String> getFirstOf(String token) {
		return first(new ABGrammarToken(token));
	}
	
	/**
	 * Get follow of a non terminal
	 * @param nonTerminal
	 * @return follow set of a non terminal
	 */
	public Set<String> getFollowOf(String nonTerminal) {
		return this.followSetMap.get(nonTerminal);
	}
	
	/**
	 * Get starting non terminal
	 * @return starting non terminal
	 */
	public String getStart() {
		return this.start;
	}
	
	/**
	 * Parse grammar file
	 * @param file
	 * @throws IOException
	 */
	private void parse(String file) throws IOException {
		// Scan file
		Scanner scanGrammar = new Scanner(this.getClass().getResource(file).openStream());
		
		// LHS
		String LHS = null;
		
		// While more lines to scan
		while(scanGrammar.hasNext()) {
			
			// Scan line
			String line = scanGrammar.nextLine();
			
			// Scan line
			Scanner scanLine = new Scanner(line);
			
			// Production
			List<ABGrammarToken> production = new ArrayList<>();
			
			// If line is not empty
			if(scanLine.hasNext()) {
				
				// First
				String first = scanLine.next();
				
				// If |
				if(first.equals("|")) {
					// Nothing
				
				// If word
				} else if(first.startsWith("%")) {
					
					// Skip line because it's a comment
					continue;
					
				} else {
					
					// Update LHS
					LHS = first;
					
					// Start
					if(start == null)
						start = LHS;
					
					// If first time create list
					if(rules.get(LHS) == null)
						rules.put(LHS, new LinkedList<List<ABGrammarToken>>());
					
					// If next chars are not ->
					if(!scanLine.next().equals("->")){
						scanLine.close();
						throw new IOException("Wrong file format! Expecting ->");
					}
				}
			}
			
			// While more words
			while(scanLine.hasNext()) {
				
				// Current word
				String current = scanLine.next();
				
				// If or
				if(current.equals("|")) {
					
					// Add to rule
					rules.get(LHS).add(production);
					
					// Create new array
					production = new ArrayList<>();
					
				} else {
					ABGrammarToken token = new ABGrammarToken(current);
					production.add(token);
					
					// If terminal, add it to list of terminals
					if(token.isTerminal())
						terminals.add(token.getValue());
				}
			}
			
			// Close scanner
			scanLine.close();
			
			// Add to rule
			if(LHS != null && production.size() > 0)
				rules.get(LHS).add(production);
		}
		
		// Close grammar scanner
		scanGrammar.close();
	}
}

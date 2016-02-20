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
			
			// Parse file
			parse(file);
			
			// Computer first
			computeFirst();
			
			for(String s : firstSetMap.get("fParamsTailRpt"))
				System.out.println(s);
			
			// Computer follow
			
		} catch (IOException e) {
			l.error(e.getMessage());
		}
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
	        
	        // Move to next
	        it.remove();
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
	        
	        // Cache first set
			firstSetMap.put(token.getValue(), firstSet);
		}
		
		// Get first set
		return firstSet;
	}
	
	/**
	 * Get first of a non terminal
	 * @param token
	 * @return First set
	 */
	public Set<String> getFirst(String token) {
		return this.firstSetMap.get(token);
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
				} else {
					
					// Update LHS
					LHS = first;
					
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
					production.add(new ABGrammarToken(current));
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

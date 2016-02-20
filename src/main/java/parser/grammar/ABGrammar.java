package parser.grammar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ABGrammar {
	
	// Logger
	private Logger l = LogManager.getFormatterLogger(getClass());
	
	// Variables
	private HashMap<String, List<List<ABGrammarToken>>> rules;
	
	@SuppressWarnings("resource")
	public ABGrammar(String file) {
		try {
			
			// Scan file
			Scanner scanGrammar = new Scanner(this.getClass().getResource(file).openStream());
			
			// Init rules
			rules = new HashMap<>();
			
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
						if(!scanLine.next().equals("->")) 
							throw new RuntimeException("Wrong file format! Expecting ->");
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
				rules.get(LHS).add(production);
			}
			
			// Close grammar scanner
			scanGrammar.close();
			
		} catch (IOException e) {
			l.error(e.getMessage());
		}
		
		System.out.println(rules);
	}
}

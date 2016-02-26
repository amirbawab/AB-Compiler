package parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import parser.grammar.ABGrammar;
import parser.grammar.ABGrammarToken;
import scanner.ABScanner;
import scanner.ABToken;

public class ABParser {
	
	// Logger
	private Logger l = LogManager.getFormatterLogger(getClass());
		
	// Variables
	private ABParserTable abParseTable;
	private ABGrammar abGrammar;
	
	// Store Snapshots
	private List<ABParserSnapshot> snapshots;
	
	/**
	 * Constructor
	 * @param file
	 */
	public ABParser(String file) {
		
		// Create grammar
		this.abGrammar = new ABGrammar(file);
		
		// Create parse table
		this.abParseTable = new ABParserTable(abGrammar);
		
		// Log
		l.info("Parse table: %s", abParseTable);
	}
	
	/**
	 * Parse input
	 * @param tokens
	 * @return true if parse was successful, otherwise false
	 */
	public boolean parse(ABToken[] scannerTokens) {
		
		// Add $ to end of tokens
		ArrayList<ABToken> tokens = new ArrayList<>();
		for(int i=0; i < scannerTokens.length; i++) {
			
			// If token should not be excluded, add it
			if(!ABScanner.EXCLUDE_PARSER.contains(scannerTokens[i].getToken()))
				tokens.add(scannerTokens[i]);
		}
		
		tokens.add(new ABToken(ABGrammarToken.END_OF_STACK, ABGrammarToken.END_OF_STACK, -1, -1));
		
		// Prepare derivation list
		List<ABGrammarToken> derivation = new ArrayList<>();
		
		// Prepare stack
		Stack<ABGrammarToken> stack = new Stack<>();
		
		// Reset the snapshot list
		snapshots = new ArrayList<>();
		
		// $
		stack.push(new ABGrammarToken(ABGrammarToken.END_OF_STACK));
		
		// S
		stack.push(new ABGrammarToken(abGrammar.getStart()));
		
		// Add S to derivation
		derivation.add(stack.peek());
		
		// Error
		boolean error = false;
		
		// Counter
		int inputTokenIndex = 0;
		
		// Step
		int step = 0;
		
		// Get next token
		ABToken inputToken = tokens.get(inputTokenIndex);
		
		// Log
		l.info("Start parsing input ...");
				
		// Take snapshot
		snapshots.add(new ABParserSnapshot(++step, StringUtils.join(stack, " "), tokensStartAt(tokens, inputTokenIndex), "", StringUtils.join(derivation, " ")));
		
		// While top is not $
		while(!stack.peek().isEndOfStack()) {
			
			// Store top
			ABGrammarToken grammarToken = stack.peek();
			
			// If is terminal
			if(grammarToken.isTerminal()) {
				
				// If match
				if(grammarToken.getValue().equals(inputToken.getToken())) {
					
					// Take snapshot
					snapshots.add(new ABParserSnapshot(++step, StringUtils.join(stack, " "), tokensStartAt(tokens, inputTokenIndex), "", ""));
					
					// Pop terminal from stack
					stack.pop();
					
					// Update inputToken
					inputToken = tokens.get(++inputTokenIndex);
					
				// If no match
				} else {
					// TODO error found
					error = true;
				}
			
			// If is non terminal
			} else {
				
				// Get cell
				ABParserTable.ABParserTableCell cell = abParseTable.getTableAt(grammarToken.getValue(), inputToken.getToken());
				
				// If not an error
				if(!cell.isError()) {
					
					// Store production
					List<ABGrammarToken> production = cell.getProduction();
					
					// Adjust the derivation
					derive(grammarToken, production, derivation);
					
					// Take snapshot
					snapshots.add(new ABParserSnapshot(++step, StringUtils.join(stack, " "), tokensStartAt(tokens, inputTokenIndex), String.format("%s:%s->%s", cell.getId(), grammarToken.getValue(), StringUtils.join(production, " ")), String.format("=>%s", StringUtils.join(derivation, " "))));
					
					// Pop
					stack.pop();
					
					// Inverse RHS multiple push. Don't push EPSILON
					for(int pTokenId = production.size()-1; pTokenId >= 0; --pTokenId) {
						if(!production.get(pTokenId).isEpsilon())
							stack.push(production.get(pTokenId));
					}
					
				// If error
				} else {
					
					// If pop
					if(cell.getErrorDecision().equals(ABParserTable.ABParserTableCell.POP)) {
						
						// Pop the stack
						stack.pop();
						
						// Create snapshot
						ABParserSnapshot snapshot = new ABParserSnapshot(++step, StringUtils.join(stack, " "), tokensStartAt(tokens, inputTokenIndex), "", cell.getErrorMessage());
						snapshot.setError(true);
						
						// Add snapshot
						snapshots.add(snapshot);
						
					// If scan
					} else if(cell.getErrorDecision().equals(ABParserTable.ABParserTableCell.SCAN)) {
						
						// Scan next input token
						inputToken = tokens.get(++inputTokenIndex);
					
					// If undefined
					} else {
						l.error("Undefined behavior for the error cell: non-terminal: %s, terminal: %s", grammarToken.getValue(), inputToken.getToken());
					}
					
					// Mark error
					error = true;
				}
			}
		}
		
		// If input token has more unparsed input or there was an error
		if(!inputToken.getToken().equals(ABGrammarToken.END_OF_STACK) || error){
			
			// Create snapshot
			ABParserSnapshot snapshot = new ABParserSnapshot(++step, StringUtils.join(stack, " "), tokensStartAt(tokens, inputTokenIndex), "", "Your code cannot end with a non function declaration");
			snapshot.setError(true);
			
			// Add snapshot
			snapshots.add(snapshot);
			
			// Return false
			return false;
		}
		
		// Take snapshot
		snapshots.add(new ABParserSnapshot(++step, StringUtils.join(stack, " "), tokensStartAt(tokens, inputTokenIndex), "", "Success"));
		
		// No errors
		return true;
	}
	
	/**
	 * Get tokens from specific index
	 * @param tokens
	 * @param index
	 * @return tokens substring
	 */
	private String tokensStartAt(ArrayList<ABToken> tokens, int index) {
		String result = "";
		for(int i = index; i < tokens.size(); i++)
			result += String.format("%s ", tokens.get(i).getToken());
		return result;
	}
	
	/**
	 * Get snapshot
	 * @return snapshots
	 */
	public List<ABParserSnapshot> getSnapshots() {
		return this.snapshots;
	}
	
	/**
	 * Adjust the value of the derivation based on the given arguments
	 * @param nonTerminal
	 * @param production
	 * @param derivation
	 */
	private void derive(ABGrammarToken nonTerminal, List<ABGrammarToken> production, List<ABGrammarToken> derivation) {
		
		// Loop on tokens
		for(int i=0; i<derivation.size(); i++) {
			
			// Get token
			ABGrammarToken token = derivation.get(i);
			
			// If non terminal is the target
			if(token == nonTerminal) {
				
				// Remove at index i
				derivation.remove(i);
				
				// Add the new production
				for(int j=production.size()-1; j>=0; j--)
					if(!production.get(j).isEpsilon())
						derivation.add(i, production.get(j));
					
				// Break when found
				break;
			}
		}
	}
	
	/**
	 * Snapshot of the data at a particular moment
	 */
	public class ABParserSnapshot {
		
		// Variables
		private String stack, input, production, derivation;
		private int id;
		private boolean isError = false;

		/**
		 * Constructor
		 * @param stack
		 * @param input
		 * @param production
		 * @param derivation
		 */
		public ABParserSnapshot(int id, String stack, String input, String production,String derivation) {
			this.id = id;
			this.stack = stack;
			this.input = input;
			this.production = production;
			this.derivation = derivation;
			
			// Log
			l.info("%d || %s || %s || %s || %s", id, stack, input, production, derivation);
		}
		
		/**
		 * Set is error value
		 * @param isError
		 */
		public void setError(boolean isError) {
			this.isError = isError;
		}
		
		/**
		 * Check if is an error snapshot
		 * @return is error
		 */
		public boolean isError() {
			return this.isError;
		}

		/**
		 * @return stack
		 */
		public String getStack() {
			return stack;
		}

		/**
		 * @param stack stack to set
		 */
		public void setStack(String stack) {
			this.stack = stack;
		}

		/**
		 * @return input
		 */
		public String getInput() {
			return input;
		}

		/**
		 * @param input input to set
		 */
		public void setInput(String input) {
			this.input = input;
		}

		/**
		 * @return production
		 */
		public String getProduction() {
			return production;
		}

		/**
		 * @param production the production to set
		 */
		public void setProduction(String production) {
			this.production = production;
		}

		/**
		 * @return derivation
		 */
		public String getDerivation() {
			return derivation;
		}

		/**
		 * @param derivation the derivation to set
		 */
		public void setDerivation(String derivation) {
			this.derivation = derivation;
		}
		
		/**
		 * @return id
		 */
		public int getId() {
			return this.id;
		}
	}
}

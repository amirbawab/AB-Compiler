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
		ABToken[] tokens = new ABToken[scannerTokens.length + 1];
		for(int i=0; i < scannerTokens.length; i++) tokens[i] = scannerTokens[i];
		tokens[tokens.length-1] = new ABToken(ABGrammarToken.END_OF_STACK, ABGrammarToken.END_OF_STACK, -1, -1);
		
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
		ABToken inputToken = tokens[inputTokenIndex];
		
		// Take snapshot
		snapshots.add(new ABParserSnapshot(++step, StringUtils.join(stack, " "), tokensStartAt(tokens, inputTokenIndex), "", StringUtils.join(derivation, " ")));
		
		// Last snapshot
		ABParserSnapshot lastSnapshot = snapshots.get(snapshots.size()-1);
		
		// Log
		l.info("Start parsing input ...");
		l.info("%d || %s || %s || -- || %s", lastSnapshot.getId(), lastSnapshot.getStack(), lastSnapshot.getInput(), lastSnapshot.getDerivation());
		
		
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
					
					// Last snapshot
					lastSnapshot = snapshots.get(snapshots.size()-1);
					
					// Log
					l.info("%d || %s || %s || -- || --", lastSnapshot.getId(), lastSnapshot.getStack(), lastSnapshot.getInput());
					
					// Pop terminal from stack
					stack.pop();
					
					// Update inputToken
					inputToken = tokens[++inputTokenIndex];
					
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
					
					// Last snapshot
					lastSnapshot = snapshots.get(snapshots.size()-1);
					
					// Log
					l.info("%d || %s || %s || %s || %s", lastSnapshot.getId(), lastSnapshot.getStack(), lastSnapshot.getInput(), lastSnapshot.getProduction(), lastSnapshot.getDerivation());
					
					// Pop
					stack.pop();
					
					// Inverse RHS multiple push. Don't push EPSILON
					for(int pTokenId = production.size()-1; pTokenId >= 0; --pTokenId) {
						if(!production.get(pTokenId).isEpsilon())
							stack.push(production.get(pTokenId));
					}
					
				// If error
				} else {
					// TODO Error found
					error = true;
				}
			}
		}
		
		// If input token has more unparsed input or there was an error
		if(!inputToken.getToken().equals(ABGrammarToken.END_OF_STACK) || error){
			return false;
		}
		
		// Take snapshot
		snapshots.add(new ABParserSnapshot(++step, StringUtils.join(stack, " "), tokensStartAt(tokens, inputTokenIndex), "", "success"));
		
		// Last snapshot
		lastSnapshot = snapshots.get(snapshots.size()-1);
		
		// Log
		l.info("%d || %s || %s || -- || success", lastSnapshot.getId(), lastSnapshot.getStack(), lastSnapshot.getInput());
		
		// No errors
		return true;
	}
	
	/**
	 * Get tokens from specific index
	 * @param tokens
	 * @param index
	 * @return tokens substring
	 */
	private String tokensStartAt(ABToken[] tokens, int index) {
		String result = "";
		for(int i = index; i < tokens.length; i++)
			result += String.format("%s ", tokens[i].getToken());
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

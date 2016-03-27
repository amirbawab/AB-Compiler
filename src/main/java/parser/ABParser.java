package parser;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import parser.ABParserTable.ABParserTableCell;
import parser.ABParserTable.ABParserTableError;
import parser.ABParserTable.ABParserTableRule;
import parser.grammar.ABGrammar;
import parser.grammar.ABGrammarToken;
import static parser.helper.ABParserMessageHelper.*;
import scanner.ABScanner;
import scanner.ABToken;
import semantic.ABSemantic;
import semantic.ABSymbolTable;

public class ABParser {
	
	// Logger
	private Logger l = LogManager.getFormatterLogger(getClass());
		
	// Variables
	private ABParserTable abParseTable;
	private ABGrammar abGrammar;
	
	// Long scan process time
	private long parserProcessTime;
	
	// Store Snapshots
	private List<ABParserSnapshot> snapshots;

	// Semantic
	private ABSemantic semantic;
	
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
	 * @param scannerTokens
	 * @return true if parse was successful, otherwise false
	 */
	public boolean parse(List<ABToken> scannerTokens) {
		
		// Update time
		parserProcessTime = System.currentTimeMillis();
		
		// Add $ to end of tokens
		ArrayList<ABToken> tokens = new ArrayList<>(scannerTokens.size()+1);
		for(int i=0; i < scannerTokens.size(); i++) {

			// If token should not be excluded, add it
			if(!ABScanner.EXCLUDE_PARSER.contains(scannerTokens.get(i).getToken()))
				tokens.add(scannerTokens.get(i));
		}
		
		// If there are token to parse
		if(scannerTokens.size() > 0){
			ABToken token = scannerTokens.get(scannerTokens.size()-1);
			tokens.add(new ABToken(ABGrammarToken.END_OF_STACK, ABGrammarToken.END_OF_STACK, token.getRow(), token.getCol() + token.getValue().length()));
		
		// If no token to parse
		} else {
			tokens.add(new ABToken(ABGrammarToken.END_OF_STACK, ABGrammarToken.END_OF_STACK, 0, 0));
		}
		
		// Prepare derivation list
		List<ABGrammarToken> derivation = new ArrayList<>();
		
		// Prepare stack
		Stack<ABGrammarToken> stack = new Stack<>();

		// Create semantic
		this.semantic = new ABSemantic();
		
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
		snapshots.add(new ABParserSnapshot(++step, stackNoAction(stack), tokensStartAt(tokens, inputTokenIndex), "", StringUtils.join(derivation, " "), false));
		
		// While top is not $
		while(!stack.peek().isEndOfStack()) {
			
			// Store top
			ABGrammarToken grammarToken = stack.peek();

			// If action
			if(grammarToken.isAction()) {

				// Pop for now
				ABGrammarToken token = stack.pop();

				// If no  errors
				if(!error)
					semantic.eval(token, tokens, inputTokenIndex);

			// If is terminal
			} else if(grammarToken.isTerminal()) {
				
				// If match
				if(grammarToken.getValue().equals(inputToken.getToken())) {
					
					// Take snapshot
					snapshots.add(new ABParserSnapshot(++step, stackNoAction(stack), tokensStartAt(tokens, inputTokenIndex), "", "", false));
					
					// Pop terminal from stack
					stack.pop();
					
					// Update inputToken
					inputToken = tokens.get(++inputTokenIndex);
					
				// If no match
				} else {

					// Prepare message
					String errorMessage = inputToken.getToken().equals(ABGrammarToken.END_OF_STACK) ? "Unexpected end of file" : String.format(GENERIC_UNEXPECTED_TOKEN_3, inputToken.getValue(), inputToken.getRow(), inputToken.getCol());
					
					// Add snapshot
					snapshots.add(new ABParserSnapshot(++step, stackNoAction(stack), tokensStartAt(tokens, inputTokenIndex), "", errorMessage, true));

					// TODO This part has to be avoided
					l.error("This statement should never execute. Step log: %s", snapshots.get(snapshots.size()-1));

					// Pop
					stack.pop();
					
					// Error found
					error = true;
				}
			
			// If is non terminal
			} else {
				
				// Get cell
				ABParserTable.ABParserTableCell cell = abParseTable.getTableAt(grammarToken.getValue(), inputToken.getToken());
				
				// If not an error
				if(!cell.isError()) {
					
					// Store production
					List<ABGrammarToken> productionWithAction = cell.getProductionWithAction();
					List<ABGrammarToken> production = cell.getProduction();

					// Adjust the derivation
					derive(grammarToken, production, derivation);
					
					// Take snapshot
					snapshots.add(new ABParserSnapshot(++step, stackNoAction(stack), tokensStartAt(tokens, inputTokenIndex), cell.getId() +": "+ grammarToken.getValue()+"->"+StringUtils.join(production, " "), "=> "+ StringUtils.join(derivation, " "), false));

					// Pop
					stack.pop();
					
					// Inverse RHS multiple push. Don't push EPSILON
					for(int pTokenId = productionWithAction.size()-1; pTokenId >= 0; --pTokenId) {
						if(!productionWithAction.get(pTokenId).isEpsilon())
							stack.push(productionWithAction.get(pTokenId));
					}
					
				// If error
				} else {
					
					// Input value
					String inputValue = inputToken.getValue();
					
					// Adjust value if $
					if(inputValue.equals(ABGrammarToken.END_OF_STACK))
						inputValue = EOF;
					
					// Prepare message
					String errorMessage =  String.format(cell.getErrorMessage(), inputValue, inputToken.getRow(), inputToken.getCol());
					
					// Add snapshot
					snapshots.add(new ABParserSnapshot(++step, stackNoAction(stack), tokensStartAt(tokens, inputTokenIndex), "", errorMessage, true));
					
					// If pop
					if(cell.getErrorDecision().equals(ABParserTable.ABParserTableCell.POP)) {
						
						// Pop the stack
						stack.pop();
						
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
		
		// If input token has more unparsed input
		if(!inputToken.getToken().equals(ABGrammarToken.END_OF_STACK)){
			
			// Add snapshot
			snapshots.add(new ABParserSnapshot(++step, stackNoAction(stack), tokensStartAt(tokens, inputTokenIndex), "", String.format(GENERIC_UNEXPECTED_CODE_3, inputToken.getValue(), inputToken.getRow(), inputToken.getCol()), true));
			
			// Error found
			error = true;
		}
		
		// If there was an error
		if(error){
			
			// Add snapshot
			snapshots.add(new ABParserSnapshot(++step, stackNoAction(stack), tokensStartAt(tokens, inputTokenIndex), "", FAILURE, false));
			
			// Update time
			parserProcessTime = System.currentTimeMillis() - parserProcessTime;
			
			return false;
		}

		// Take snapshot
		snapshots.add(new ABParserSnapshot(++step, stackNoAction(stack), tokensStartAt(tokens, inputTokenIndex), "", SUCCESS, false));
		
		// Update time
		parserProcessTime = System.currentTimeMillis() - parserProcessTime;

		// Log tables
		l.info(semantic.tablesToString());

		// No errors
		return true;
	}
	
	/**
	 * Get parser process time
	 * @return process time
	 */
	public long getParserProcessTime() {
		return this.parserProcessTime;
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
			result += tokens.get(i).getToken() + " ";
		return result;
	}
	
	/**
	 * Get non error snapshot
	 * @return snapshots
	 */
	public List<ABParserSnapshot> getAllSnapshots() {
		return this.snapshots;
	}
	
	/**
	 * Get error snapshot
	 * @return snapshots
	 */
	public List<ABParserSnapshot> getFilteredErrorSnapshots() {
		
		// Prepare list
		List<ABParserSnapshot> filterError = new ArrayList<>();
		
		// Loop on snapshots
		for(int i=0; i < snapshots.size(); i++) {
			
			// If error
			if(snapshots.get(i).isError()) {
				
				// Cache
				ABParserSnapshot snapshot = snapshots.get(i);
				
				// Add to list
				filterError.add(snapshot);
				
				// Skip the other consecutive errors
				while (snapshot.isError() && ++i < snapshots.size()) {
					snapshot = snapshots.get(i);
				}
			}
		}
		return filterError;
	}
	
	/**
	 * Get first and follow sets
	 * @return first and follow sets data
	 */
	public Object[][] getFirstFollowSetsData() {
		
		// Get non terminal
		Set<String> nonTerminals = abGrammar.getNonTerminals();
		
		// Prepare table
		Object[][] table = new Object[nonTerminals.size()][3];
		
		// Counter
		int rowCounter = 0;
		
		// Populate table
		for(String nonTerminal : nonTerminals) {
			table[rowCounter][0] = nonTerminal;
			table[rowCounter][1] = StringUtils.join(abGrammar.getFirstOf(nonTerminal), ", ");
			table[rowCounter][2] = StringUtils.join(abGrammar.getFollowOf(nonTerminal), ", ");
			++rowCounter;
		}
		
		return table;
	}
	
	/**
	 * Get parsing data
	 * @return parsing data
	 */
	public Object[][] getParsingTableData() {
		
		// Get terminals and non terminals
		Set<String> terminals = abGrammar.getTerminals();
		Set<String> nonTerminals = abGrammar.getNonTerminals();
		
		// Prepare table
		Object[][] table = new Object[nonTerminals.size()+1][terminals.size()+1];
				
		// Set terminals
		for(String terminal : terminals) {
			int index = abParseTable.getIndexOfTerminal(terminal);
			table[0][index+1] = terminal;
		}
		
		// Set non terminals
		for(String nonTerminal : nonTerminals) {
			int index = abParseTable.getIndexOfNonTerminal(nonTerminal);
			table[index+1][0] = nonTerminal;
		}
		
		// Get table
		ABParserTableCell[][] parseTable = abParseTable.getTable();
		
		// Copy data
		for(int row = 1; row < table.length; ++row) {
			for(int col = 1; col < table[row].length; ++col) {
				ABParserTableCell cell = parseTable[row-1][col-1];
				String data = cell.getId();
				if(cell.isError()) {
					if(cell.getErrorDecision().equals(ABParserTableCell.POP))
						data += " Pop";
					
					else if (cell.getErrorDecision().equals(ABParserTableCell.SCAN))
						data += " Scan";
				}
				table[row][col] = data;
			}
		}
		
		return table;
	}
	
	/**
	 * Get parsing table rules data
	 * @return parsing table rules data
	 */
	public Object[][] getParsingTableRulesData() {
		
		// Get rules
		Collection<ABParserTableRule> tableRules = abParseTable.getRules();
		
		// Prepare table
		Object[][] table = new Object[tableRules.size()][2];

		int row = 0;
		for(ABParserTableRule tableRule : tableRules) {
			table[row][0] = tableRule.getId();
			table[row++][1] = tableRule.getLHS() + " -> " + StringUtils.join(tableRule.getProduction(), " ");
		}
		
		return table;
	}
	
	/**
	 * Get parsing table error data
	 * @return parsing table error data
	 */
	public Object[][] getParsingTableErrorsData() {
		
		// Get rules
		Collection<ABParserTableError> tableErrors = abParseTable.getErrors();
		
		// Prepare table
		Object[][] table = new Object[tableErrors.size()][2];

		int row = 0;
		for(ABParserTableError tableError : tableErrors) {
			table[row][0] = tableError.getId();
			table[row++][1] = tableError.getMessage();
		}
		
		return table;
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
	 * Get all symbol tables
	 * @return
     */
	public List<ABSymbolTable> getSymbolTables() {
		return semantic.getAllTables();
	}

	/**
	 * Get string representatin of stack without action tokens
	 * @param stack
     * @return
     */
	private String stackNoAction(Stack<ABGrammarToken> stack) {
		String value = "";
		for(ABGrammarToken token : stack)
			if(!token.isAction())
				value += token.getValue() + " ";
		return value.trim();
	}

	/**
	 * Snapshot of the data at a particular moment
	 */
	public class ABParserSnapshot {
		
		// Variables
		private String stack, input, production, derivation;
		private int id;
		private boolean isError;

		/**
		 * Constructor
		 * @param stack
		 * @param input
		 * @param production
		 * @param derivation
		 */
		public ABParserSnapshot(int id, String stack, String input, String production,String derivation, boolean isError) {
			this.id = id;
			this.stack = stack;
			this.input = input;
			this.production = production;
			this.derivation = derivation;
			this.isError = isError;
			
			// Log
			l.info("%d || %s || %s || %s || %s", id, stack, input, production, derivation);
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
		 * Check if is error
		 * @return true if error snapshot
		 */
		public boolean isError() {
			return this.isError;
		}
		
		/**
		 * Set error
		 * @param isError boolean
		 */
		public void setError(boolean isError) {
			this.isError = isError;
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

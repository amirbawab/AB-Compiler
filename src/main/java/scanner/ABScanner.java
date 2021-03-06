package scanner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import scanner.helper.ABTokenHelper;
import scanner.helper.IdentifierHelper;
import finiteAutomata.FiniteAutomata;
import finiteAutomata.State;

public class ABScanner {
	
	// Logger
	private Logger l = LogManager.getFormatterLogger(getClass());
		
	// Components
	private FiniteAutomata machine;
	private ABTableModel model;
	
	// Current line
	private String currentLine;
	
	// File line and index
	private int line, index;
	
	// Word row and column
	private int wordRow, wordCol;
	
	// Current token word
	private String word;
	
	// Lists
	private List<ABToken> nonErrorToken, errorToken;
	
	// Long scan process time
	private long scannerProcessTime;
	
	// Scanner
	private Scanner scan;
	
	// State
	private int state;
	
	// Exclude when parsing
	public static final Set<String> EXCLUDE_PARSER = new HashSet<>(Arrays.asList(ABTokenHelper.T_BLOCK_COMMENT, ABTokenHelper.T_INLINE_COMMENT));
	
	public ABScanner(String dfaFile) {
		
		try {
			// Create finite state machine
			machine = FiniteAutomata.inParser(dfaFile);
			
			// Create table from machine
			model = new ABTableModel(machine.getStates(), machine.getAllTransitionLabels());
			
			// Store logs
			l.info(model);
			
			// Init variables
			nonErrorToken = new ArrayList<>();
			errorToken = new ArrayList<>();
			word = "";
		} catch (IOException e) {
			l.error(e.getMessage());
		}
	}
	
	/**
	 * Process a full file
	 * @param text
	 */
	public void processText(String text) {
		
		// Update time
		this.scannerProcessTime = System.currentTimeMillis();
		
		// Reset row count
		this.line = 0;
		
		// Reset state
		this.state = 0;
		
		// Scan text
		scan = new Scanner(text);
		
		// Reset list of tokens
		nonErrorToken.clear();
		errorToken.clear();
		
		// Process lines
		while(scan.hasNextLine())
			processLine(scan.nextLine());
		
		// Close scanner
		scan.close();
		
		// Store scanner time
		this.scannerProcessTime = System.currentTimeMillis() - this.scannerProcessTime;
	}
	
	/**
	 * Process a line of code
	 * @param code
	 * @return list of tokens
	 */
	private void processLine(String code) {
		
		// Append EOL or EOF
		code += scan.hasNextLine() ? ABTableModel.EOL_CHAR : ABTableModel.EOF_CHAR;
		
		// Log
		l.info("> Scanning line: %s", code.replace(String.format("%c", ABTableModel.EOL_CHAR), "\\n").replace(String.format("%c", ABTableModel.EOF_CHAR), "EOF"));
		
		// Store line
		this.currentLine = code;
		
		// Reset column
		this.index = 0;
		
		// Increment row
		this.line++;
		
		// While there are more tokens to consume
		while(index < code.length()) {
			
			// Get next token
			ABToken token = nextToken();
			
			// If token found
			if(token != null) {
				
				// Log
				l.info("%s : %s", token.getValue(), token.getToken());
				
				// If error token, store it in error list
				if(token.getToken().startsWith(ABTokenHelper.ERROR_TOKEN_PREFIX))
					errorToken.add(token);
				
				// If not error token, store it in non error list
				else
					nonErrorToken.add(token);
					
			}
		}
	}
	
	/**
	 * Get next token
	 * @return next token
	 */
	private ABToken nextToken() {
		
		ABToken token = null;
		
		do {
			
			// If initial state
			if(state == 0){ 
				wordCol = index + 1;
				wordRow = line;
			}
			
			// Current char
			Character currentChar = nextChar();
			
			// Update state
			state = model.lookup(state, currentChar);
			
			// Fetch new state
			State currentState = model.getStateAtRow(state);
			
			// If final
			if(currentState.isFinal()) {
				
				// If should backup, then backup one char
				if(currentState.getBacktrack()) {
					backupChar();
					
				// If should not backup, then the character is part of the token value
				} else {
					word += currentChar;
				}
				
				// Token value
				String tokenValue = IdentifierHelper.getTokenIfReservedWord(word, currentState.getToken());
				
				// Create token
				token = new ABToken(tokenValue, word, wordRow, wordCol);
				
				// Reset word
				word = "";
				
				// Go to initial state
				state = 0;
			
			// If not final state and not in the initial state
			} else if(state != 0) {
				word += currentChar;
			}
		} while(token == null && index < currentLine.length());
		return token;
	}
	
	/**
	 * Get next char
	 * @return next char
	 */
	private Character nextChar() {
		return currentLine.charAt(index++);
	}
	
	/**
	 * Backup one char
	 */
	private void backupChar() {
		index--;
	}
	
	/**
	 * Get error tokens
	 * @return error token
	 */
	public List<ABToken> getErrorTokens() {
		return errorToken;
	}
	
	/**
	 * Get non error tokens
	 * @return non error token
	 */
	public List<ABToken> getNonErrorTokens() {
		return nonErrorToken;
	}
	
	/**
	 * Get table data
	 * @return model
	 */
	public Object[][] getTableData() {
		Object[][] table = new Object[model.getNumOfRow() + 1][model.getNumOfCol() + 4];
		
		// Add columns
		for(int col = 1; col < table[0].length-3; col++)
			table[0][col] = model.getCharAtCol(col-1);

		// Add extra columns
		table[0][table[0].length-3] = "Backtrack";
		table[0][table[0].length-2] = "Final";
		table[0][table[0].length-1] = "Token";
		
		// Add rows
		for(int row = 1; row < table.length; row++)
			table[row][0] = model.getStateAtRow(row-1).getVID();
		
		for(int row = 1; row < table.length; row++) {
			for(int col = 1; col < table[row].length - 3; col++) {
				table[row][col] = model.getAt(row-1, col-1);
			}
		
			// Add extra columns
			table[row][table[row].length-3] = model.getStateAtRow(row-1).getBacktrack() ? "yes" : "no";
			table[row][table[row].length-2] = model.getStateAtRow(row-1).isFinal() ? "yes" : "no";
			table[row][table[row].length-1] = model.getStateAtRow(row-1).isFinal() ? model.getStateAtRow(row-1).getToken() : "";
		}
		
		return table;
	}
	
	/**
	 * Get scanner process time
	 * @return scanner process time in ms
	 */
	public long getScannerProcessTime() {
		return this.scannerProcessTime;
	}
}

package scanner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import finiteAutomata.FiniteAutomata;
import finiteAutomata.State;

public class ABScanner {
	
	// Logger
	private Logger l = LogManager.getFormatterLogger(getClass());
		
	// Components
	private FiniteAutomata machine;
	private ABTableModel model;
	
	// Variables
	private String currentLine;
	private int row, col;
	
	// Lists
	private List<ABToken> nonErrorToken, errorToken;
	
	// Long scan process time
	private long scannerProcessTime;
	
	// Scanner
	private Scanner scan;
	
	// State
	int state;
	
	// Prefix
	private final String ERROR_PREFIX = "T_ERR_";
	
	public ABScanner(String dfaFile) {
		
		try {
			// Create finite state machine
			machine = FiniteAutomata.inParser(dfaFile);
			
			// Create table from machine
			model = new ABTableModel(machine.getStates(), machine.getAllTransitionLabels());
			
			// Store logs
			l.info(model);
			
			// Init list of tokens
			nonErrorToken = new ArrayList<>();
			errorToken = new ArrayList<>();
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
		this.row = 0;
		
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
		
		// Log
		l.info("Scanning line: %s", code);
		
		// Store line
		this.currentLine = code;
		
		// Reset column
		this.col = 0;
		
		// Increment row
		this.row++;
		
		// While there are more tokens to consume
		while(col < code.length()) {
			
			// Get next token
			ABToken token = nextToken();
			
			// If token found
			if(token != null) {
				
				// Log
				l.info("%s : %s", token.getValue(), token.getToken());
				
				// If error token, store it in error list
				if(token.getToken().startsWith(ERROR_PREFIX))
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
		
		// FIXME block comment value
		// FIXME block comment not closed
		
		ABToken token = null;
		int startIndex = col;
		
		do {
			// Current char
			Character currentChar = nextChar();
			
			// Update state
			state = model.lookup(state, currentChar);
			
			// If state is 0, update start index
			if(state == 0) startIndex = col;
			
			// Fetch new state
			State currentState = model.getStateAtRow(state);
			
			// If final
			if(currentState.isFinal()) {
				
				// If not end of line and should backup, then backup one char
				if(currentState.getBacktrack())
					backupChar();
				
				// Cache word
				String word = currentLine.substring(startIndex, Math.min(col, currentLine.length()));
				
				// Token value
				String tokenValue = IdentifierHelper.getTokenIfReservedWord(word, currentState.getToken());
				
				// Create token
				token = new ABToken(tokenValue, word, row, startIndex+1);
				
				// Reset state
				state = 0;
			}
		} while(token == null && col <= currentLine.length());
		return token;
	}
	
	/**
	 * Get next char
	 * @return next char
	 */
	private Character nextChar() {
		if(col < currentLine.length())
			return currentLine.charAt(col++);
		
		// EOL and EOF are considered characters and can be repeated on backup, so increment col
		col++;
		
		// Return if end of line or end of file
		return scan.hasNextLine() ? ABTableModel.EOL_CHAR : ABTableModel.EOF_CHAR;
	}
	
	/**
	 * Backup one char
	 */
	private void backupChar() {
		col--;
	}
	
	/**
	 * Get error tokens
	 * @return error token array
	 */
	public ABToken[] getErrorTokens() {
		ABToken[] tmp = new ABToken[errorToken.size()];
		for(int i=0; i < tmp.length; i++)
			tmp[i] = errorToken.get(i);
		return tmp;
	}
	
	/**
	 * Get non error tokens
	 * @return non error token array
	 */
	public ABToken[] getNonErrorTokens() {
		ABToken[] tmp = new ABToken[nonErrorToken.size()];
		for(int i=0; i < tmp.length; i++)
			tmp[i] = nonErrorToken.get(i);
		return tmp;
	}
	
	/**
	 * Get scanner process time
	 * @return scanner process time in ms
	 */
	public long getScannerProcessTime() {
		return this.scannerProcessTime;
	}
}

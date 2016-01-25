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
		
		// Reset row count
		this.row = 0;
		
		// Scan text
		Scanner scan = new Scanner(text);
		
		// Reset list of tokens
		nonErrorToken.clear();
		errorToken.clear();
		
		// Process lines
		while(scan.hasNextLine())
			processLine(scan.nextLine());
		
		// Close scanner
		scan.close();
	}
	
	/**
	 * Process a line of code
	 * @param code
	 * @return list of tokens
	 */
	private void processLine(String code) {
		
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
		int state = 0;
		ABToken token = null;
		int startIndex = col;
		
		do {
			// Current char
			Character currentChar = nextChar();
			
			// If end of line EOL
			if(currentChar == null){
				
				// If previous char is a space
				if(state == 0)
					return null;
				
				state = model.getOtherOf(state);
			} else {
				state = model.lookup(state, currentChar);
			}
			
			// If state is 0, update start index
			if(state == 0)
				startIndex = col;
			
			// Fetch new state
			State currentState = model.getStateAtRow(state);
			
			// If final
			if(currentState.isFinal()) {
				
				// If not end of line and should backup, then backup one char
				if(currentChar != null && currentState.getBacktrack())
					backupChar();
				
				// Create token
				token = new ABToken(currentState.getToken(), currentLine.substring(startIndex, col), row, startIndex+1);
			}
		} while(token == null);
		return token;
	}
	
	/**
	 * Get next char
	 * @return next char
	 */
	private Character nextChar() {
		if(col < currentLine.length())
			return currentLine.charAt(col++);
		return null;
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
}

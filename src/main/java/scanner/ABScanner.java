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
	
	public ABScanner(String dfaFile) {
		
		try {
			// Create finite state machine
			machine = FiniteAutomata.inParser(dfaFile);
			
			// Generate table
			generateTable();
		} catch (IOException e) {
			l.error(e.getMessage());
		}
	}
	
	/**
	 * Generate table
	 */
	private void generateTable() {
		// Create table from machine
		model = new ABTableModel(machine.getStates(), machine.getAllTransitionLabels());
		
		// Store logs
		l.info(model);
	}
	
	/**
	 * Process a full file
	 * @param text
	 */
	public List<ABToken> processText(String text) {
		this.row = 0;
		Scanner scan = new Scanner(text);
		List<ABToken> tokens = new ArrayList<>();
		while(scan.hasNextLine())
			processLine(scan.nextLine(), tokens);
		return tokens;
	}
	
	/**
	 * Process a line of code
	 * @param code
	 * @return list of tokens
	 */
	private void processLine(String code, List<ABToken> tokens) {
		this.currentLine = code;
		this.col = 0;
		this.row++;
		
		while(col < code.length()) {
			
			// Get next token
			ABToken token = nextToken();
			
			// If token found
			if(token != null) {
				tokens.add(token);
			}
		}
	}
	
	/**
	 * Get next token
	 * @return next oken
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
				
				token = new ABToken(currentState.getToken(), currentLine.substring(startIndex, col), row, startIndex+1);
				
				// If not end of line and should backup, then backup one char
				if(currentChar != null && currentState.getBacktrack()) {
					backupChar();
				}
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
}

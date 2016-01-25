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
			ABToken token = new ABToken(nextToken(), currentLine, row, col+1);
			tokens.add(token);
		}
	}
	
	/**
	 * Get next token
	 * @return next oken
	 */
	private String nextToken() {
		int state = 0;
		String token = null;
		
		do {
			// Current char
			Character currentChar = nextChar();
			
			// If end of line
			if(currentChar == null){
				state = model.getOtherOf(state);
			} else {
				state = model.lookup(state, currentChar);
			}
			
			// Fetch new state
			State currentState = model.getStateAtRow(state);
			
			// If final
			if(currentState.isFinal()) {
				
				// Store token
				token = currentState.getToken();
				
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

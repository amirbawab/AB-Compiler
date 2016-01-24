package scanner;

import finiteAutomata.FiniteAutomata;
import finiteAutomata.State;
import finiteAutomata.Transition;
import graph.Edge;
import graph.doublyLinkedList.NodeIterator;

public class ABTableModel {

	int row, col;
	int[][] table;
	State[] states;
	char[] header;
	
	public ABTableModel(State[] states, char[] header) {
		this.table = new int[states.length][header.length];
		this.states = states;
		this.header = header;
		populateTable();
	}
	
	/**
	 * Get row col in a table
	 * @param row
	 * @param col
	 * @return table[row][col]
	 */
	public int getAt(int row, int col) {
		return this.table[row][col];
	}
	
	/**
	 * Get state at row
	 * @param row
	 * @return State
	 */
	public State getStateAtRow(int row) {
		return states[row];
	}
	
	/**
	 * Get char at col
	 * @param col
	 * @return char
	 */
	public char getCharAtCol(int col) {
		return header[col];
	}
	
	/**
	 * Get width
	 * @return table width
	 */
	public int getNumOfCol() {
		return header.length;
	}
	
	/**
	 * Get height
	 * @return table height
	 */
	public int getNumOfRow() {
		return states.length;
	}
	
	/**
	 * Populate table content
	 */
	private void populateTable() {
		// Iterate on outgoing transitions
		for(int row = 0; row < states.length; row++) {
			State currentState = states[row];
			State onOther = currentState.getOnRead(FiniteAutomata.OTHER);
			
			// Adjust the values
			for(int col = 0; col < header.length; col++) {
				State toState = currentState.getOnRead(header[col]);
				
				// If final, there no other state
				if(currentState.isFinal()){
					table[row][col] = toState == null ? 0 : toState.getVID();
				} else {
					table[row][col] = toState == null ? onOther.getVID() : toState.getVID();
				}
			}
		}
	}
	
	/**
	 * Formatted table
	 */
	public String toString() {
		String output = "\n\t";
		
		for(int col = 0; col < header.length; col++)
			output += header[col] + "\t";
		
		output += "Bt\t";
		output += "Final [token]";
		
		output += "\n";
		for(int row = 0; row < states.length; row++) {
			output += row + "\t";
			for(int col = 0; col < header.length; col++) {
				output += table[row][col] + "\t";
			}
			output += (states[row].getBacktrack() ? "yes" : "no") + "\t";
			
			if(states[row].isFinal())
				output += "yes [" + states[row].getToken() +"]";
			else
				output += "no";
			
			output += "\n";
		}
		return output;
	}
}

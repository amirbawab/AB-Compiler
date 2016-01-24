package scanner;

import finiteAutomata.State;

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
	
	private void populateTable() {
		
	}
	
	public String toString() {
		String output = "\n\t";
		
		for(int col = 0; col < header.length; col++)
			output += header[col] + "\t";
		
		output += "\n";
		for(int row = 0; row < states.length; row++) {
			output += row + "\t";
			for(int col = 0; col < header.length; col++) {
				output += table[row][col] + "\t";
			}
			output += "\n";
		}
		return output;
	}
}

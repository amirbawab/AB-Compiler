import gui.MainFrame;
import gui.listener.ABIDEListener;

import scanner.ABScanner;
import scanner.ABToken;

public class Application {
	public static void main(String[] args) {
		
		// Create scanner
		final ABScanner abScanner = new ABScanner("/scanner/machine.dfa");
		
		// Start GUI
		MainFrame frame = new MainFrame("AB Editor");
		
		// Set listener
		frame.setABIDEListener(new ABIDEListener() {

			@Override
			public void analyze(String text) {
				abScanner.processText(text);
			}
			
			@Override
			public Object[][] getScannerOutput() {
				ABToken[] tokens = abScanner.getNonErrorTokens();
				Object[][] table = new Object[tokens.length][4];
				for(int i=0; i < table.length; i++) {
					table[i][0] = tokens[i].getToken();
					table[i][1] = tokens[i].getValue();
					table[i][2] = tokens[i].getRow();
					table[i][3] = tokens[i].getCol();
				}
				return table;
			}

			@Override
			public Object[][] getScannerError() {
				ABToken[] tokens = abScanner.getErrorTokens();
				Object[][] table = new Object[tokens.length][4];
				for(int i=0; i < table.length; i++) {
					table[i][0] = tokens[i].getToken();
					table[i][1] = tokens[i].getValue();
					table[i][2] = tokens[i].getRow();
					table[i][3] = tokens[i].getCol();
				}
				return table;
			}

			@Override
			public long getScannerTime() {
				return abScanner.getScannerProcessTime();
			}
		});
	}
}


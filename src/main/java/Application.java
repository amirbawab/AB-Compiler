import java.util.Arrays;
import java.util.List;

import parser.ABParser;
import parser.grammar.ABGrammar;
import gui.MainFrame;
import gui.listener.ABIDEListener;
import scanner.ABScanner;
import scanner.ABToken;
import scanner.helper.ErrorHelper;

public class Application {
	public static void main(String[] args) {
		
		// Create scanner
		final ABScanner abScanner = new ABScanner("/scanner/machine.dfa");
		
		// Create parser
		final ABParser abParser = new ABParser("/parser/grammar.bnf");
		
		// Start GUI
		MainFrame frame = new MainFrame("AB Editor");
		
		// Set listener
		frame.setABIDEListener(new ABIDEListener() {

			// Cache
			private ABToken[] nonErrorTokens, errorTokens;
			
			@Override
			public void analyze(String text) {
				abScanner.processText(text);
			}
			
			@Override
			public Object[][] getScannerOutput() {
				nonErrorTokens = abScanner.getNonErrorTokens();
				Object[][] table = new Object[nonErrorTokens.length][4];
				for(int i=0; i < table.length; i++) {
					table[i][0] = nonErrorTokens[i].getToken();
					table[i][1] = nonErrorTokens[i].getValue();
					table[i][2] = nonErrorTokens[i].getRow();
					table[i][3] = nonErrorTokens[i].getCol();
				}
				return table;
			}

			@Override
			public Object[][] getScannerError() {
				errorTokens = abScanner.getErrorTokens();
				Object[][] table = new Object[errorTokens.length][5];
				for(int i=0; i < table.length; i++) {
					table[i][0] = errorTokens[i].getToken();
					table[i][1] = errorTokens[i].getValue();
					table[i][2] = errorTokens[i].getRow();
					table[i][3] = errorTokens[i].getCol();
					table[i][4] = ErrorHelper.getComment(errorTokens[i].getToken(), errorTokens[i].getValue(), errorTokens[i].getRow(), errorTokens[i].getCol());
				}
				return table;
			}

			@Override
			public long getScannerTime() {
				return abScanner.getScannerProcessTime();
			}

			@Override
			public Object[][] getParserOutput() {
				
				// Parse
				boolean error = abParser.parse(nonErrorTokens);
				
				// Get snapshots
				List<ABParser.ABParserSnapshot> snapshots = abParser.getSnapshots();
				
				Object[][] table = new Object[snapshots.size()][5];
				for(int i=0; i < table.length; i++) {
					table[i][0] = snapshots.get(i).getId();
					table[i][1] = snapshots.get(i).getStack();
					table[i][2] = snapshots.get(i).getInput();
					table[i][3] = snapshots.get(i).getProduction();
					table[i][4] = snapshots.get(i).getDerivation();
				}
				return table;
			}
		});
	}
}


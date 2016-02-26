import java.util.List;

import parser.ABParser;
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
			private List<ABToken> nonErrorTokens, errorTokens;
			private List<ABParser.ABParserSnapshot> nonErrorSnapshots, errorSnapshots;
			
			/**
			 * Analyze 
			 */
			
			@Override
			public void scan(String text) {
				abScanner.processText(text);
			}
			
			@Override
			public void parse() {
				abParser.parse(nonErrorTokens);
			}
			
			/**
			 * Console
			 */
			
			@Override
			public Object[][] getScannerOutput() {
				nonErrorTokens = abScanner.getNonErrorTokens();
				Object[][] table = new Object[nonErrorTokens.size()][4];
				for(int i=0; i < table.length; i++) {
					table[i][0] = nonErrorTokens.get(i).getToken();
					table[i][1] = nonErrorTokens.get(i).getValue();
					table[i][2] = nonErrorTokens.get(i).getRow();
					table[i][3] = nonErrorTokens.get(i).getCol();
				}
				return table;
			}

			@Override
			public Object[][] getScannerError() {
				errorTokens = abScanner.getErrorTokens();
				Object[][] table = new Object[errorTokens.size()][5];
				for(int i=0; i < table.length; i++) {
					table[i][0] = errorTokens.get(i).getToken();
					table[i][1] = errorTokens.get(i).getValue();
					table[i][2] = errorTokens.get(i).getRow();
					table[i][3] = errorTokens.get(i).getCol();
					table[i][4] = ErrorHelper.getComment(errorTokens.get(i).getToken(), errorTokens.get(i).getValue(), errorTokens.get(i).getRow(), errorTokens.get(i).getCol());
				}
				return table;
			}

			@Override
			public Object[][] getParserOutput() {
				
				// Get snapshots
				nonErrorSnapshots = abParser.getNonErrorSnapshots();
				
				Object[][] table = new Object[nonErrorSnapshots.size()][5];
				for(int i=0; i < table.length; i++) {
					table[i][0] = nonErrorSnapshots.get(i).getId();
					table[i][1] = nonErrorSnapshots.get(i).getStack();
					table[i][2] = nonErrorSnapshots.get(i).getInput();
					table[i][3] = nonErrorSnapshots.get(i).getProduction();
					table[i][4] = nonErrorSnapshots.get(i).getDerivation();
				}
				return table;
			}

			@Override
			public Object[][] getParserError() {
				
				// Get snapshots
				errorSnapshots = abParser.getErrorSnapshots();
				
				Object[][] table = new Object[errorSnapshots.size()][5];
				for(int i=0; i < table.length; i++) {
					table[i][0] = errorSnapshots.get(i).getId();
					table[i][1] = errorSnapshots.get(i).getStack();
					table[i][2] = errorSnapshots.get(i).getInput();
					table[i][3] = errorSnapshots.get(i).getDerivation();
				}
				return table;
			}

			
			/**
			 * Compilation time
			 */
			
			@Override
			public long getScannerTime() {
				return abScanner.getScannerProcessTime();
			}
			
			@Override
			public long getParserTime() {
				return abParser.getParserProcessTime();
			}

			/**
			 * Menu
			 */
			
			@Override
			public Object[][] getStateTable() {
				return abScanner.getTableData();
			}

			@Override
			public Object[][] getParsingTable() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Object[][] getFirstAndFollowSets() {
				// TODO Auto-generated method stub
				return null;
			}
		});
	}
}


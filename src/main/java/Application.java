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
//		final ABScanner abScanner = new ABScanner("/scanner/machine.dfa");
		
		ABParser abParser = new ABParser("/parser/grammar.bnf");
		
		// Start GUI
//		MainFrame frame = new MainFrame("AB Editor");
		
		// Set listener
//		frame.setABIDEListener(new ABIDEListener() {
//
//			@Override
//			public void analyze(String text) {
//				abScanner.processText(text);
//			}
//			
//			@Override
//			public Object[][] getScannerOutput() {
//				ABToken[] tokens = abScanner.getNonErrorTokens();
//				Object[][] table = new Object[tokens.length][4];
//				for(int i=0; i < table.length; i++) {
//					table[i][0] = tokens[i].getToken();
//					table[i][1] = tokens[i].getValue();
//					table[i][2] = tokens[i].getRow();
//					table[i][3] = tokens[i].getCol();
//				}
//				return table;
//			}
//
//			@Override
//			public Object[][] getScannerError() {
//				ABToken[] tokens = abScanner.getErrorTokens();
//				Object[][] table = new Object[tokens.length][5];
//				for(int i=0; i < table.length; i++) {
//					table[i][0] = tokens[i].getToken();
//					table[i][1] = tokens[i].getValue();
//					table[i][2] = tokens[i].getRow();
//					table[i][3] = tokens[i].getCol();
//					table[i][4] = ErrorHelper.getComment(tokens[i].getToken(), tokens[i].getValue(), tokens[i].getRow(), tokens[i].getCol());
//				}
//				return table;
//			}
//
//			@Override
//			public long getScannerTime() {
//				return abScanner.getScannerProcessTime();
//			}
//		});
	}
}


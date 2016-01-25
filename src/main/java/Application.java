import java.util.List;

import gui.MainFrame;
import gui.center.CenterPanel;
import gui.listener.ABIDEListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import scanner.ABScanner;
import scanner.ABToken;
import config.Config;

public class Application {
	// Logger
	private static Logger l = LogManager.getFormatterLogger(Application.class.getClass());
		
	public static void main(String[] args) {
		
		final ABScanner abScanner = new ABScanner("/scanner/machine.dfa");
		
		// Start GUI
		MainFrame frame = new MainFrame("AB Editor");
		
		frame.setABIDEListener(new ABIDEListener() {

			@Override
			public Object[][] scan(String text) {
				
				List<ABToken> tokens = abScanner.processText(text);
				Object[][] tableData = new Object[tokens.size()][4];
				for(int row=0; row < tokens.size(); row++) {
					ABToken token = tokens.get(row);
					tableData[row][0] = token.getToken();
					tableData[row][1] = token.getValue();
					tableData[row][2] = token.getRow();
					tableData[row][3] = token.getCol();
				}
				return tableData;
			}
		});
	}
}


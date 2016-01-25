import gui.MainFrame;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import scanner.ABScanner;
import config.Config;

public class Application {
	// Logger
	private static Logger l = LogManager.getFormatterLogger(Application.class.getClass());
		
	public static void main(String[] args) {
		
		ABScanner abScanner = new ABScanner("/scanner/machine.dfa");
		
		// Start GUI
		new MainFrame("AB Editor");
	}
}


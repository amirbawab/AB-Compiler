package scanner;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import finiteAutomata.FiniteAutomata;

public class ABScanner {
	
	// Logger
	private Logger l = LogManager.getFormatterLogger(getClass());
		
	public ABScanner() {
		
		try {
			
			// Create finite state machine
			FiniteAutomata machine = FiniteAutomata.inParser("/scanner/machine.dfa");
			
			// Create table from machine
			ABTableModel model = new ABTableModel(machine.getStates(), machine.getAllTransitionLabels());
			
			// Store logs
			l.info(model);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

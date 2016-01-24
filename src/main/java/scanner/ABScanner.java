package scanner;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.ListenableDirectedGraph;

import finiteAutomata.FiniteAutomata;

public class ABScanner {
	
	// Logger
	private Logger l = LogManager.getFormatterLogger(getClass());
		
	public ABScanner() {
		
		try {
			
			// Get file URI
			URI fileUri = new URI(getClass().getResource("/scanner/machine.dfa").getPath());
		
			// Create finite state machine
			FiniteAutomata machine = FiniteAutomata.inParser(fileUri.getPath());
			System.out.println(machine.toDot());
		} catch (URISyntaxException | FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}

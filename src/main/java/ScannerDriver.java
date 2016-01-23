import gui.MainFrame;

import java.awt.Color;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgraph.JGraph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.jgrapht.graph.SimpleGraph;

import scanner.Token;

import com.mxgraph.view.mxGraph;

import config.Config;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

public class ScannerDriver {
	// Logger
	private static Logger l = LogManager.getFormatterLogger(ScannerDriver.class.getClass());
		
	public static void main(String[] args) {
		Config g = Config.getInstance();
		
		// Start GUI
//		new MainFrame("AB Editor");
		
		Token[] lexicalTokens = Token.values();
		String concat = "";
		for(Token lexicalToken : lexicalTokens) {
			RegExp regex = new RegExp(lexicalToken.getValue());
			Automaton machine = regex.toAutomaton();
			
			l.info("----------------------------------");
			l.info(lexicalToken.getToken());
			l.info("\n" + machine);
		}
	}
}


import java.awt.*;
import java.util.*;
import java.util.List;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.util.mxConstants;
import parser.ABParser;
import gui.MainFrame;
import gui.listener.ABIDEListener;
import parser.grammar.ABGrammarToken;
import scanner.ABScanner;
import scanner.ABToken;
import scanner.helper.ErrorHelper;
import semantic.ABSemantic;
import semantic.ABSymbolTable;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import javax.swing.*;

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
			
			// Compile
			boolean doesCompile = true;
			
			/**
			 * Analyze 
			 */
			
			@Override
			public void scan(String text) {
				abScanner.processText(text);
				doesCompile = abScanner.getErrorTokens().size() == 0;
			}
			
			@Override
			public void parse() {
				doesCompile &= abParser.parse(nonErrorTokens);
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
				nonErrorSnapshots = abParser.getAllSnapshots();
				
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
				errorSnapshots = abParser.getFilteredErrorSnapshots();
				
				Object[][] table = new Object[errorSnapshots.size()][5];
				for(int i=0; i < table.length; i++) {
					table[i][0] = errorSnapshots.get(i).getId();
					table[i][1] = errorSnapshots.get(i).getStack();
					table[i][2] = errorSnapshots.get(i).getInput();
					table[i][3] = errorSnapshots.get(i).getDerivation();
				}
				return table;
			}

			@Override
			public Object[][][] getSymbolTables() {
				List<ABSymbolTable> tablesList = abParser.getSymbolTables();
				Object[][][] tablesArray = new Object[tablesList.size()][][];

				// Populate data
				for(int tableId = 0; tableId < tablesArray.length; tableId++) {
					// Add values
					tablesArray[tableId] = tablesList.get(tableId).getTableData();
				}
				return tablesArray;
			}

			@Override
			public String getSymbolTableName(int id) {
				return abParser.getSymbolTables().get(id).getName();
			}

			@Override
			public Object[][] getSemanticErrors() {

				// Cache error list
				List<ABSemantic.ABSemanticError> errors = abParser.getSemantic().getErrors();

				Object[][] data = new Object[errors.size()][4];
				for(int i=0; i < data.length; i++) {
					data[i][0] = errors.get(i).getToken().getValue();
					data[i][1] = errors.get(i).getToken().getRow();
					data[i][2] = errors.get(i).getToken().getCol();
					data[i][3] = errors.get(i).getMessage();
				}
				doesCompile &= data.length == 0;
				return data;
			}

			@Override
			public JPanel getParserTree() {
				JPanel panel = new JPanel();
				panel.setLayout(new BorderLayout());

				mxGraph graph = new mxGraph();
				Object parent = graph.getDefaultParent();

				// Configure graph
				graph.setAutoSizeCells(true);

				// Start drawing
				graph.getModel().beginUpdate();

				// Create nodes
				Queue<ABGrammarToken> tokensQueue = new LinkedList<>();
				Map<ABGrammarToken, Object> vertexMap = new HashMap<>();

				// Add root
				tokensQueue.offer(abParser.getTreeRoot());
				Object vRoot = graph.insertVertex(parent, null, tokensQueue.peek().getValue(), 20, 20, 80,30);
				vertexMap.put(tokensQueue.peek(), vRoot);
				try
				{
					while(!tokensQueue.isEmpty()) {

						// Peek
						ABGrammarToken currentToken = tokensQueue.poll();

						// Create node
						Object v1 = vertexMap.get(currentToken);
						graph.updateCellSize(v1);

						if(currentToken.isNonTerminal()) {
							for (int i=currentToken.getChildren().size()-1; i >= 0; i--) {

								// Get token
								ABGrammarToken token = currentToken.getChildren().get(i);

								// Add child
								Object v2 = graph.insertVertex(parent, null, token.getDetailedValue(), 240, 150, 80, 30);
								vertexMap.put(token, v2);
								tokensQueue.offer(token);

								graph.insertEdge(parent, null, null, v1, v2);
							}

						} else if(currentToken.isTerminal()) {
							graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, "green", new Object[]{v1});
							graph.setCellStyles(mxConstants.STYLE_FONTCOLOR, "white", new Object[]{v1});

						} else if(currentToken.isEpsilon()) {
							graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, "brown", new Object[]{v1});
							graph.setCellStyles(mxConstants.STYLE_FONTCOLOR, "white", new Object[]{v1});

						} else if(currentToken.isAction()) {
							graph.setCellStyles(mxConstants.STYLE_FONTCOLOR, "red", new Object[]{v1});
						}
					}

					mxHierarchicalLayout layout = new mxHierarchicalLayout(graph, SwingConstants.NORTH);
					layout.execute(parent);
				}
				finally
				{
					graph.getModel().endUpdate();
				}

				mxGraphComponent graphComponent = new mxGraphComponent(graph);
				graphComponent.setEnabled(false);
				panel.add(graphComponent, BorderLayout.CENTER);
				return panel;
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
				return abParser.getParsingTableData();
			}

			@Override
			public Object[][] getFirstAndFollowSets() {
				return abParser.getFirstFollowSetsData();
			}

			@Override
			public Object[][] getParsingTableRules() {
				return abParser.getParsingTableRulesData();
			}

			@Override
			public Object[][] getParsingTableErrors() {
				return abParser.getParsingTableErrorsData();
			}

			@Override
			public boolean doesCompile() {
				return doesCompile;
			}
		});
	}
}


package gui;

import gui.bottom.BottomPanel;
import gui.center.CenterPanel;
import gui.listener.ABIDEListener;
import gui.menu.MainMenu;
import gui.menu.dialogs.DFADialog;
import gui.menu.dialogs.FirstFollowDialog;
import gui.menu.dialogs.ParsingTableDialog;
import gui.menu.dialogs.ParsingTableErrorsDialog;
import gui.menu.dialogs.ParsingTableRulesDialog;
import gui.menu.dialogs.StateTableDialog;
import gui.menu.dialogs.StateTableInfoDialog;
import gui.menu.listeners.MainMenuListener;
import gui.tool.ToolBarPanel;
import gui.tool.ToolBarPanel.Button;
import gui.tool.listeners.ClickListener;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class MainFrame extends JFrame {
	
	private static final long serialVersionUID = -8026416994513756565L;

	// Listener
	private ABIDEListener abIDElistener;
	
	// Components
	private CenterPanel centerPanel;
	private ToolBarPanel toolBarPanel;
	private MainMenu mainMenu;
	private BottomPanel bottomPanel;
	
	public MainFrame(String title) {
		
		// Set application name
		setTitle(title);
		
		// Set default layout to border layout
		setLayout(new BorderLayout());
		
		// Init components
		this.centerPanel = new CenterPanel();
		this.toolBarPanel = new ToolBarPanel();
		this.mainMenu = new MainMenu(this);
		this.bottomPanel = new BottomPanel();
		
		// Configure bottom panel
		this.bottomPanel.setStyle(BottomPanel.Style.NORMAL);
		
		// Set listener
		this.toolBarPanel.setClickListener(new ClickListener() {
			
			@Override
			public void onClickListener(Button type) {
				switch(type) {
				case NEW_FILE:
					centerPanel.addNewFile();
					break;
					
				case RUN:
					if(abIDElistener != null) {
						
						// Prepare message
						String message = "";
						
						// Content
						String content = centerPanel.getFileContent();
						
						if(content.replace("\n", "").trim().isEmpty()) {
							
							// Show warning message
							JOptionPane.showMessageDialog(MainFrame.this, "Please enter code to compile", "Empty file", JOptionPane.WARNING_MESSAGE);
							
						// Not empty
						} else {
							
							// Analyze input
							abIDElistener.scan(content);
							
							// Scanner output
							Object[][] scannerOutputData = abIDElistener.getScannerOutput();
							centerPanel.setTableData(CenterPanel.SCANNER_OUTPUT_TITLE, scannerOutputData);
							
							// Error error
							Object[][] scannerErrorData = abIDElistener.getScannerError();
							centerPanel.setTableData(CenterPanel.SCANNER_ERROR_TITLE, scannerErrorData);
							
							// Compilation time
							long compilationTime = abIDElistener.getScannerTime();
							
							// Scanner error
							if(scannerErrorData.length > 0)
								
								// Update compiler message
								message += String.format("Scanner: %d error(s) found! ", scannerErrorData.length);

							// Parse token
							abIDElistener.parse();
							
							// Update time
							compilationTime += abIDElistener.getParserTime();
							
							// Parser output
							Object[][] parserOutputData = abIDElistener.getParserOutput();
							centerPanel.setTableData(CenterPanel.PARSER_OUTPUT_TITLE, parserOutputData);
							
							// Parser error
							Object[][] parserErrorData = abIDElistener.getParserError();
							centerPanel.setTableData(CenterPanel.PARSER_ERROR_TITLE, parserErrorData);
							
							// If parser error found, update compiler message
							if(parserErrorData.length > 0)
								message += String.format("Parser: %d error(s) found! ", parserErrorData.length);

							// Get tree
							centerPanel.setPanel(CenterPanel.PARSER_TREE_TITLE, abIDElistener.getParserTree());

							// Clear symbol tables
							centerPanel.removeTablesInNavigationTable(CenterPanel.SYMBOL_TABLE_TITLE);

							// Clear semantic errors
							centerPanel.resetTable(CenterPanel.SEMANTIC_ERROR_TITLE);

							// If no parsing errors
							if(parserErrorData.length == 0){
								Object[][][] symbolTables = abIDElistener.getSymbolTables();
								for(int i=0; i < symbolTables.length; i++) {
									String subTableName = abIDElistener.getSymbolTableName(i);
									centerPanel.addTableToNavigationTable(CenterPanel.SYMBOL_TABLE_TITLE, subTableName, CenterPanel.SYMBOL_TABLE_HEADER);
									centerPanel.setTableOfTableData(CenterPanel.SYMBOL_TABLE_TITLE, subTableName, symbolTables[i]);
								}

								// Semantic errors
								Object[][] semanticErrorData = abIDElistener.getSemanticErrors();
								centerPanel.setTableData(CenterPanel.SEMANTIC_ERROR_TITLE, semanticErrorData);

								// If semantic errors, update compiler message
								if(semanticErrorData.length > 0)
									message	+= String.format("Semantic: %d error(s) found! ", semanticErrorData.length);
							}

							// Insert time
							message += String.format("Total time: %d ms", compilationTime);
							
							// Set message
							bottomPanel.setCompilerMessageText(message);
							
							// Change color
							if(abIDElistener.doesCompile())
								bottomPanel.setStyle(BottomPanel.Style.SUCCESS);
							else
								bottomPanel.setStyle(BottomPanel.Style.ERROR);
						}
					}
					break;
				}
			}
		});
		
		// Add menu listeners
		mainMenu.setMainMenuListener(new MainMenuListener() {
			
			@Override
			public void menuClicked(MainMenu.Button button) {
				switch (button) {
				case DFA:
					
					// Open dialog
					new DFADialog(MainFrame.this);
					break;

				case FIRST_FOLLOW:
					
					if(abIDElistener != null) {
						
						// Get data
						Object[][] firstFollowData = abIDElistener.getFirstAndFollowSets();
						
						if(firstFollowData.length > 0)
							new FirstFollowDialog(MainFrame.this, firstFollowData);
					}
					break;

				case PARSING_TABLE:
					if(abIDElistener != null) {
						
						// Get data
						Object[][] parsingData = abIDElistener.getParsingTable();
						Object[][] parsingRulesData = abIDElistener.getParsingTableRules();
						Object[][] parsingErrorData = abIDElistener.getParsingTableErrors();
						
						if(parsingData.length > 0) {
							new ParsingTableDialog(MainFrame.this, parsingData);
							new ParsingTableRulesDialog(MainFrame.this, parsingRulesData);
							new ParsingTableErrorsDialog(MainFrame.this, parsingErrorData);
						}
					}
					break;

				case STATE_TABLE:
					
					if(abIDElistener != null) {
						
						// Get data
						Object[][] stateTableData = abIDElistener.getStateTable();
						
						if(stateTableData.length > 0) {
							new StateTableDialog(MainFrame.this, stateTableData);
							new StateTableInfoDialog(MainFrame.this);
						}
					}
					break;
					
				case EXIT:
					MainFrame.this.dispatchEvent(new WindowEvent(MainFrame.this, WindowEvent.WINDOW_CLOSING));
					break;
				}
			}
		});
		
		// Add menu
		setJMenuBar(mainMenu);
		
		// Add components
		add(this.centerPanel, BorderLayout.CENTER);
		add(this.toolBarPanel, BorderLayout.NORTH);
		add(this.bottomPanel, BorderLayout.SOUTH);
		
		// Screen dim
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		
		 // Configure the JFrame
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);        // Exit when click on X
        this.setPreferredSize(new Dimension((int) (screenDim.width*0.9), (int) (screenDim.height*0.9)));    // Frame initial size
        this.setMinimumSize(new Dimension(600, 600));        // Minimum window size
        this.setVisible(true);                               // Make the frame visible
        this.pack();                                         // Force setting the size of components
        this.setLocationRelativeTo(null);                    // Load on center of the screen
	}
	
	/**
	 * Set listener
	 * @param abIDElistener
	 */
	public void setABIDEListener(ABIDEListener abIDElistener) {
		this.abIDElistener = abIDElistener;
	}
}

package gui.center;

import gui.center.console.TabbedConsolePanel;
import gui.center.editor.TabbedTextEditorPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.Border;

public class CenterPanel extends JPanel {
	
	private static final long serialVersionUID = -2635837911890107546L;

	// Components
	private TabbedTextEditorPanel tabbedTextEditorPanel;
	private TabbedConsolePanel tabbedConsolePanel;
	private JSplitPane splitPane;
	
	// Headers
	public final Object[] SCANNER_OUTPUT_HEADER = {"Token", "Value", "Row", "Col"};
	public final Object[] SCANNER_ERROR_HEADER = {"Token", "Value", "Row", "Col", "Comment"};
	public final Object[] PARSER_OUTPUT_HEADER = {"Step", "Stack", "Input", "Production", "Derivation"};
	public final Object[] PARSER_ERROR_HEADER = {"Step", "Stack", "Input", "Comment"};
	public final Object[] SYMBOL_TABLE_HEADER = {"Name", "Kind", "Type", "Parameter", "Link"};

	// Panel titles
	public static final String 	SCANNER_OUTPUT_TITLE = "Scanner - Output",
								SCANNER_ERROR_TITLE = "Scanner - Error",
								PARSER_OUTPUT_TITLE = "Parser - Steps",
								PARSER_ERROR_TITLE = "Parser - Error",
								SYMBOL_TABLE_TITLE = "Symbol tables";
	
	
	public CenterPanel() {
		
		// Set layout
		setLayout(new BorderLayout());
		
		// Add padding
		Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
		setBorder(padding);
		
		// Init components
		this.tabbedTextEditorPanel = new TabbedTextEditorPanel();
		this.tabbedConsolePanel = new TabbedConsolePanel();
		
		// Add default panel
		addNewFile();
		
		// Add Custom consoles
		this.tabbedConsolePanel.addTable(SCANNER_OUTPUT_TITLE, SCANNER_OUTPUT_HEADER);
		this.tabbedConsolePanel.addTable(SCANNER_ERROR_TITLE, SCANNER_ERROR_HEADER);
		this.tabbedConsolePanel.addTable(PARSER_OUTPUT_TITLE, PARSER_OUTPUT_HEADER);
		this.tabbedConsolePanel.addTable(PARSER_ERROR_TITLE, PARSER_ERROR_HEADER);
		this.tabbedConsolePanel.addTableNavigation(SYMBOL_TABLE_TITLE);

		// Resize
		this.tabbedConsolePanel.setPreferredSize(new Dimension(0, 0));
		this.tabbedTextEditorPanel.setPreferredSize(new Dimension(0, 0));

		// Add and configure splitter 
		this.splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.tabbedTextEditorPanel, this.tabbedConsolePanel);
		this.splitPane.setResizeWeight(0.8);
		
		// Add components
		add(this.splitPane, BorderLayout.CENTER);
	}
	
	/**
	 * Set table data
	 * @param table
	 */
	public void setTableData(String panelTitle, Object[][] table) {
		this.tabbedConsolePanel.resetTable(panelTitle);
		
		if(table != null)
			for(int row=0; row<table.length; row++) 
				this.tabbedConsolePanel.addRowToTable(panelTitle, table[row]);
	}

	/**
	 * Set table data
	 * @param table
	 */
	public void setTableOfTableData(String inTable, String panelTitle, Object[][] table) {
		if(table != null)
			for(int row=0; row<table.length; row++)
				this.tabbedConsolePanel.addRowToTableInTableNavigation(inTable, panelTitle, table[row]);
	}

	/**
	 * Remove tables in a navigation table
	 * @param title
     */
	public void removeTablesInNavigationTable(String title) {
		this.tabbedConsolePanel.removeTablesInNavigationTable(title);
	}

	/**
	 * Add a table to a navigation table
	 * @param table
	 * @param subTable
	 * @param header
     */
	public void addTableToNavigationTable(String table, String subTable, Object[] header) {
		this.tabbedConsolePanel.addTableToTabelNavigation(table, subTable, header);
	}
	
	/**
	 * Add table data
	 */
	public void addTableRowData(String panelTitle, Object[] data) {
		this.tabbedConsolePanel.addRowToTable(panelTitle, data);
	}
	
	/**
	 * Get file content of the active file
	 * @return content of active file
	 */
	public String getFileContent() {
		return this.tabbedTextEditorPanel.getText(this.tabbedTextEditorPanel.getSelectedIndex());
	}
	
	/**
	 * Add a new file
	 */
	public void addNewFile() {
		this.tabbedTextEditorPanel.addDefaultTextEditor();
	}
}

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
	private final Object[] SCANNER_OUTPUT_HEADER = {"Token", "Value", "Row", "Col"};
	private final Object[] SCANNER_ERROR_HEADER = {"Token", "Value", "Row", "Col", "Comment"};
	
	// Panel titles
	public static final String 	SCANNER_OUTPUT_TITLE = "Scanner - Output",
								SCANNER_ERROR_TITLE = "Scanner - Error";
	
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
		for(int row=0; row<table.length; row++) {
			this.tabbedConsolePanel.addRowToTable(panelTitle, table[row]);
		}
	}
	
	/**
	 * Add table data
	 * @param table
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

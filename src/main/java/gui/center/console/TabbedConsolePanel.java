package gui.center.console;

import gui.center.console.components.ConsoleCellRender;
import gui.center.console.components.ConsoleTable;
import gui.center.console.components.ConsoleTableNavigation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;

public class TabbedConsolePanel extends JTabbedPane {
	
	
	private static final long serialVersionUID = 3408129578272533534L;
	
	// Store panels
	private Map<String, Component> tabPanelsMap;
	
	public TabbedConsolePanel() {
		
		// Init variables
		this.tabPanelsMap = new HashMap<>();
	}
	
	/**
	 * Add a new tab
	 * @param title Tab title
	 */
	public void addTextEditor(String title) {
		
		// Create panel
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		// Create and add text editor to panel
		JTextPane textPane = new JTextPane();
		textPane.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textPane);
		panel.add(scrollPane, BorderLayout.CENTER);
		
		// Add the tab
		addTab(title, null, scrollPane);
		
		// Store it in the map
		this.tabPanelsMap.put(title, textPane);
	}
	
	/**
	 * Add a new tab
	 * @param title Tab title
	 */
	public void addTable(String title, Object[] header) {
		
		// Create panel
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		// Create and add text editor to panel
		ConsoleTable table = new ConsoleTable(new Object[][]{}, header);
		JScrollPane scrollPane = new JScrollPane(table);
		panel.add(scrollPane, BorderLayout.CENTER);
		
		// Add the tab
		addTab(title, null, scrollPane);
		
		// Store it in the map
		this.tabPanelsMap.put(title, table);
	}

	/**
	 * Add a new table for a table navigation panel
	 * @param title
     */
	public void addTableNavigation(String title) {

		// Create panel
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		// Create and add text editor to panel
		ConsoleTableNavigation table = new ConsoleTableNavigation();
		JScrollPane scrollPane = new JScrollPane(table);
		panel.add(scrollPane, BorderLayout.CENTER);

		// Add the tab
		addTab(title, null, scrollPane);

		// Store it in the map
		this.tabPanelsMap.put(title, table);
	}

	/**
	 * Add table to table navigation
	 * @param subTableTitle
	 * @param header
     */
	public void addTableToTabelNavigation(String tableTitle, String subTableTitle, Object[] header) {
		ConsoleTableNavigation table = (ConsoleTableNavigation) getBoard(tableTitle);
		table.getTabbedPane().addTable(subTableTitle, header);
	}

	/**
	 * Add row to a table in  table navigation
	 * @param tableNavigationTitle
	 * @param tableTitle
	 * @param data
     */
	public void addRowToTableInTableNavigation(String tableNavigationTitle, String tableTitle, Object[] data) {
		ConsoleTableNavigation table = (ConsoleTableNavigation) getBoard(tableNavigationTitle);
		ConsoleTable subTable = (ConsoleTable) table.getTabbedPane().getBoard(tableTitle);
		subTable.addRow(data);
	}
	
	/**
	 * Add row to table panel
	 * @param panelTitle
	 * @param data
	 */
	public void addRowToTable(String panelTitle, Object[] data) {
		ConsoleTable table = (ConsoleTable) getBoard(panelTitle);
		table.addRow(data);
	}

	/**
	 * Reset table
	 * @param panelTitle
	 */
	public void resetTable(String panelTitle) {
		ConsoleTable table = (ConsoleTable) getBoard(panelTitle);
		table.clearAll();
	}

	/**
	 * Remove tables in a navigation table
	 * @param panelTitle
     */
	public void removeTablesInNavigationTable(String panelTitle) {
		ConsoleTableNavigation table = (ConsoleTableNavigation) getBoard(panelTitle);
		table.removeTables();
	}
	
	/**
	 * Get panel by title
	 * @param title
	 * @return panel or null
	 */
	public Component getBoard(String title) {
		return this.tabPanelsMap.get(title);
	}
	
	/**
	 * Close panel
	 * @param title
	 * @return true if it was removed
	 */
	public boolean closePanel(String title) {
		return this.tabPanelsMap.remove(title) != null;
	}
	
	/**
	 * Get the number of text editor
	 * @return number of tabs
	 */
	public int getNumberOfTabs() {
		return this.tabPanelsMap.size();
	}
	
	/**
	 * Enable tooltip text
	 * @param panelTitle
	 * @param identifier
	 */
	public void enableTooltipTextForColumn(String panelTitle, Object identifier) {
		ConsoleTable table = (ConsoleTable) getBoard(panelTitle);
		table.getColumn(identifier).setCellRenderer(new ConsoleCellRender());
	}

	public Map<String, Component> getTabPanelsMap() {
		return tabPanelsMap;
	}
}

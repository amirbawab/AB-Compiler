package gui.center.console;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.table.DefaultTableModel;

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
		JTable table = new JTable(new DefaultTableModel(new Object[][]{}, header));
		JScrollPane scrollPane = new JScrollPane(table);
		panel.add(scrollPane, BorderLayout.CENTER);
		
		// Add the tab
		addTab(title, null, scrollPane);
		
		// Store it in the map
		this.tabPanelsMap.put(title, table);
	}
	
	/**
	 * Add row to table panel
	 * @param panelTitle
	 * @param data
	 */
	public void addRowToTable(String panelTitle, Object[] data) {
		JTable table = (JTable) getBoard(panelTitle);
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.addRow(data);
	}

	/**
	 * Reset table
	 * @param panelTitle
	 */
	public void resetTable(String panelTitle) {
		JTable table = (JTable) getBoard(panelTitle);
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		int rowCount = model.getRowCount();
		for(int i=0; i<rowCount; i++)
			model.removeRow(0);
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
}

package gui.editor;

import gui.editor.flavor.TextLineNumber;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;

public class TabbedTextEditorPanel extends JTabbedPane {
	
	private static final long serialVersionUID = -7457396179112788684L;

	// Constants
	public static String DEFAULT_TITLE = "New document";
	
	// Unique id
	private static int UNIQUE_ID = 1;
	
	// Store panels
	private Map<String, JScrollPane> tabPanelsMap;
	
	public TabbedTextEditorPanel() {
		
		// Init variables
		this.tabPanelsMap = new HashMap<>();
	}
	
	/**
	 * Add a new tab
	 * @param title Tab title
	 */
	public void addTextEditor(String title) {
		
		// Create and add text editor to panel
		JTextPane textPane = new JTextPane();
		JScrollPane scrollPane = new JScrollPane(textPane);
		TextLineNumber tln = new TextLineNumber(textPane);
		scrollPane.setRowHeaderView( tln );
		
		// Add the tab
		addTab(title, null, scrollPane);
		
		// Store it in the map
		this.tabPanelsMap.put(title, scrollPane);
	}
	
	/**
	 * Add new default text editor
	 */
	public void addDefaultTextEditor() {
		addTextEditor(String.format("%s - %d", DEFAULT_TITLE, UNIQUE_ID++));
	}
	
	/**
	 * Get panel by title
	 * @param title
	 * @return panel or null
	 */
	public JScrollPane getPanel(String title) {
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

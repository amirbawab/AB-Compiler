package gui.center.editor;

import gui.center.editor.flavor.TextLineNumber;

import java.util.ArrayList;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.text.PlainDocument;

public class TabbedTextEditorPanel extends JTabbedPane {
	
	private static final long serialVersionUID = -7457396179112788684L;

	// Constants
	public static String DEFAULT_TITLE = "New document";
	
	public TabbedTextEditorPanel() {
		// Nothing 
	}
	
	/**
	 * Add a new tab
	 * @param title Tab title
	 */
	public void addTextEditor(String title) {
		
		// Create and add text editor to panel
		JEditorPane textPane = new JEditorPane();
		textPane.getDocument().putProperty(PlainDocument.tabSizeAttribute, 2);
		JScrollPane scrollPane = new JScrollPane(textPane);
		TextLineNumber tln = new TextLineNumber(textPane);
		scrollPane.setRowHeaderView( tln );
		
		// Add the tab
		addTab(title, null, scrollPane);
	}
	
	/**
	 * Add new default text editor
	 */
	public void addDefaultTextEditor() {
		addTextEditor(String.format("%s", DEFAULT_TITLE));
	}
	
	/**
	 * Get panel by index
	 * @param title
	 * @return panel or null
	 */
	public JScrollPane getPanel(int index) {
		return (JScrollPane) getTabComponentAt(index);
	}
}

package gui.center;

import gui.center.console.TabbedConsolePanel;
import gui.center.editor.TabbedTextEditorPanel;

import java.awt.BorderLayout;

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
		this.tabbedConsolePanel.addTextEditor("Scanner");
		this.tabbedConsolePanel.addTextEditor("Errors");
		
		// Add and configure splitter 
		this.splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.tabbedTextEditorPanel, this.tabbedConsolePanel);
		this.splitPane.setResizeWeight(0.5);
		
		// Add components
		add(this.splitPane, BorderLayout.CENTER);
	}
	
	/**
	 * Add a new file
	 */
	public void addNewFile() {
		this.tabbedTextEditorPanel.addDefaultTextEditor();
	}
}

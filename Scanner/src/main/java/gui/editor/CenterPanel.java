package gui.editor;

import java.awt.BorderLayout;

import javax.swing.JPanel;

public class CenterPanel extends JPanel {
	
	private static final long serialVersionUID = -2635837911890107546L;

	// Components
	private TabbedTextEditorPanel tabbedTextEditorPanel;
	
	public CenterPanel() {
		
		// Set layout
		setLayout(new BorderLayout());
		
		// Init components
		this.tabbedTextEditorPanel = new TabbedTextEditorPanel();
		
		// Add default panel
		this.tabbedTextEditorPanel.addDefaultTextEditor();
		
		// Add components
		add(this.tabbedTextEditorPanel, BorderLayout.CENTER);
	}
}

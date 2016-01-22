package gui.tool;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class ToolBarPanel extends JPanel {
	
	private static final long serialVersionUID = -8385951480562168262L;

	// Components
	private ToolVButton newFileBtn, runBtn;
	
	public ToolBarPanel() {

		// Set layout
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        // Init components
        newFileBtn = new ToolVButton("New File", new ImageIcon(getClass().getResource("/images/top_menu/new_file.png")));
        runBtn = new ToolVButton("Run", new ImageIcon(getClass().getResource("/images/top_menu/run.png")));
        
        // Add components
        int spacing = 20;
        this.add(Box.createRigidArea(new Dimension(10, 0)));
        this.add(this.newFileBtn);
        this.add(Box.createRigidArea(new Dimension(spacing, 0)));
        this.add(this.runBtn);
        
        this.setPreferredSize(new Dimension(0, 80)); // Size of the JPanel (Width is automatically set)
	}
}

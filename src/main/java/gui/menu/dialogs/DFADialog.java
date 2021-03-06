package gui.menu.dialogs;

import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

public class DFADialog extends JDialog {

	private static final long serialVersionUID = 4722229452456233559L;

	public DFADialog(JFrame parent) {
		
		// Set title
		setTitle("DFA");
		
		// Init components
		JLabel dfaLabel = new JLabel(new ImageIcon(getClass().getResource("/images/dfa/DFA.jpg")));
		JScrollPane dfaScrollPane = new JScrollPane(dfaLabel);
		
		// Config scroll
		dfaScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		
		// Add components
		add(dfaScrollPane);
		
		// Config dialog
		setSize(new Dimension((int) (parent.getWidth()*0.9), (int) (parent.getHeight()*0.9)));
		setLocationRelativeTo(parent);
		setVisible(true);
	}
}

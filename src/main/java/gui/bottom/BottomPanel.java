package gui.bottom;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class BottomPanel extends JPanel {

	private static final long serialVersionUID = -8198154848229580550L;
	
	// Compoentns
	private JLabel compilerMessage;
	
	// Style
	public enum Style {
		ERROR,
		WARNING,
		SUCCESS,
		NORMAL
	}
	
	// Colors
	Color LIGHT_RED = new Color(254, 228, 227);
	Color DARK_RED = new Color(138, 33, 12);
	Color LIGHT_GREEN = new Color(234, 255, 216);
	Color DARK_GREEN = new Color(141, 166, 49);
	Color LIGHT_YELLOW = new Color(255, 251, 219);
	Color DARK_YELLOW = new Color(251, 198, 132);
	
	
	public BottomPanel() {
		
		// Layout
		setLayout(new BorderLayout());
		
		// Init components
		this.compilerMessage = new JLabel("Please enter your code and press 'Run'");
		
		// Configure compoentns
		this.compilerMessage.setOpaque(true);
		this.compilerMessage.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		// Add components
		add(compilerMessage, BorderLayout.CENTER);
	}
	
	/**
	 * Set compiler message text
	 * @param text
	 */
	public void setCompilerMessageText(String text) {
		this.compilerMessage.setText(text);
	}
	
	/**
	 * Set style
	 * @param style
	 */
	public void setStyle(Style style) {
		switch(style) {
		case ERROR:
			this.compilerMessage.setBackground(LIGHT_RED);
			this.compilerMessage.setForeground(DARK_RED);
			break;
		case SUCCESS:
			this.compilerMessage.setBackground(LIGHT_GREEN);
			this.compilerMessage.setForeground(DARK_GREEN);
			break;
		case WARNING:
			this.compilerMessage.setBackground(LIGHT_YELLOW);
			this.compilerMessage.setForeground(DARK_YELLOW);
			break;
		case NORMAL:
			this.compilerMessage.setBackground(null);
			this.compilerMessage.setForeground(Color.BLACK);
			break;
		}
	}
}

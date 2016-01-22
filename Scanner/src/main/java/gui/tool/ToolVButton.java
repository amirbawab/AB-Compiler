package gui.tool;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.SwingConstants;

public class ToolVButton extends JButton{
	
	private static final long serialVersionUID = 6824107648856257001L;

	public ToolVButton(String title, Icon icon) {
		super(title, icon);
		setContentAreaFilled(false);
        setBorder(null);
        setVerticalTextPosition(SwingConstants.BOTTOM);
        setHorizontalTextPosition(SwingConstants.CENTER);
        setText(title);
        setToolTipText(title);
	}
}

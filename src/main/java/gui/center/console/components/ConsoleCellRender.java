package gui.center.console.components;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class ConsoleCellRender extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1744124296631670118L;

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel c = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        c.setToolTipText(c.getText());
        return c;
    }
}
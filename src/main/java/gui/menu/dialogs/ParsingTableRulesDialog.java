package gui.menu.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class ParsingTableRulesDialog extends JDialog {

	private static final long serialVersionUID = -1965488402878339634L;

	public ParsingTableRulesDialog(JFrame parent, Object[][] data) {
		
		// Set title
		setTitle("Parsing table rules");
				
		// Set layout
		setLayout(new BorderLayout());
		
		// Data
		Object[] header = {"Rule", "Production"};
		
		// Init components
		ParsingTableRulesModel tableModel = new ParsingTableRulesModel(data, header);
		JTable table = new JTable(tableModel);
		
		// Config table
		table.getColumnModel().getColumn(0).setPreferredWidth(50);
		
		// Scroll
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		
		// Add components
		add(scrollPane, BorderLayout.CENTER);
		
		// Config dialog
		setSize(new Dimension((int) (parent.getWidth()*0.3), (int) (parent.getHeight()*0.7)));
		setLocationRelativeTo(parent);
		setVisible(true);
	}
	
	private class ParsingTableRulesModel extends DefaultTableModel {

		private static final long serialVersionUID = -8047273053227581642L;

		public ParsingTableRulesModel(Object[][] data, Object[] cols) {
			super(data, cols);
		}
		
		public boolean isCellEditable(int row, int column){  
	          return false;  
	    }
	}
}

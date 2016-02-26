package gui.menu.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class StateTableInfoDialog extends JDialog {

	private static final long serialVersionUID = -1965488402878339634L;

	public StateTableInfoDialog(JFrame parent) {
		
		// Set title
		setTitle("State transition table information");
				
		// Set layout
		setLayout(new BorderLayout());
		
		// Data
		Object[] header = {"Letter", "Meaning"};
		Object[][] data = {
				{"O", "Other"},
				{"N", "Non-zero (1-9)"},
				{"S", "White space (space, tab etc...)"},
				{"L", "Letter (a-z or A-Z)"},
				{"E", "End of line"},
				{"F", "End of file"},
				{"<character>", "Appearance of <character>"}
				
		};
		
		// Init components
		TextTableModel stateTableInfoModel = new TextTableModel(data, header);
		JTable stateTableInfo = new JTable(stateTableInfoModel);
		
		// Config table
		stateTableInfo.getColumnModel().getColumn(1).setPreferredWidth(200);
		
		// Scroll
		JScrollPane stateScrollPane = new JScrollPane(stateTableInfo);
		stateScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		
		// Add components
		add(stateScrollPane, BorderLayout.CENTER);
		
		// Config dialog
		setSize(new Dimension((int) (parent.getWidth()*0.3), (int) (parent.getHeight()*0.2)));
		setLocationRelativeTo(parent);
		setVisible(true);
	}
	
	private class TextTableModel extends DefaultTableModel {

		private static final long serialVersionUID = -8047273053227581642L;

		public TextTableModel(Object[][] data, Object[] cols) {
			super(data, cols);
		}
		
		public boolean isCellEditable(int row, int column){  
	          return false;  
	    }
	}
}

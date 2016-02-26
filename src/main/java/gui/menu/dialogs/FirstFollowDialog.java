package gui.menu.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class FirstFollowDialog extends JDialog {

	private static final long serialVersionUID = -1965488402878339634L;

	public FirstFollowDialog(JFrame parent, Object[][] data) {
		
		// Set title
		setTitle("First and Follow sets");
				
		// Set layout
		setLayout(new BorderLayout());
		
		// Data
		Object[] header = {"Non terminal", "First set", "Follow set"};
		
		// Init components
		FirstFollowModel firstFollowModel = new FirstFollowModel(data, header);
		JTable firstFollowTable = new JTable(firstFollowModel);
		
		// Scroll
		JScrollPane firstFollowScrollPane = new JScrollPane(firstFollowTable);
		firstFollowScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		
		// Add components
		add(firstFollowScrollPane, BorderLayout.CENTER);
		
		// Config dialog
		setSize(new Dimension((int) (parent.getWidth()*0.5), (int) (parent.getHeight()*0.5)));
		setLocationRelativeTo(parent);
		setVisible(true);
	}
	
	private class FirstFollowModel extends DefaultTableModel {

		private static final long serialVersionUID = -8047273053227581642L;

		public FirstFollowModel(Object[][] data, Object[] cols) {
			super(data, cols);
		}
		
		public boolean isCellEditable(int row, int column){  
	          return false;  
	    }
	}
}

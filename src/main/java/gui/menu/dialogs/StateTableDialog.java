package gui.menu.dialogs;

import gui.MainFrame;

import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class StateTableDialog extends JDialog {

	private static final long serialVersionUID = -7402407159765350532L;
	
	public StateTableDialog(JFrame parent, Object[][] stateTableData) {
		
		// Init components
		StateTableModel stateTableModel = new StateTableModel(stateTableData, new Object[stateTableData[0].length]);
		JTable stateTable = new JTable(stateTableModel);
		
		// Config table
		stateTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
		stateTable.getColumnModel().getColumn(stateTableData[0].length-1).setPreferredWidth(200);
		
		// Scroll
		JScrollPane stateScrollPane = new JScrollPane(stateTable);
		stateScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		
		// Add components
		add(stateScrollPane);
		
		// Config dialog
		setSize(new Dimension((int) (parent.getWidth()*0.9), (int) (parent.getHeight()*0.9)));
		setLocationRelativeTo(parent);
		setVisible(true);
	}
	
	private class StateTableModel extends DefaultTableModel {

		private static final long serialVersionUID = -1178640675013081336L;

		public StateTableModel(Object[][] data, Object[] cols) {
			super(data, cols);
		}
		
		public boolean isCellEditable(int row, int column){  
	          return false;  
	    }
	}
}

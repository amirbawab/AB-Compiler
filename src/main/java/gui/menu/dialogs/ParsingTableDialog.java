package gui.menu.dialogs;

import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class ParsingTableDialog extends JDialog {

	private static final long serialVersionUID = -7402407159765350532L;
	
	public ParsingTableDialog(JFrame parent, Object[][] parsingTableData) {
		
		// Set title
		setTitle("Parsing table");
		
		// Init components
		ParsingTableModel stateTableModel = new ParsingTableModel(parsingTableData, new Object[parsingTableData[0].length]);
		JTable stateTable = new JTable(stateTableModel);
		
		// Config table
		stateTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
		
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
	
	private class ParsingTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 2928330731470579769L;

		public ParsingTableModel(Object[][] data, Object[] cols) {
			super(data, cols);
		}
		
		public boolean isCellEditable(int row, int column){  
	          return false;  
	    }
	}
}

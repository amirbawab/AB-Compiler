package gui.center.console.components;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class ConsoleTable extends JTable {

	private static final long serialVersionUID = -6976897050297294281L;

	public ConsoleTable(Object[][] data, Object[] header) {
		setModel(new ConsoleTableModel(data, header));
		getTableHeader().setReorderingAllowed(false);
	}
	
	/**
	 * Add row to table
	 * @param data
	 */
	public void addRow(Object[] data) {
		ConsoleTableModel model = (ConsoleTableModel) getModel();
		model.addRow(data);
	}
	
	/**
	 * Clear all rows
	 */
	public void clearAll() {
		ConsoleTableModel model = (ConsoleTableModel) getModel();
		int rowCount = model.getRowCount();
		for(int i=0; i<rowCount; i++)
			model.removeRow(0);
	}
	
	/**
	 * Table custom model
	 */
	class ConsoleTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 7902134137847991346L;

		/**
		 * Create model
		 * @param data
		 * @param header
		 */
		public ConsoleTableModel(Object[][] data, Object[] header) {
			super(data, header);
		}
		
		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}
}

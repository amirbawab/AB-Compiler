package gui.center.console.components;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class ConsoleTable extends JTable {

	private static final long serialVersionUID = -6976897050297294281L;

	public ConsoleTable(Object[][] data, Object[] header) {
		setModel(new ConsoleTableModel(data, header));
	}
	
	class ConsoleTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 7902134137847991346L;

		public ConsoleTableModel(Object[][] data, Object[] header) {
			super(data, header);
		}
	}
}

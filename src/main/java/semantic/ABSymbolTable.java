package semantic;

import scanner.ABToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amir on 3/25/2016.
 */
public class ABSymbolTable {
    private List<ABSymbolTableEntry> rows;
    private String name;
    private int id;

    /**
     * Constructor
     */
    public ABSymbolTable(String name) {
        this.name = name;
        this.rows = new ArrayList<>();
    }

    public List<ABSymbolTableEntry> getRows() {
        return rows;
    }

    public void addRow(ABSymbolTableEntry entry) { this.rows.add(entry); }
    public String getName() {
        return name;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public Object[][] getTableData() {
        Object[][] tableData = new Object[rows.size()][5];

        for(int i=0; i < tableData.length; i++) {
            ABSymbolTableEntry entry = rows.get(i);

            String entryName = entry.getName();
            String entryKind = entry.getKind().getName();
            String entryType = entry.getTypeAsString();
            String entryParams = entry.getParametersAsString();
            int entryLink = entry.getLink() == null ? -1 : entry.getLink().getId();

            tableData[i] = new Object[]{entryName, entryKind, entryType, entryParams, entryLink};
        }
        return tableData;
    }

    /**
     * Print table
     * @return
     */
    public String toString() {
        String result = "\n======================\n";
        result += "Symbol table: " + name;
        for(ABSymbolTableEntry entry : rows)
            result += "\n" + entry;
        result += "\n====================\n";
        return result;
    }
}

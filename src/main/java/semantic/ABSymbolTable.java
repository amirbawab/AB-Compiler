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
        Object[][] tableData = new Object[rows.size()][7];

        for(int i=0; i < tableData.length; i++) {
            ABSymbolTableEntry entry = rows.get(i);

            String entryName = entry.getName();
            String entryKind = entry.getKindAsString();
            String entryStructure = entry.getStructure();
            String entryType = entry.getTypeAsString();
            String entryParams = entry.getParametersAsString();
            int entryAddress = entry.getAddress();
            String entryProperlyDefined = entry.isProperlyDefined() ? "Yes" : "No";
            int entryLink = entry.getLink() == null ? -1 : entry.getLink().getId();

            tableData[i] = new Object[]{entryName, entryKind, entryStructure, entryType, entryParams, entryProperlyDefined,entryAddress, entryLink};
        }
        return tableData;
    }

    /**
     * Check if entry exist
     * @param name
     * @return
     */
    public ABSymbolTableEntry getEntry(String name) {
        return getEntry(name, ABSymbolTableEntry.Kind.ANY);
    }

    /**
     * Check if entry exist
     * @param name
     * @param kind
     * @return
     */
    public ABSymbolTableEntry getEntry(String name, ABSymbolTableEntry.Kind kind) {
        for(ABSymbolTableEntry entry : rows)
            if (entry.getName().equals(name) && (kind == ABSymbolTableEntry.Kind.ANY || entry.getKind() == kind))
                return entry;
        return null;
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

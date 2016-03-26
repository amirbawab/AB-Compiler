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

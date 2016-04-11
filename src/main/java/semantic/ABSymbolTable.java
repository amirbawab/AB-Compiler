package semantic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amir on 3/25/2016.
 */
public class ABSymbolTable {
    private List<ABSymbolTableEntry> rows;
    private String name;
    private String simpleName;
    private ABSymbolTableEntry.Kind kind;
    private int id;
    private int sizeInBytes = Integer.MIN_VALUE;

    /**
     * Constructor
     */
    public ABSymbolTable(String name, String simpleName, ABSymbolTableEntry.Kind kind) {
        this.name = name;
        this.kind = kind;
        this.simpleName = simpleName;
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
        Object[][] tableData = new Object[rows.size()][];

        for(int i=0; i < tableData.length; i++) {
            ABSymbolTableEntry entry = rows.get(i);

            String entryName = entry.getName();
            String entryKind = entry.getKindAsString();
            String entryStructure = entry.getStructure();
            String entryType = entry.getTypeAsString();
            String entryParams = entry.getParametersAsString();
            int entryAddress = entry.getAddress();
            int sizeInByte = entry.getSizeInBytes();
            String sizeInByteString = sizeInByte == Integer.MIN_VALUE ? "???": sizeInByte+"";
            String entryProperlyDefined = entry.isProperlyDefined() ? "Yes" : "No";
            int entryLink = entry.getLink() == null ? -1 : entry.getLink().getId();
            String entryLabel = entry.getLabel() == null ? "???" : entry.getLabel();

            tableData[i] = new Object[]{entryName, entryKind, entryStructure, entryType, entryParams, entryProperlyDefined,entryAddress, sizeInByteString, entryLink};
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
     * Get all entries with specific name and type
     * @param name
     * @return
     */
    public List<ABSymbolTableEntry> getEntries(String name) {
        return getEntries(name, ABSymbolTableEntry.Kind.ANY);
    }

    /**
     * Get all entries with specific name and type
     * @param name
     * @param kind
     * @return
     */
    public List<ABSymbolTableEntry> getEntries(String name, ABSymbolTableEntry.Kind kind) {
        List<ABSymbolTableEntry> entries = new ArrayList<>();
        for(ABSymbolTableEntry entry : rows)
            if (entry.getName().equals(name) && (kind == ABSymbolTableEntry.Kind.ANY || entry.getKind() == kind))
                entries.add(entry);
        return entries;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    public ABSymbolTableEntry.Kind getKind() {
        return kind;
    }

    public void setKind(ABSymbolTableEntry.Kind kind) {
        this.kind = kind;
    }

    public int getSizeInBytes() {
        return sizeInBytes;
    }

    public void setSizeInBytes(int sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
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

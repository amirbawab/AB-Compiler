package semantic;

/**
 * Created by Amir on 3/25/2016.
 */
public class ABSymbolTableEntryFactory {

    public static ABSymbolTableEntry createFunctionEntry(ABSymbolTable table, String name) {
        ABSymbolTableEntry entry = new ABSymbolTableEntry(table, name, ABSymbolTableEntry.Kind.FUNCTION);
        entry.setLink(new ABSymbolTable(table.getName() + " > Function " + name, name, ABSymbolTableEntry.Kind.FUNCTION));
        return entry;
    }

    public static ABSymbolTableEntry createClassEntry(ABSymbolTable table, String name) {
        ABSymbolTableEntry entry = new ABSymbolTableEntry(table, name, ABSymbolTableEntry.Kind.CLASS);
        entry.setLink(new ABSymbolTable(table.getName() + " > Class " + name, name, ABSymbolTableEntry.Kind.CLASS));
        return entry;
    }

    public static ABSymbolTableEntry createParameterEntry(ABSymbolTable table, String name) {
        ABSymbolTableEntry entry = new ABSymbolTableEntry(table, name, ABSymbolTableEntry.Kind.PARAMETER);
        return entry;
    }

    public static ABSymbolTableEntry createVariableEntry(ABSymbolTable table, String name) {
        ABSymbolTableEntry entry = new ABSymbolTableEntry(table, name, ABSymbolTableEntry.Kind.VARIABLE);
        return entry;
    }

    public static ABSymbolTableEntry createProgramEntry(ABSymbolTable table, String name) {
        ABSymbolTableEntry entry = new ABSymbolTableEntry(table, name, ABSymbolTableEntry.Kind.PROGRAM);
        entry.setLink(new ABSymbolTable(table.getName() + " > " + name, name, ABSymbolTableEntry.Kind.PROGRAM));
        return entry;
    }

    public static ABSymbolTableEntry createForEntry(ABSymbolTable table, String name) {
        ABSymbolTableEntry entry = new ABSymbolTableEntry(table, name, ABSymbolTableEntry.Kind.FOR);
        entry.setLink(new ABSymbolTable(table.getName() + " > " + name, name, ABSymbolTableEntry.Kind.FOR));
        return entry;
    }
}

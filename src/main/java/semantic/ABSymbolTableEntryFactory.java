package semantic;

/**
 * Created by Amir on 3/25/2016.
 */
public class ABSymbolTableEntryFactory {

    public static ABSymbolTableEntry createFunctionEntry(String name) {
        ABSymbolTableEntry entry = new ABSymbolTableEntry(name, ABSymbolTableEntry.Kind.FUNCTION);
        entry.setLink(new ABSymbolTable(name));
        return entry;
    }

    public static ABSymbolTableEntry createClassEntry(String name) {
        ABSymbolTableEntry entry = new ABSymbolTableEntry(name, ABSymbolTableEntry.Kind.CLASS);
        entry.setLink(new ABSymbolTable(name));
        return entry;
    }

    public static ABSymbolTableEntry createParameterEntry(String name) {
        ABSymbolTableEntry entry = new ABSymbolTableEntry(name, ABSymbolTableEntry.Kind.PARAMETER);
        return entry;
    }

    public static ABSymbolTableEntry createVariableEntry(String name) {
        ABSymbolTableEntry entry = new ABSymbolTableEntry(name, ABSymbolTableEntry.Kind.VARIABLE);
        return entry;
    }
}

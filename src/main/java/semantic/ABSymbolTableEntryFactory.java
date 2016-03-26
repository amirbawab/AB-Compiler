package semantic;

/**
 * Created by Amir on 3/25/2016.
 */
public class ABSymbolTableEntryFactory {

    public static ABSymbolTableEntry createFunctionEntry(String name, String parent) {
        ABSymbolTableEntry entry = new ABSymbolTableEntry(name, ABSymbolTableEntry.Kind.FUNCTION);
        entry.setLink(new ABSymbolTable(parent + " > Function " + name));
        return entry;
    }

    public static ABSymbolTableEntry createClassEntry(String name, String parent) {
        ABSymbolTableEntry entry = new ABSymbolTableEntry(name, ABSymbolTableEntry.Kind.CLASS);
        entry.setLink(new ABSymbolTable(parent + " > Class " + name));
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

    public static ABSymbolTableEntry createProgramEntry(String name, String parent) {
        ABSymbolTableEntry entry = new ABSymbolTableEntry(name, ABSymbolTableEntry.Kind.PROGRAM);
        entry.setLink(new ABSymbolTable(parent + " > " + name));
        return entry;
    }
}

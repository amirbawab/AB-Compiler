package semantic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import parser.grammar.ABGrammarToken;
import scanner.ABToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by Amir on 3/26/2016.
 */
public class ABSemantic {

    // Logger
    private Logger l = LogManager.getFormatterLogger(getClass());

    private ABSymbolTable globalTable;
    private Stack<ABSymbolTable> tables;

    // Buffers
    List<ABToken> type_buffer;

    public enum Type {
        CREATE_GLOBAL_TABLE("createGlobalTable"),
        CREATE_CLASS_TABLE_AND_ENTRY("createClassTableAndEntry"),
        PARENT("parent"),
        TYPE("type"),
        CREATE_VAR_ENTRY("createVarEntry"),
        CREATE_PARAM_ENTRY("createParamEntry"),
        CREATE_FUNCTION_ENTRY_AND_TABLE("createFunctionEntryAndTable"),
        CREATE_PROGRAM_ENTRY_AND_TABLE("createProgramEntryAndTable"),
        MORE_TYPE("moreType")
        ;

        private String name;
        Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public ABSemantic() {
        tables = new Stack<>();
    }

    public void eval(ABGrammarToken token, List<ABToken> tokens, int tokenIndex) {
        if(token.getValue().equals(Type.CREATE_GLOBAL_TABLE.getName())) {

            // Create global table
            globalTable = new ABSymbolTable("Global");

            // Push global to stack
            tables.push(globalTable);

        } else if(token.getValue().equals(Type.CREATE_CLASS_TABLE_AND_ENTRY.getName())) {

            // Create class entry
            ABSymbolTableEntry entry = ABSymbolTableEntryFactory.createClassEntry(tokens.get(tokenIndex-1).getValue());
            tables.peek().addRow(entry);

            // Push class to stack
            tables.push(entry.getLink());

        } else if(token.getValue().equals(Type.PARENT.getName())) {

            // Pop the table
            tables.pop();

        } else if(token.getValue().equals(Type.TYPE.getName())) {

            // Put token
            type_buffer = new ArrayList<>();
            type_buffer.add(tokens.get(tokenIndex-1));

        } else if(token.getValue().equals(Type.CREATE_VAR_ENTRY.getName())) {

            // Create variable entry
            ABSymbolTableEntry entry = ABSymbolTableEntryFactory.createVariableEntry(tokens.get(tokenIndex-1).getValue());
            entry.setType(type_buffer);
            tables.peek().addRow(entry);

        } else if(token.getValue().equals(Type.MORE_TYPE.getName())) {

            // Add more type
            type_buffer.add(tokens.get(tokenIndex-1));

        } else if(token.getValue().equals(Type.CREATE_PARAM_ENTRY.getName())) {

            // Create class entry
            ABSymbolTableEntry entry = ABSymbolTableEntryFactory.createParameterEntry(tokens.get(tokenIndex-1).getValue());
            tables.peek().addRow(entry);

        } else if(token.getValue().equals(Type.CREATE_FUNCTION_ENTRY_AND_TABLE.getName())) {

            // Create class entry
            ABSymbolTableEntry entry = ABSymbolTableEntryFactory.createFunctionEntry(tokens.get(tokenIndex-1).getValue());
            tables.peek().addRow(entry);

            // Push class to stack
            tables.push(entry.getLink());

        } else if(token.getValue().equals(Type.CREATE_PROGRAM_ENTRY_AND_TABLE.getName())) {

            // Create class entry
            ABSymbolTableEntry entry = ABSymbolTableEntryFactory.createProgramEntry(tokens.get(tokenIndex-1).getValue());
            tables.peek().addRow(entry);

            // Push class to stack
            tables.push(entry.getLink());

        } else {
            l.error("Action token: %s not found!", token.getValue());
        }
    }

    public ABSymbolTable getGlobalTable() {
        return globalTable;
    }

    public void setGlobalTable(ABSymbolTable globalTable) {
        this.globalTable = globalTable;
    }

    public Stack<ABSymbolTable> getTables() {
        return tables;
    }

    public void setTables(Stack<ABSymbolTable> tables) {
        this.tables = tables;
    }


    // TODO Remove the methods below

    public void printTables() {
        printTables(globalTable);
    }

    private void printTables(ABSymbolTable table) {
        if(table != null) {
            System.out.print(table);
            for (ABSymbolTableEntry entry : table.getRows()) {
                printTables(entry.getLink());
            }
        }
    }
}

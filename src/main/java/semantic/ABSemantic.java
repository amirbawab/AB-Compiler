package semantic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import parser.grammar.ABGrammarToken;
import scanner.ABToken;
import semantic.helper.ABSemanticMessageHelper;

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
    private List<ABSymbolTable> allTables;
    private Stack<ABSymbolTable> tablesStack;
    private List<ABSemanticError> errors;

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
        tablesStack = new Stack<>();
        allTables = new ArrayList<>();
        errors = new ArrayList<>();
    }

    /**
     * Evaluate action token
     * @param token
     * @param tokens
     * @param tokenIndex
     */
    public void eval(ABGrammarToken token, List<ABToken> tokens, int tokenIndex) {
        if(token.getValue().equals(Type.CREATE_GLOBAL_TABLE.getName())) {

            // Create global table
            globalTable = new ABSymbolTable("Global");

            // Push global to stack
            globalTable.setId(allTables.size());
            allTables.add(globalTable);
            tablesStack.push(globalTable);

        } else if(token.getValue().equals(Type.CREATE_CLASS_TABLE_AND_ENTRY.getName())) {

            // Input token
            ABToken inputToken = tokens.get(tokenIndex-1);
            ABSymbolTableEntry definedEntry = searchEntryLocally(inputToken.getValue());

            // If exists already
            if(definedEntry != null)
                addError(inputToken, String.format(ABSemanticMessageHelper.MUTLIPLE_DECLARATION, inputToken.getValue(), definedEntry.getToken().getRow(), definedEntry.getToken().getCol(), inputToken.getRow(), inputToken.getCol()));

            // Create class entry
            ABSymbolTableEntry entry = ABSymbolTableEntryFactory.createClassEntry(inputToken.getValue(), tablesStack.peek().getName());
            entry.setToken(inputToken);
            tablesStack.peek().addRow(entry);

            // Push class to stack
            entry.getLink().setId(allTables.size());
            allTables.add(entry.getLink());
            tablesStack.push(entry.getLink());

        } else if(token.getValue().equals(Type.PARENT.getName())) {

            // Pop the table
            tablesStack.pop();

        } else if(token.getValue().equals(Type.TYPE.getName())) {

            // Put token
            type_buffer = new ArrayList<>();
            type_buffer.add(tokens.get(tokenIndex-1));

        } else if(token.getValue().equals(Type.CREATE_VAR_ENTRY.getName())) {

            // Check if already defined
            ABToken inputToken = tokens.get(tokenIndex-1);
            ABSymbolTableEntry definedEntry = searchEntry(inputToken.getValue());

            // If exists already
            if(definedEntry != null)
                addError(inputToken, String.format(ABSemanticMessageHelper.MUTLIPLE_DECLARATION, inputToken.getValue(), definedEntry.getToken().getRow(), definedEntry.getToken().getCol(), inputToken.getRow(), inputToken.getCol()));

            // Create variable entry
            ABSymbolTableEntry entry = ABSymbolTableEntryFactory.createVariableEntry(inputToken.getValue());
            entry.setType(type_buffer);
            entry.setToken(inputToken);
            tablesStack.peek().addRow(entry);

        } else if(token.getValue().equals(Type.MORE_TYPE.getName())) {

            // Add more type
            type_buffer.add(tokens.get(tokenIndex-1));

        } else if(token.getValue().equals(Type.CREATE_PARAM_ENTRY.getName())) {

            // Input token
            ABToken inputToken = tokens.get(tokenIndex-1);
            ABSymbolTableEntry definedEntry = searchEntryLocally(inputToken.getValue());

            // If exists already
            if(definedEntry != null)
                addError(inputToken, String.format(ABSemanticMessageHelper.MUTLIPLE_DECLARATION, inputToken.getValue(), definedEntry.getToken().getRow(), definedEntry.getToken().getCol(), inputToken.getRow(), inputToken.getCol()));

            // Create class entry
            ABSymbolTableEntry entry = ABSymbolTableEntryFactory.createParameterEntry(inputToken.getValue());
            entry.setType(type_buffer);
            entry.setToken(inputToken);
            tablesStack.peek().addRow(entry);

        } else if(token.getValue().equals(Type.CREATE_FUNCTION_ENTRY_AND_TABLE.getName())) {

            // Input token
            ABToken inputToken = tokens.get(tokenIndex-1);
            ABSymbolTableEntry definedEntry = searchEntryLocally(inputToken.getValue());

            // If exists already
            if(definedEntry != null)
                addError(inputToken, String.format(ABSemanticMessageHelper.MUTLIPLE_DECLARATION, inputToken.getValue(), definedEntry.getToken().getRow(), definedEntry.getToken().getCol(), inputToken.getRow(), inputToken.getCol()));

            // Create class entry
            ABSymbolTableEntry entry = ABSymbolTableEntryFactory.createFunctionEntry(inputToken.getValue(), tablesStack.peek().getName());
            entry.setType(type_buffer);
            entry.setToken(inputToken);
            tablesStack.peek().addRow(entry);

            // Push class to stack
            entry.getLink().setId(allTables.size());
            allTables.add(entry.getLink());
            tablesStack.push(entry.getLink());

        } else if(token.getValue().equals(Type.CREATE_PROGRAM_ENTRY_AND_TABLE.getName())) {

            // Input token
            ABToken inputToken = tokens.get(tokenIndex-1);

            // Create class entry
            ABSymbolTableEntry entry = ABSymbolTableEntryFactory.createProgramEntry(inputToken.getValue(), tablesStack.peek().getName());
            entry.setToken(inputToken);
            tablesStack.peek().addRow(entry);

            // Push class to stack
            entry.getLink().setId(allTables.size());
            allTables.add(entry.getLink());
            tablesStack.push(entry.getLink());

        } else {
            l.error("Action token: %s not found!", token.getValue());
        }
    }

    /**
     * Search for an entry locally
     * @param name
     * @return
     */
    public ABSymbolTableEntry searchEntryLocally(String name) {
        return tablesStack.peek().getEntry(name);
    }

    /**
     * Search for an entry
     * @param name
     * @return
     */
    public ABSymbolTableEntry searchEntry(String name) {
        Stack<ABSymbolTable> closestTables = new Stack<>();
        ABSymbolTableEntry found = null;

        // Search starting from current table
        while(found == null && !tablesStack.isEmpty()) {

            // Entry
            ABSymbolTableEntry entry = tablesStack.peek().getEntry(name);

            // If found
            if(entry != null)
                found = entry;
            else
                closestTables.push(tablesStack.pop());
        }

        // Put all tables back into stack
        while(!closestTables.isEmpty())
            tablesStack.push(closestTables.pop());
        return found;
    }

    public ABSymbolTable getGlobalTable() {
        return globalTable;
    }

    public void setGlobalTable(ABSymbolTable globalTable) {
        this.globalTable = globalTable;
    }

    public Stack<ABSymbolTable> getTablesStack() {
        return tablesStack;
    }

    public void setTablesStack(Stack<ABSymbolTable> tablesStack) {
        this.tablesStack = tablesStack;
    }

    public List<ABSymbolTable> getAllTables() {
        return allTables;
    }

    public List<ABSemanticError> getErrors() {
        return errors;
    }

    private void addError(ABToken token, String message) {
        errors.add(new ABSemanticError(message, token));
    }

    /**
     * Get symbol tables
     * @return
     */
    public String tablesToString() {
        return tablesToString(globalTable);
    }

    private String tablesToString(ABSymbolTable table) {
        String tableStr = "";
        if(table != null) {
            tableStr = table.toString();
            for (ABSymbolTableEntry entry : table.getRows()) {
                tableStr += tablesToString(entry.getLink());
            }
        }
        return tableStr;
    }

    public class ABSemanticError {
        private String message;
        private ABToken token;

        public ABSemanticError(String message, ABToken token) {
            this.message = message;
            this.token = token;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public ABToken getToken() {
            return token;
        }

        public void setToken(ABToken token) {
            this.token = token;
        }

        public String toString() {
            return message;
        }
    }
}

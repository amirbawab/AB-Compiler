package semantic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import parser.grammar.ABGrammarToken;
import scanner.ABToken;
import scanner.helper.ABTokenHelper;
import semantic.helper.ABSemanticMessageHelper;

import java.util.*;

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
    private Queue<ABSymbolTableEntry> phaseTwoEntry;
    private Queue<ABSemanticFunctionCall> phaseTwoFunctions;

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
        MORE_TYPE("moreType"),
        USE_VAR("useVar"),
        USE_FUNCTION("useFunction")
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
        phaseTwoEntry = new LinkedList<>();
        phaseTwoFunctions = new LinkedList<>();
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
            globalTable = new ABSymbolTable("Global", "Global", ABSymbolTableEntry.Kind.GLOBAL);

            // Push global to stack
            globalTable.setId(allTables.size());
            allTables.add(globalTable);
            tablesStack.push(globalTable);

        } else if(token.getValue().equals(Type.CREATE_CLASS_TABLE_AND_ENTRY.getName())) {

            // Input token
            ABToken inputToken = tokens.get(tokenIndex-1);
            ABSymbolTableEntry definedEntry = searchEntry(tablesStack, inputToken.getValue());

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
            ABSymbolTableEntry definedEntry = searchEntry(tablesStack, inputToken.getValue());

            // If exists already
            if(definedEntry != null)
                addError(inputToken, String.format(ABSemanticMessageHelper.MUTLIPLE_DECLARATION, inputToken.getValue(), definedEntry.getToken().getRow(), definedEntry.getToken().getCol(), inputToken.getRow(), inputToken.getCol()));

            // Create variable entry
            ABSymbolTableEntry entry = ABSymbolTableEntryFactory.createVariableEntry(inputToken.getValue());
            entry.setType(type_buffer);
            entry.setToken(inputToken);
            tablesStack.peek().addRow(entry);

            // Push for phase 2 verification if the type is not primitive
            if(entry.getType().get(0).getToken().equals(ABTokenHelper.T_IDENTIFIER))
                phaseTwoEntry.offer(entry);

        } else if(token.getValue().equals(Type.MORE_TYPE.getName())) {

            // Add more type
            type_buffer.add(tokens.get(tokenIndex-1));

        } else if(token.getValue().equals(Type.CREATE_PARAM_ENTRY.getName())) {

            // Input token
            ABToken inputToken = tokens.get(tokenIndex-1);
            ABSymbolTableEntry definedEntry = searchEntry(tablesStack, inputToken.getValue());

            // If exists already
            if(definedEntry != null)
                addError(inputToken, String.format(ABSemanticMessageHelper.MUTLIPLE_DECLARATION, inputToken.getValue(), definedEntry.getToken().getRow(), definedEntry.getToken().getCol(), inputToken.getRow(), inputToken.getCol()));

            // Create class entry
            ABSymbolTableEntry entry = ABSymbolTableEntryFactory.createParameterEntry(inputToken.getValue());
            entry.setType(type_buffer);
            entry.setToken(inputToken);
            tablesStack.peek().addRow(entry);

            // Push for phase 2 verification if the type is not primitive
            if(entry.getType().get(0).getToken().equals(ABTokenHelper.T_IDENTIFIER))
                phaseTwoEntry.offer(entry);

        } else if(token.getValue().equals(Type.CREATE_FUNCTION_ENTRY_AND_TABLE.getName())) {

            // Input token
            ABToken inputToken = tokens.get(tokenIndex-1);
            ABSymbolTableEntry definedEntry = searchEntry(tablesStack, inputToken.getValue());

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

            // Push for phase 2 verification if the type is not primitive
            if(entry.getType().get(0).getToken().equals(ABTokenHelper.T_IDENTIFIER))
                phaseTwoEntry.offer(entry);

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

        } else if(token.getValue().equals(Type.USE_VAR.getName())) {

            // Input token
            ABToken inputToken = tokens.get(tokenIndex-1);
            ABSymbolTableEntry definedVar = searchEntry(tablesStack, inputToken.getValue(), ABSymbolTableEntry.Kind.VARIABLE);
            ABSymbolTableEntry definedParam = searchEntry(tablesStack, inputToken.getValue(), ABSymbolTableEntry.Kind.PARAMETER);

            if(definedVar == null && definedParam == null)
                addError(inputToken, String.format(ABSemanticMessageHelper.UNDEFINED_VARIABLE, inputToken.getValue(), inputToken.getRow(), inputToken.getCol()));

        } else if(token.getValue().equals(Type.USE_FUNCTION.getName())) {

            // Input token
            ABToken inputToken = tokens.get(tokenIndex-1);
            ABSymbolTableEntry result = searchEntry(tablesStack, inputToken.getValue(), ABSymbolTableEntry.Kind.FUNCTION);

            // If function not found, then push for phase 2 verification
            if(result == null)
                phaseTwoFunctions.offer(new ABSemanticFunctionCall(inputToken, tablesStack));

        } else {
            l.error("Action token: %s not found!", token.getValue());
        }
    }

    /**
     * Evaluate the data again
     */
    public void evalPhaseTwo() {

        // Process variable and function types
        while(!phaseTwoEntry.isEmpty()) {
            ABSymbolTableEntry entry = phaseTwoEntry.poll();

            // Type
            ABToken type = entry.getType().get(0);

            // Search for the type
            ABSymbolTableEntry result = searchEntryInTable(globalTable, type.getValue(), ABSymbolTableEntry.Kind.CLASS);

            // If not found
            if(result == null) {
                entry.setProperlyDefined(false);
                addError(entry.getToken(), String.format(ABSemanticMessageHelper.UNDEFINED_TYPE, type.getValue(), type.getRow(), type.getCol()));
            }
        }

        // Process functions calls
        while(!phaseTwoFunctions.isEmpty()) {
            ABSemanticFunctionCall functionCall = phaseTwoFunctions.poll();

            // Search for the type
            ABSymbolTableEntry result = searchEntry(functionCall.getTableStack(), functionCall.getToken().getValue(), ABSymbolTableEntry.Kind.FUNCTION);

            // If not found
            if(result == null)
                addError(functionCall.getToken(), String.format(ABSemanticMessageHelper.UNDEFINED_FUNCTION, functionCall.getToken().getValue(), functionCall.getToken().getRow(), functionCall.getToken().getCol()));
        }
    }

    /**
     * Search in specific table
     * @param table
     * @param name
     * @param kind
     * @return
     */
    public ABSymbolTableEntry searchEntryInTable(ABSymbolTable table, String name, ABSymbolTableEntry.Kind kind) {
        return table.getEntry(name, kind);
    }

    /**
     * Search for an entry
     * @param name
     * @return
     */
    public ABSymbolTableEntry searchEntry(Stack<ABSymbolTable> tablesStack, String name) {
        return searchEntry(tablesStack, name, ABSymbolTableEntry.Kind.ANY);
    }

    /**
     * Search for an entry
     * @param name
     * @param kind
     * @return
     */
    public ABSymbolTableEntry searchEntry(Stack<ABSymbolTable> tablesStack, String name, ABSymbolTableEntry.Kind kind) {
        Stack<ABSymbolTable> closestTables = new Stack<>();
        ABSymbolTableEntry found = null;

        // Search starting from current table
        while(found == null && !tablesStack.isEmpty()) {

            // Entry
            ABSymbolTableEntry entry = tablesStack.peek().getEntry(name, kind);

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

    public class ABSemanticFunctionCall {
        private ABToken token;
        private Stack<ABSymbolTable> tableStack;

        public ABSemanticFunctionCall(ABToken token, Stack<ABSymbolTable> tableStack) {
            this.token = token;
            this.tableStack = (Stack<ABSymbolTable>) tableStack.clone();
        }

        public ABToken getToken() {
            return token;
        }

        public void setToken(ABToken token) {
            this.token = token;
        }

        public Stack<ABSymbolTable> getTableStack() {
            return tableStack;
        }

        public void setTableStack(Stack<ABSymbolTable> tableStack) {
            this.tableStack = tableStack;
        }
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

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

    // Tables
    private ABSymbolTable globalTable;
    private List<ABSymbolTable> allTables;
    private Stack<ABSymbolTable> tablesStack;

    // Error
    private List<ABSemanticError> errors;

    // Helper
    private Map<ABToken, ABSymbolTableEntry> tokenEntryMap;
    private Stack<ABSemanticTokenGroup> tokenGroupsStack;

    // Tokens list
    private List<ABToken> inputTokens;

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
        USE_FUNCTION("useFunction"),
        USE_VAR_BASED_ON_LAST_VAR("useVarBasedOnLastVar"),
        USE_FUNCTION_BASED_ON_LAST_VAR("useFunctionBasedOnLastVar"),
        CREATE_FOR_TABLE("createForTable")
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
        tokenEntryMap = new HashMap<>();
        tokenGroupsStack = new Stack<>();
    }

    /**
     * Evaluate action token
     * @param token
     * @param tokenIndex
     */
    public void eval(ABGrammarToken token, int tokenIndex, int phase) {

        if(token.getValue().equals(Type.CREATE_GLOBAL_TABLE.getName())) {

            /**
             * + Create global table
             * + Push global table to table stack
             */
            if(phase == 1) {
                // Create global table
                globalTable = new ABSymbolTable("Global", "Global", ABSymbolTableEntry.Kind.GLOBAL);
                globalTable.setId(allTables.size());
                allTables.add(globalTable);
                tablesStack.push(globalTable);

            /**
             * + Detect cycles in classes
             * + Push global table to table stack
             */
            } else if(phase == 2) {

                // Detect recursive declarations
                Set<ABSymbolTableEntry> visitedVariables = new HashSet<>();
                for(ABSymbolTableEntry entry : globalTable.getRows())
                    detectCycle(entry.getLink(), new HashSet<ABSymbolTable>(), visitedVariables);

                // Push to table stack
                tablesStack.push(globalTable);
            }

        } else if(token.getValue().equals(Type.CREATE_CLASS_TABLE_AND_ENTRY.getName())) {

            // Input token
            ABToken inputToken = inputTokens.get(tokenIndex - 1);

            /**
             * + Create class table and entry
             * + Check if class already exists
             * + Push class to table stack
             */
            if(phase == 1) {

                // Create class entry
                ABSymbolTableEntry entry = ABSymbolTableEntryFactory.createClassEntry(tablesStack.peek(), inputToken.getValue());
                entry.setToken(inputToken);

                // If class exists already
                checkExists(entry);

                // Add entry
                tablesStack.peek().addRow(entry);

                // Push class to stack
                entry.getLink().setId(allTables.size());
                allTables.add(entry.getLink());
                tablesStack.push(entry.getLink());

                // Register token
                tokenEntryMap.put(inputToken, entry);

            /**
             * + Find class
             * + Push to table stack
             */
            } else if(phase == 2) {

                // Search for table
                ABSymbolTableEntry entry = tokenEntryMap.get(inputToken);

                // Push to table stack
                tablesStack.push(entry.getLink());
            }

        } else if(token.getValue().equals(Type.PARENT.getName())) {

            // Pop the table
            tablesStack.pop();

        } else if(token.getValue().equals(Type.TYPE.getName())) {

            /**
             * + Create a new buffer
             * + Add token to the newly created buffer
             */
            if(phase == 1) {
                // Put token
                ABSemanticTokenGroup tokenGroup = new ABSemanticTokenGroup();
                tokenGroupsStack.push(tokenGroup);
                tokenGroup.addSubGroupToken(inputTokens.get(tokenIndex - 1));
            }

        } else if(token.getValue().equals(Type.CREATE_VAR_ENTRY.getName())) {
            ABToken inputToken = inputTokens.get(tokenIndex-1);

            /**
             * + Create variable entry
             * + Check if variable already exists
             */
            if(phase == 1) {

                // Create variable entry
                ABSymbolTableEntry entry = ABSymbolTableEntryFactory.createVariableEntry(tablesStack.peek(), inputToken.getValue());
                entry.setType(tokenGroupsStack.peek().getLastTokenSubGroup().getTokens());
                entry.setToken(inputToken);

                // If exists already
                checkExists(entry);

                // Add entry
                tablesStack.peek().addRow(entry);

                // Register token
                tokenEntryMap.put(inputToken, entry);

            /**
             * + Search for variable
             * + Check if the type exists
             */
            } else if(phase == 2) {

                // Find variable
                ABSymbolTableEntry entry = tokenEntryMap.get(inputToken);

                // Check type
                checkType(entry);
            }

        } else if(token.getValue().equals(Type.MORE_TYPE.getName())) {

            /**
             * Add more types
             */
            if(phase == 1) {
                tokenGroupsStack.peek().getLastTokenSubGroup().getTokens().add(inputTokens.get(tokenIndex - 1));
            }

        } else if(token.getValue().equals(Type.CREATE_PARAM_ENTRY.getName())) {

            // Input token
            ABToken inputToken = inputTokens.get(tokenIndex-1);

            /**
             * + Create a parameter entry
             * + Check if parameter exists
             */
            if(phase == 1) {

                // Create parameter entry
                ABSymbolTableEntry entry = ABSymbolTableEntryFactory.createParameterEntry(tablesStack.peek(), inputToken.getValue());
                entry.setType(tokenGroupsStack.peek().getLastTokenSubGroup().getTokens());
                entry.setToken(inputToken);

                // Check if exists
                checkExists(entry);

                // Add entry
                tablesStack.peek().addRow(entry);

                // Register token
                tokenEntryMap.put(inputToken, entry);

            /**
             * + Search for entry
             * + Check if type exists
             */
            } else if (phase == 2){

                // Find entry
                ABSymbolTableEntry entry = tokenEntryMap.get(inputToken);

                // If identifier
                checkType(entry);
            }

        } else if(token.getValue().equals(Type.CREATE_FUNCTION_ENTRY_AND_TABLE.getName())) {

            // Input token
            ABToken inputToken = inputTokens.get(tokenIndex-1);

            /**
             * + Create function table and entry
             * + Push function to table stack
             */
            if(phase == 1) {

                // Create function entry
                ABSymbolTableEntry entry = ABSymbolTableEntryFactory.createFunctionEntry(tablesStack.peek(), inputToken.getValue());
                entry.setType(tokenGroupsStack.peek().getLastTokenSubGroup().getTokens());
                entry.setToken(inputToken);

                // If exists already and not a function
                ABSymbolTableEntry definedEntry = searchEntryInTable(tablesStack.peek(), inputToken.getValue());
                if (definedEntry != null && definedEntry.getKind() != ABSymbolTableEntry.Kind.FUNCTION) {
                    entry.setProperlyDefined(false);
                    addError(inputToken, String.format(ABSemanticMessageHelper.MUTLIPLE_DECLARATION, inputToken.getValue(), definedEntry.getToken().getRow(), definedEntry.getToken().getCol(), inputToken.getRow(), inputToken.getCol()));
                }

                // Add entry
                tablesStack.peek().addRow(entry);

                // Push function to stack
                entry.getLink().setId(allTables.size());
                allTables.add(entry.getLink());
                tablesStack.push(entry.getLink());

                // Register token
                tokenEntryMap.put(inputToken, entry);

            /**
             * + Search for entry
             * + Push to stack
             */
            } else if (phase == 2) {

                // Find entry
                ABSymbolTableEntry entry = tokenEntryMap.get(inputToken);

                // Check if function type exists
                checkType(entry);

                // Check for function overload
                checkOverload(entry);

                // Push to table stack
                tablesStack.push(entry.getLink());
            }

        } else if(token.getValue().equals(Type.CREATE_PROGRAM_ENTRY_AND_TABLE.getName())) {

            // Input token
            ABToken inputToken = inputTokens.get(tokenIndex-1);

            /**
             * + Create program entry and table
             * + Push to stack
             */
            if(phase == 1) {
                // Create program entry
                ABSymbolTableEntry entry = ABSymbolTableEntryFactory.createProgramEntry(tablesStack.peek(), inputToken.getValue());
                entry.setToken(inputToken);
                tablesStack.peek().addRow(entry);

                // Push program to stack
                entry.getLink().setId(allTables.size());
                allTables.add(entry.getLink());
                tablesStack.push(entry.getLink());

                // Register token
                tokenEntryMap.put(inputToken, entry);

            /**
             * + Search for entry
             */
            } else if (phase == 2) {

                // Find entry
                ABSymbolTableEntry entry = tokenEntryMap.get(inputToken);

                // Push to stack
                tablesStack.push(entry.getLink());
            }

        } else if(token.getValue().equals(Type.CREATE_FOR_TABLE.getName())) {
            // Input token
            ABToken inputToken = inputTokens.get(tokenIndex-1);

            /**
             * + Create a for table
             * + Push to table stack
             */
            if(phase == 1) {
                // Create for entry
                ABSymbolTableEntry entry = ABSymbolTableEntryFactory.createForEntry(tablesStack.peek(), inputToken.getValue());
                entry.setToken(inputToken);

                // Push table to stack
                tablesStack.push(entry.getLink());

                // Register token
                tokenEntryMap.put(inputToken, entry);

            /**
             * + Search for table
             */
            } else if(phase == 2) {

                // Find entry
                ABSymbolTableEntry entry = tokenEntryMap.get(inputToken);

                // Push to stack
                tablesStack.push(entry.getLink());
            }

        } else if(token.getValue().equals(Type.USE_VAR.getName())) {

            // Input token
            ABToken inputToken = inputTokens.get(tokenIndex-1);

            /**
             * + Check if variable is defined
             */
            if(phase == 2) {
                ABSymbolTableEntry definedVarEntry = searchEntryInTableStack(tablesStack, inputToken.getValue(), ABSymbolTableEntry.Kind.VARIABLE);
                ABSymbolTableEntry definedParamEntry = searchEntryInTableStack(tablesStack, inputToken.getValue(), ABSymbolTableEntry.Kind.PARAMETER);

                // If undefined
                if (definedVarEntry == null && definedParamEntry == null) {
                    addError(inputToken, String.format(ABSemanticMessageHelper.UNDEFINED_VARIABLE, inputToken.getValue(), inputToken.getRow(), inputToken.getCol()));
                } else {
                    tokenEntryMap.put(inputToken, definedVarEntry != null ? definedVarEntry : definedParamEntry);
                }

                // Add to token group stack
                tokenGroupsStack.push(new ABSemanticTokenGroup());
                tokenGroupsStack.peek().addSubGroupToken(inputToken);
            }

        } else if(token.getValue().equals(Type.USE_FUNCTION.getName())) {

            /**
             * Check if function exists
             */
            if(phase == 2) {

                // Input token
                ABToken inputToken = inputTokens.get(tokenIndex - 1);
                ABSymbolTableEntry result = searchEntryInTableStack(tablesStack, inputToken.getValue(), ABSymbolTableEntry.Kind.FUNCTION);

                // If not found
                if(result == null) {
                    addError(inputToken, String.format(ABSemanticMessageHelper.UNDEFINED_FUNCTION, inputToken.getValue(), inputToken.getRow(), inputToken.getCol()));
                } else {
                    tokenEntryMap.put(inputToken, result);
                }

                // Add to token group stack
                tokenGroupsStack.push(new ABSemanticTokenGroup());
                tokenGroupsStack.peek().addSubGroupToken(inputToken);
            }

        } else if(token.getValue().equals(Type.USE_VAR_BASED_ON_LAST_VAR.getName())) {

            if(phase == 2) {
                // Input token
                ABToken inputToken = inputTokens.get(tokenIndex - 1);

                // Get group
                ABSemanticTokenGroup group = tokenGroupsStack.peek();

                // Get the last token sub group
                ABSemanticTokenGroup.ABSemanticTokenSubGroup subGroup = tokenGroupsStack.peek().getLastTokenSubGroup();

                // Get last token table entry
                ABSymbolTableEntry baseTableEntry = tokenEntryMap.get(subGroup.getTokens().get(0));

                // If not found
                if(baseTableEntry == null) {
                    addError(inputToken, String.format(ABSemanticMessageHelper.UNDEFINED_MEMBER_OF_UNDEFINED_VARIABLE, inputToken.getValue(), inputToken.getRow(), inputToken.getCol()));
                } else {

                    // If primitive type
                    if(baseTableEntry.isPrimitiveType()) {
                        addError(inputToken, String.format(ABSemanticMessageHelper.UNDEFINED_MEMBER_OF_PRIMITIVE_VARIABLE, inputToken.getValue(), inputToken.getRow(), inputToken.getCol()));
                    } else {

                        // Check if the type is defined
                        ABSymbolTableEntry classEntry = searchEntryInTable(globalTable, baseTableEntry.getType().get(0).getValue(), ABSymbolTableEntry.Kind.CLASS);

                        // If not found
                        if(classEntry == null) {
                            addError(inputToken, String.format(ABSemanticMessageHelper.UNDEFINED_MEMBER_OF_UNDEFINED_VARIABLE, inputToken.getValue(), inputToken.getRow(), inputToken.getCol()));
                        } else {
                            // Search for data member
                            ABSymbolTableEntry entry = searchEntryInTable(classEntry.getLink(), inputToken.getValue(), ABSymbolTableEntry.Kind.VARIABLE);

                            // If not found
                            if(entry == null) {
                                addError(inputToken, String.format(ABSemanticMessageHelper.UNDEFINED_MEMBER_OF_CLASS, inputToken.getValue(), classEntry.getName(), inputToken.getRow(), inputToken.getCol()));
                            } else {
                                tokenEntryMap.put(inputToken, entry);
                            }
                        }
                    }
                }

                // Add to token group stack
                group.addSubGroupToken(inputToken);
            }
        } else if(token.getValue().equals(Type.USE_FUNCTION_BASED_ON_LAST_VAR.getName())) {

            if(phase == 2) {
                // Input token
                ABToken inputToken = inputTokens.get(tokenIndex - 1);
                // TODO Check data member if found: Variable
            }
        } else {
            l.error("Action token: %s not found!", token.getValue());
        }
    }

    /*****************************************************
     *
     *                  PHASES I AND II METHODS
     *
     *****************************************************/

    /**
     * Check if multiple declaration was found for an identifier of any type
     * @param entry
     */
    public void checkExists(ABSymbolTableEntry entry) {
        // If exists already
        ABSymbolTableEntry definedEntry = searchEntryInTable(tablesStack.peek(), entry.getToken().getValue());
        if (definedEntry != null) {
            addError(entry.getToken(), String.format(ABSemanticMessageHelper.MUTLIPLE_DECLARATION, entry.getToken().getValue(), definedEntry.getToken().getRow(), definedEntry.getToken().getCol(), entry.getToken().getRow(), entry.getToken().getCol()));
            entry.setProperlyDefined(false);
        }
    }

    /**
     * Detect if a recursive declaration is found
     * @param table
     * @param visitedClasses
     */
    public void detectCycle(ABSymbolTable table, Set<ABSymbolTable> visitedClasses, Set<ABSymbolTableEntry> visitedVariables) {

        // Add table
        visitedClasses.add(table);

        // Loop on all rows
        for(ABSymbolTableEntry entry : table.getRows()) {

            // Cache type
            ABToken type = entry.getType().get(0);

            // If it's a variable and of non primitive type
            if(entry.getKind() == ABSymbolTableEntry.Kind.VARIABLE && type.getToken().equals(ABTokenHelper.T_IDENTIFIER) && !visitedVariables.contains(entry)) {

                // Get the class symbol table for this
                ABSymbolTableEntry newTableEntry = searchEntryInTable(globalTable, type.getValue(), ABSymbolTableEntry.Kind.CLASS);

                // If class found
                if(newTableEntry != null) {

                    // Cache table
                    ABSymbolTable newTable = newTableEntry.getLink();

                    // If new table is already visited
                    if(visitedClasses.contains(newTable)) {
                        addError(entry.getToken(), String.format(ABSemanticMessageHelper.RECURSIVE_DECLARATION, entry.getToken().getValue(), entry.getToken().getRow(), entry.getToken().getCol()));
                        visitedVariables.add(entry);
                        entry.setProperlyDefined(false);

                    // If not visited before
                    } else {
                        detectCycle(newTable, visitedClasses, visitedVariables);
                    }
                }
            }
        }

        // Remove table
        visitedClasses.remove(table);
    }

    /**
     * Check for method overload
     * @param entry
     */
    public void checkOverload(ABSymbolTableEntry entry) {

        // Get all functions with the same name
        List<ABSymbolTableEntry> entries = searchEntriesInTable(tablesStack.peek(), entry.getToken().getValue(), ABSymbolTableEntry.Kind.FUNCTION);
        for(ABSymbolTableEntry tableEntry : entries) {

            // If not same entry
            if(tableEntry == entry) return;

            // If match number of parameters
            List<List<ABToken>> tableEntryParameters = tableEntry.getParameters();
            List<List<ABToken>> entryParameters = entry.getParameters();
            if(tableEntryParameters.size() == entryParameters.size()) {

                // Check the type of each parameter
                for(int i=0; i < entryParameters.size(); i++) {

                    // If parameter size does not match
                    if(tableEntryParameters.get(i).size() != tableEntryParameters.get(i).size()) return;

                    // If any token in a parameter type is not equal, return
                    for(int j=0; j < entryParameters.get(i).size(); j++)
                        if(!entryParameters.get(i).get(j).getValue().equals(tableEntryParameters.get(i).get(j).getValue())) return;
                }

                // Signature matches
                addError(entry.getToken(), String.format(ABSemanticMessageHelper.SAME_SIGNATURE_FUNCTION, tableEntry.getToken().getValue(), tableEntry.getToken().getRow(), tableEntry.getToken().getCol(), entry.getToken().getRow(), entry.getToken().getCol()));
                entry.setProperlyDefined(false);
                return;
            }
        }
    }

    /**
     * Checks if type exists
     * @param entry
     */
    public ABSymbolTableEntry checkType(ABSymbolTableEntry entry) {

        // Cache type
        ABToken type = entry.getType().get(0);

        // If non-primitive
        if(type.getToken().equals(ABTokenHelper.T_IDENTIFIER)) {

            // Search for the type in the classes
            ABSymbolTableEntry result = searchEntryInTable(globalTable, type.getValue(), ABSymbolTableEntry.Kind.CLASS);

            // If not found
            if(result == null) {
                entry.setProperlyDefined(false);
                addError(entry.getToken(), String.format(ABSemanticMessageHelper.UNDEFINED_TYPE, type.getValue(), type.getRow(), type.getCol()));
            }
            return result;
        }
        return null;
    }

    /*****************************************************
     *
     *          SEARCH IN TABLE FOR ENTRIES
     *
     *****************************************************/

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
     * Search in specific table
     * @param table
     * @param name
     * @return
     */
    public ABSymbolTableEntry searchEntryInTable(ABSymbolTable table, String name) {
        return table.getEntry(name);
    }

    /**
     * Search entries in specific table
     * @param table
     * @param name
     * @return
     */
    public List<ABSymbolTableEntry> searchEntriesInTable(ABSymbolTable table, String name) {
        return table.getEntries(name);
    }

    /**
     * Search entries in specific table
     * @param table
     * @param name
     * @param kind
     * @return
     */
    public List<ABSymbolTableEntry> searchEntriesInTable(ABSymbolTable table, String name, ABSymbolTableEntry.Kind kind) {
        return table.getEntries(name, kind);
    }

    /**
     * Search for an entry
     * @param name
     * @return
     */
    public ABSymbolTableEntry searchEntryInTableStack(Stack<ABSymbolTable> tablesStack, String name) {
        return searchEntryInTableStack(tablesStack, name, ABSymbolTableEntry.Kind.ANY);
    }

    /**
     * Search for an entry
     * @param name
     * @param kind
     * @return
     */
    public ABSymbolTableEntry searchEntryInTableStack(Stack<ABSymbolTable> tablesStack, String name, ABSymbolTableEntry.Kind kind) {
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

    /*****************************************************
     *
     *          GETTERS AND SETTERS
     *
     *****************************************************/

    public List<ABToken> getInputTokens() {
        return inputTokens;
    }

    public void setInputTokens(List<ABToken> inputTokens) {
        this.inputTokens = inputTokens;
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

    /*****************************************************
     *
     *                   ADD ERROR
     *
     *****************************************************/

    private void addError(ABToken token, String message) {
        errors.add(new ABSemanticError(message, token));
    }

    /*****************************************************
     *
     *                   STRING UTILS
     *
     *****************************************************/

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

    /*****************************************************
     *
     *                   INNER CLASSES
     *
     *****************************************************/


    /*****************************
     *  AB SEMANTIC TOKEN GROUP
     *****************************/

    public class ABSemanticTokenGroup {

        private List<ABSemanticTokenSubGroup> tokens;
        private List<ABToken> type;

        public ABSemanticTokenGroup() {
            tokens = new ArrayList<>();
            type = new ArrayList<>();
        }

        public void addSubGroupToken(ABToken newToken) {
            ABSemanticTokenSubGroup tokenGroup = new ABSemanticTokenSubGroup();
            tokenGroup.addToken(newToken);
            tokens.add(tokenGroup);
        }

        public ABSemanticTokenSubGroup getLastTokenSubGroup() {
            return tokens.get(tokens.size()-1);
        }

        public List<ABToken> getType() {
            return type;
        }

        /**
         * Create a new list of all tokens
         * @deprecated
         * @return
         */
        public List<ABToken> getAllTokens() {
            List<ABToken> allTokens = new ArrayList<>();
            for(ABSemanticTokenSubGroup subGroup : tokens)
                for(ABToken subGroupToken : subGroup.getTokens())
                    allTokens.add(subGroupToken);
            return allTokens;
        }

        /*****************************
         *  AB SEMANTIC TOKEN SUB-GROUP
         *****************************/

        private class ABSemanticTokenSubGroup {
            private List<ABToken> tokensList;

            public ABSemanticTokenSubGroup() {
                tokensList = new ArrayList<>();
            }

            public void addToken(ABToken token) {
                tokensList.add(token);
            }

            public List<ABToken> getTokens() {
                return tokensList;
            }

            public void setTokens(List<ABToken> tokens) {
                this.tokensList = tokens;
            }

            public boolean isArray() {
                return tokensList.size() > 1;
            }

            public int getArrayDimension() { return (tokensList.size()-1)/3; }
        }
    }

    /*****************************
     *  AB SEMANTIC ERROR
     *****************************/

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

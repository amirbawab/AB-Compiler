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
    private Queue<ABSymbolTableEntry> phaseTwoEntryType;
    private Queue<ABSemanticFunctionCall> phaseTwoFunctions;
    private Queue<ABSemanticDataMember> phaseTwoDataMembers;
    private Queue<ABSymbolTableEntry> phaseTwoFunctionOverload;
    private ABSymbolTableEntry lastUsedVar = null;
    private int dataMemberGroupId = 0;

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
        USE_FUNCTION("useFunction"),
        USE_VAR_BASED_ON_LAST_VAR("useVarBasedOnLastVar"),
        USE_FUNCTION_BASED_ON_LAST_VAR("useFunctionBasedOnLastVar")
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
        phaseTwoEntryType = new LinkedList<>();
        phaseTwoFunctions = new LinkedList<>();
        phaseTwoDataMembers = new LinkedList<>();
        phaseTwoFunctionOverload = new LinkedList<>();
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
            ABSymbolTableEntry definedEntry = searchEntryInTable(globalTable, inputToken.getValue());

            // Create class entry
            ABSymbolTableEntry entry = ABSymbolTableEntryFactory.createClassEntry(tablesStack.peek(), inputToken.getValue());
            entry.setToken(inputToken);
            tablesStack.peek().addRow(entry);

            // If exists already
            if(definedEntry != null) {
                addError(inputToken, String.format(ABSemanticMessageHelper.MUTLIPLE_DECLARATION, inputToken.getValue(), definedEntry.getToken().getRow(), definedEntry.getToken().getCol(), inputToken.getRow(), inputToken.getCol()));
                entry.setProperlyDefined(false);
            }

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
            ABSymbolTableEntry definedEntry = searchEntryInTable(tablesStack.peek(), inputToken.getValue());

            // Create variable entry
            ABSymbolTableEntry entry = ABSymbolTableEntryFactory.createVariableEntry(tablesStack.peek(), inputToken.getValue());
            entry.setType(type_buffer);
            entry.setToken(inputToken);
            tablesStack.peek().addRow(entry);

            // If exists already
            if(definedEntry != null) {
                entry.setProperlyDefined(false);
                addError(inputToken, String.format(ABSemanticMessageHelper.MUTLIPLE_DECLARATION, inputToken.getValue(), definedEntry.getToken().getRow(), definedEntry.getToken().getCol(), inputToken.getRow(), inputToken.getCol()));
            }

            // Push for phase 2 verification if the type is not primitive
            if(entry.getType().get(0).getToken().equals(ABTokenHelper.T_IDENTIFIER))
                phaseTwoEntryType.offer(entry);

        } else if(token.getValue().equals(Type.MORE_TYPE.getName())) {

            // Add more type
            type_buffer.add(tokens.get(tokenIndex-1));

        } else if(token.getValue().equals(Type.CREATE_PARAM_ENTRY.getName())) {

            // Input token
            ABToken inputToken = tokens.get(tokenIndex-1);
            ABSymbolTableEntry definedEntry = searchEntryInTable(tablesStack.peek(), inputToken.getValue());

            // Create class entry
            ABSymbolTableEntry entry = ABSymbolTableEntryFactory.createParameterEntry(tablesStack.peek(), inputToken.getValue());
            entry.setType(type_buffer);
            entry.setToken(inputToken);
            tablesStack.peek().addRow(entry);

            // If exists already
            if(definedEntry != null) {
                addError(inputToken, String.format(ABSemanticMessageHelper.MUTLIPLE_DECLARATION, inputToken.getValue(), definedEntry.getToken().getRow(), definedEntry.getToken().getCol(), inputToken.getRow(), inputToken.getCol()));
                entry.setProperlyDefined(false);
            }

            // Push for phase 2 verification if the type is not primitive
            if(entry.getType().get(0).getToken().equals(ABTokenHelper.T_IDENTIFIER))
                phaseTwoEntryType.offer(entry);

        } else if(token.getValue().equals(Type.CREATE_FUNCTION_ENTRY_AND_TABLE.getName())) {

            // Input token
            ABToken inputToken = tokens.get(tokenIndex-1);
            ABSymbolTableEntry definedEntry = searchEntryInTable(tablesStack.peek(), inputToken.getValue());

            // Create function entry
            ABSymbolTableEntry entry = ABSymbolTableEntryFactory.createFunctionEntry(tablesStack.peek(), inputToken.getValue());
            entry.setType(type_buffer);
            entry.setToken(inputToken);
            tablesStack.peek().addRow(entry);

            // If exists already and not a function
            if(definedEntry != null && definedEntry.getKind() != ABSymbolTableEntry.Kind.FUNCTION) {
                entry.setProperlyDefined(false);
                addError(inputToken, String.format(ABSemanticMessageHelper.MUTLIPLE_DECLARATION, inputToken.getValue(), definedEntry.getToken().getRow(), definedEntry.getToken().getCol(), inputToken.getRow(), inputToken.getCol()));
            }

            // Push function to stack
            entry.getLink().setId(allTables.size());
            allTables.add(entry.getLink());
            tablesStack.push(entry.getLink());

            // Push for phase 2 verification if the type is not primitive
            if(entry.getType().get(0).getToken().equals(ABTokenHelper.T_IDENTIFIER))
                phaseTwoEntryType.offer(entry);

            // Push for phase 2 to verify if overloaded
            phaseTwoFunctionOverload.offer(entry);

        } else if(token.getValue().equals(Type.CREATE_PROGRAM_ENTRY_AND_TABLE.getName())) {

            // Input token
            ABToken inputToken = tokens.get(tokenIndex-1);

            // Create program entry
            ABSymbolTableEntry entry = ABSymbolTableEntryFactory.createProgramEntry(tablesStack.peek(), inputToken.getValue());
            entry.setToken(inputToken);
            tablesStack.peek().addRow(entry);

            // Push program to stack
            entry.getLink().setId(allTables.size());
            allTables.add(entry.getLink());
            tablesStack.push(entry.getLink());

        } else if(token.getValue().equals(Type.USE_VAR.getName())) {

            // Input token
            ABToken inputToken = tokens.get(tokenIndex-1);
            ABSymbolTableEntry definedVar = searchEntryInTableStack(tablesStack, inputToken.getValue(), ABSymbolTableEntry.Kind.VARIABLE);
            ABSymbolTableEntry definedParam = searchEntryInTableStack(tablesStack, inputToken.getValue(), ABSymbolTableEntry.Kind.PARAMETER);

            if(definedVar == null && definedParam == null) {
                addError(inputToken, String.format(ABSemanticMessageHelper.UNDEFINED_VARIABLE, inputToken.getValue(), inputToken.getRow(), inputToken.getCol()));
                lastUsedVar = null;

            } else if(definedVar != null) {
                lastUsedVar = definedVar;

            } else{
                lastUsedVar = definedParam;
            }

            // Increment data member id
            dataMemberGroupId++;

        } else if(token.getValue().equals(Type.USE_FUNCTION.getName())) {

            // Input token
            ABToken inputToken = tokens.get(tokenIndex-1);
            ABSymbolTableEntry result = searchEntryInTableStack(tablesStack, inputToken.getValue(), ABSymbolTableEntry.Kind.FUNCTION);

            // If function not found, then push for phase 2 verification
            if(result == null)
                phaseTwoFunctions.offer(new ABSemanticFunctionCall(inputToken, tablesStack));

        } else if(token.getValue().equals(Type.USE_VAR_BASED_ON_LAST_VAR.getName())) {

            // Input token
            ABToken inputToken = tokens.get(tokenIndex-1);
            phaseTwoDataMembers.offer(new ABSemanticDataMember(dataMemberGroupId, lastUsedVar, ABSymbolTableEntry.Kind.VARIABLE, inputToken));

        } else if(token.getValue().equals(Type.USE_FUNCTION_BASED_ON_LAST_VAR.getName())) {

            // Input token
            ABToken inputToken = tokens.get(tokenIndex-1);
            phaseTwoDataMembers.offer(new ABSemanticDataMember(dataMemberGroupId, lastUsedVar, ABSymbolTableEntry.Kind.FUNCTION, inputToken));

        } else {
            l.error("Action token: %s not found!", token.getValue());
        }
    }

    /**
     * Evaluate the data again
     */
    public void evalPhaseTwo() {

        // Process variable and function types
        while(!phaseTwoEntryType.isEmpty()) {
            ABSymbolTableEntry entry = phaseTwoEntryType.poll();

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
            ABSymbolTableEntry result = searchEntryInTableStack(functionCall.getTableStack(), functionCall.getToken().getValue(), ABSymbolTableEntry.Kind.FUNCTION);

            // If not found
            if(result == null)
                addError(functionCall.getToken(), String.format(ABSemanticMessageHelper.UNDEFINED_FUNCTION, functionCall.getToken().getValue(), functionCall.getToken().getRow(), functionCall.getToken().getCol()));
        }

        // Check if data members are defined correctly
        ABToken lastType = null;
        int lastGroupId = -1;
        while(!phaseTwoDataMembers.isEmpty()) {
            ABSemanticDataMember dataMember = phaseTwoDataMembers.poll();

            // If no previous entry
            if(dataMember.getPreviousEntry() == null) {
                addError(dataMember.getToken(), String.format(ABSemanticMessageHelper.UNDEFINED_MEMBER_OF_PRIMITIVE_OR_UNDEFINED_VAR, dataMember.getToken().getValue(), dataMember.getToken().getRow(), dataMember.getToken().getCol()));
                lastType = null;

            // If same sequence and an error was detected before
            } else if(lastGroupId == dataMember.getGroupId() && lastType == null) {
                addError(dataMember.getToken(), String.format(ABSemanticMessageHelper.UNDEFINED_MEMBER_OF_PRIMITIVE_OR_UNDEFINED_VAR, dataMember.getToken().getValue(), dataMember.getToken().getRow(), dataMember.getToken().getCol()));

            } else {

                // Update last type if new sequence
                if(lastGroupId != dataMember.getGroupId()) {
                    lastType = dataMember.getPreviousEntry().getType().get(0);
                    lastGroupId = dataMember.getGroupId();
                }

                // If previous token doesn't have a primitive type
                if(lastType.getToken().equals(ABTokenHelper.T_IDENTIFIER)) {

                    // Search for class table
                    ABSymbolTableEntry classTableEntry = searchEntryInTable(globalTable, lastType.getValue(), ABSymbolTableEntry.Kind.CLASS);

                    // Check if table was found
                    if(classTableEntry != null) {

                        // If data member is a variable
                        if(dataMember.getKind() == ABSymbolTableEntry.Kind.VARIABLE) {

                            // Search for data member
                            ABSymbolTableEntry variableEntry = searchEntryInTable(classTableEntry.getLink(), dataMember.getToken().getValue(), ABSymbolTableEntry.Kind.VARIABLE);

                            // If not found
                            if(variableEntry == null) {
                                addError(dataMember.getToken(), String.format(ABSemanticMessageHelper.UNDEFINED_MEMBER_OF_CLASS, dataMember.getToken().getValue(), lastType.getValue(), dataMember.getToken().getRow(), dataMember.getToken().getCol()));
                                lastType = null;

                            // If found, update last type
                            } else {
                                lastType = variableEntry.getType().get(0);
                            }

                            // If data member is a function
                        } else if(dataMember.getKind() == ABSymbolTableEntry.Kind.FUNCTION) {

                            // Search for data member
                            ABSymbolTableEntry functionEntry = searchEntryInTable(classTableEntry.getLink(), dataMember.getToken().getValue(), ABSymbolTableEntry.Kind.FUNCTION);

                            // If not found
                            if(functionEntry == null) {
                                addError(dataMember.getToken(), String.format(ABSemanticMessageHelper.UNDEFINED_MEMBER_OF_CLASS, dataMember.getToken().getValue(), lastType.getValue(), dataMember.getToken().getRow(), dataMember.getToken().getCol()));
                                lastType = null;
                            }
                        }
                        // If table was not found
                    } else {
                        addError(dataMember.getToken(), String.format(ABSemanticMessageHelper.UNDEFINED_MEMBER_OF_PRIMITIVE_OR_UNDEFINED_VAR, dataMember.getToken().getValue(), dataMember.getToken().getRow(), dataMember.getToken().getCol()));
                        lastType = null;
                    }

                    // If primitive type
                } else {
                    addError(dataMember.getToken(), String.format(ABSemanticMessageHelper.UNDEFINED_MEMBER_OF_PRIMITIVE_OR_UNDEFINED_VAR, dataMember.getToken().getValue(), dataMember.getToken().getRow(), dataMember.getToken().getCol()));
                    lastType = null;
                }
            }
        }

        // Check for function overload
        Set<ABSymbolTableEntry> sameSignature = new HashSet<>();
        while(!phaseTwoFunctionOverload.isEmpty()) {
            ABSymbolTableEntry entry = phaseTwoFunctionOverload.poll();

            // If entry was not checked by a previous entry
            if(!sameSignature.contains(entry)) {

                // Get all function entries
                List<ABSymbolTableEntry> entries = searchEntriesInTable(entry.getTable(), entry.getName(), ABSymbolTableEntry.Kind.FUNCTION);
                for (ABSymbolTableEntry tableEntry : entries) {
                    if (entry != tableEntry && entry.equals(tableEntry)) {
                        addError(entry.getToken(), String.format(ABSemanticMessageHelper.SAME_SIGNATURE_FUNCTION, entry.getToken().getValue(), entry.getToken().getRow(), entry.getToken().getCol(), tableEntry.getToken().getRow(), tableEntry.getToken().getCol()));
                        tableEntry.setProperlyDefined(false);
                        sameSignature.add(tableEntry);
                    }
                }
            }
        }

        // Detect recursive declarations
        Set<ABSymbolTableEntry> visitedVariables = new HashSet<>();
        for(ABSymbolTableEntry entry : globalTable.getRows()) {
            detectCycle(entry.getLink(), new HashSet<ABSymbolTable>(), visitedVariables);
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

    public class ABSemanticDataMember {
        private ABSymbolTableEntry previousEntry;
        private ABSymbolTableEntry.Kind kind;
        private ABToken token;
        private int groupId;

        public ABSemanticDataMember(int groupId, ABSymbolTableEntry previousEntry, ABSymbolTableEntry.Kind kind, ABToken token) {
            this.previousEntry = previousEntry;
            this.kind = kind;
            this.token = token;
            this.groupId = groupId;
        }

        public ABSymbolTableEntry getPreviousEntry() {
            return previousEntry;
        }

        public void setPreviousEntry(ABSymbolTableEntry previousEntry) {
            this.previousEntry = previousEntry;
        }

        public ABSymbolTableEntry.Kind getKind() {
            return kind;
        }

        public void setKind(ABSymbolTableEntry.Kind kind) {
            this.kind = kind;
        }

        public ABToken getToken() {
            return token;
        }

        public void setToken(ABToken token) {
            this.token = token;
        }

        public int getGroupId() {
            return groupId;
        }

        public void setGroupId(int groupId) {
            this.groupId = groupId;
        }
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

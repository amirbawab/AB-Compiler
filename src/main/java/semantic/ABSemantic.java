package semantic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import parser.grammar.ABGrammarToken;
import scanner.ABToken;
import scanner.helper.ABTokenHelper;
import scanner.helper.IdentifierHelper;
import semantic.helper.ABSemanticMessageHelper;
import translation.ABTranslation;
import translation.helper.ABArchitectureHelper;

import java.util.*;

/**
 * Created by Amir on 3/26/2016.
 */
public class ABSemantic {

    // Logger
    private Logger l = LogManager.getFormatterLogger(getClass());

    // Translation
    ABTranslation abTranslation;

    // Tables
    private ABSymbolTable globalTable;
    private List<ABSymbolTable> allTables;
    private Stack<ABSymbolTable> tablesStack;

    // Error
    private List<ABSemanticError> errors;

    // Helper
    private Map<ABToken, ABSymbolTableEntry> tokenEntryMap;
    private Stack<ABSemanticTokenGroup> tokenGroupsStack;
    private Stack<ABToken> arithOpStack;

    // Tokens list
    private List<ABToken> inputTokens;

    public enum Type {
        CREATE_GLOBAL_TABLE("createGlobalTable"),                                               // Create global table
        CREATE_CLASS_TABLE_AND_ENTRY("createClassTableAndEntry"),                               // Create class table and entry
        PARENT("parent"),                                                                       // Pop table stack
        TYPE("type"),                                                                           // Buffer type in tokens group stack
        CREATE_VAR_ENTRY("createVarEntry"),                                                     // Create a variable entry
        CREATE_PARAM_ENTRY("createParamEntry"),                                                 // Create a parameter entry
        CREATE_FUNCTION_ENTRY_AND_TABLE("createFunctionEntryAndTable"),                         // Create a function table and entry
        CREATE_PROGRAM_ENTRY_AND_TABLE("createProgramEntryAndTable"),                           // Create a program table and entry
        MORE_TYPE("moreType"),                                                                  // Add more type to buffer
        USE_VAR("useVar"),                                                                      // Buffer a variable in token group stack
        USE_VAR_CHECK("useVarCheck"),                                                           // Check that the only sub group variable is defined correctly
        USE_FUNCTION("useFunction"),                                                            // Buffer a function in token group stack
        USE_FUNCTION_CHECK("useFunctionCheck"),                                                 // Check that the only sub group function is defined correctly
        USE_VAR_BASED_ON_LAST_VAR("useVarBasedOnLastVar"),                                      // Buffer a data member in token group stack
        USE_VAR_BASED_ON_LAST_VAR_CHECK("useVarBasedOnLastVarCheck"),                           // Check that the last sub group variable is defined correctly
        USE_FUNCTION_BASED_ON_LAST_VAR("useFunctionBasedOnLastVar"),                            // Buffer a data function in token group stack
        USE_FUNCTION_BASED_ON_LAST_VAR_CHECK("useFunctionBasedOnLastVarCheck"),                 // Check that the last sub group function is defined correctly
        CREATE_FOR_TABLE("createForTable"),                                                     // Create a for loop table [no entry]
        POP_GROUP_STACK_1("popGroupStack1"),                                                    // Pop token group stack in phase 1
        POP_GROUP_STACK_2("popGroupStack2"),                                                    // Pop token group stack in phase 2
        USE_NOT("useNot"),                                                                      // Peeek token group
        USE_ADD_OP("useAddOp"),                                                                 // Store operator in symbol stack
        USE_MULT_OP("useMultOp"),                                                               // Store operator in symbol stack
        USE_COMPARE_OP("useCompareOp"),                                                         // Store operator in symbol stack
        USE_ASSIGN_OP("useAssignOp"),                                                           // Store operator in symbol stack
        USE_INT("useInt"),                                                                      // Buffer an integer
        USE_FLOAT("useFloat"),                                                                  // Buffer a float
        USE_GET("useGet"),                                                                      // Pop token group stack in phase 2
        USE_PUT("usePut"),                                                                      // Pop token group stack in phase 2
        MATH_ADD_OP("mathAddOp"),                                                               // Check type for two last sub groups
        MATH_MULT_OP("mathMultOp"),                                                             // Check type for two last sub groups
        MATH_ASSIGN_OP("mathAssignOp"),                                                         // Check the type of assignment
        MATH_COMPARE_OP("mathCompareOp"),                                                       // Check type for two last sub groups
        VAR_INDEX("varIndex"),                                                                  // Check if the type is correct
        FUNCTION_PARAM("functionParam"),                                                        // Check if the index is correct
        FUNCTION_RETURN("functionReturn"),                                                      // Store return into function
        POP_GROUP_STACK_FUNCTION("popGroupStackFunction");                                      // Pop function from stack

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
        arithOpStack = new Stack<>();
        abTranslation = new ABTranslation();
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

            // If end of phase 2 and no errors
            if(phase == 2 && tablesStack.isEmpty() && errors.isEmpty()) {
                try {

                    // Generate size
                    generateTableAndEntrySize(globalTable);

                    // Generate labels
                    generateEntryLabel();

                    // Generate footer from tables
                    for(ABSymbolTable table : allTables) {
                        switch (table.getKind()) {
                            case PROGRAM:
                                abTranslation.appendFooter(table);
                                break;
                        }
                    }

                } catch (NumberFormatException e) {
                    l.error(e.getMessage());
                    abTranslation.setGenerateCode(false);
                }
            }

        } else if(token.getValue().equals(Type.TYPE.getName())) {

            /**
             * + Create a new buffer
             * + Add token to the newly created buffer
             */
            if(phase == 1) {
                // Put token
                ABSemanticTokenGroup tokenGroup = new ABSemanticTokenGroup();
                tokenGroupsStack.push(tokenGroup);
                tokenGroup.addSubGroupToken(null);
                List<ABToken> typeTokens = new ArrayList<>();
                typeTokens.add(inputTokens.get(tokenIndex - 1));
                tokenGroup.getLastTokenSubGroup().addArgument(typeTokens);
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
                entry.setType(tokenGroupsStack.peek().getLastTokenSubGroup().getArgumentList(0));
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
                checkTypeExists(entry);
            }

        } else if(token.getValue().equals(Type.MORE_TYPE.getName())) {

            /**
             * Add more types
             */
            if(phase == 1) {
                tokenGroupsStack.peek().getLastTokenSubGroup().getArgumentList(0).add(inputTokens.get(tokenIndex - 1));
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
                entry.setType(tokenGroupsStack.peek().getLastTokenSubGroup().getArgumentList(0));
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
                checkTypeExists(entry);
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
                entry.setType(tokenGroupsStack.peek().getLastTokenSubGroup().getArgumentList(0));
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
                checkTypeExists(entry);

                // Check for function overload
                checkOverload(entry);

                // Push to group stack to check for later return statement
                ABSemanticTokenGroup functionGroup = new ABSemanticTokenGroup();
                functionGroup.addSubGroupToken(inputToken);
                tokenGroupsStack.push(functionGroup);

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

                // Allow code generation
                abTranslation.setGenerateCode(true);
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

            /**
             * + Check if variable is defined
             */
            if(phase == 2) {

                // Input token
                ABToken inputToken = inputTokens.get(tokenIndex-1);

                // Add to token group stack
                tokenGroupsStack.push(new ABSemanticTokenGroup());
                tokenGroupsStack.peek().addSubGroupToken(inputToken);
            }

        } else if(token.getValue().equals(Type.USE_VAR_CHECK.getName())) {

            if(phase == 2) {
                checkVariableUse();
            }

        } else if(token.getValue().equals(Type.USE_VAR_BASED_ON_LAST_VAR.getName())) {

            if(phase == 2) {

                // Input token
                ABToken inputToken = inputTokens.get(tokenIndex - 1);

                // Add to token group stack
                tokenGroupsStack.peek().addSubGroupToken(inputToken);
            }
        } else if(token.getValue().equals(Type.USE_VAR_BASED_ON_LAST_VAR_CHECK.getName())) {

            if(phase == 2) {
                checkVariableDataMember();
            }

        } else if(token.getValue().equals(Type.USE_FUNCTION.getName())) {

            /**
             * Check if function exists
             */
            if(phase == 2) {

                // Input token
                ABToken inputToken = inputTokens.get(tokenIndex - 1);

                // Add to token group stack
                tokenGroupsStack.push(new ABSemanticTokenGroup());
                tokenGroupsStack.peek().addSubGroupToken(inputToken);
            }

        } else if(token.getValue().equals(Type.USE_FUNCTION_CHECK.getName())) {

            if(phase == 2) {
                checkFunctionUse();
            }

        } else if(token.getValue().equals(Type.USE_FUNCTION_BASED_ON_LAST_VAR.getName())) {

            if(phase == 2) {
                // Input token
                ABToken inputToken = inputTokens.get(tokenIndex - 1);

                // Add to token group stack
                tokenGroupsStack.peek().addSubGroupToken(inputToken);
            }

        } else if(token.getValue().equals(Type.USE_FUNCTION_BASED_ON_LAST_VAR_CHECK.getName())) {

            if(phase == 2) {
                checkFunctionDataMember();
            }

        } else if(token.getValue().equals(Type.POP_GROUP_STACK_1.getName())) {

            if(phase == 1) {
                tokenGroupsStack.pop();
            }
        } else if(token.getValue().equals(Type.POP_GROUP_STACK_2.getName())) {

            if(phase == 2) {
                tokenGroupsStack.pop();
            }

        } else if(token.getValue().equals(Type.USE_NOT.getName())) {

            if(phase == 2) {

                // Peek data without popping it
                ABSemanticTokenGroup dataGroup = tokenGroupsStack.peek();

                // TODO Generate code for inverse
            }

        } else if(token.getValue().equals(Type.USE_ADD_OP.getName())) {

            if(phase == 2) {
                arithOpStack.push(inputTokens.get(tokenIndex - 1));
            }

        } else if(token.getValue().equals(Type.USE_MULT_OP.getName())) {

            if(phase == 2) {
                arithOpStack.push(inputTokens.get(tokenIndex - 1));
            }

        } else if(token.getValue().equals(Type.USE_ASSIGN_OP.getName())) {

            if(phase == 2) {
                arithOpStack.push(inputTokens.get(tokenIndex - 1));
            }

        } else if(token.getValue().equals(Type.USE_COMPARE_OP.getName())) {

            if(phase == 2) {
                arithOpStack.push(inputTokens.get(tokenIndex - 1));
            }

        } else if(token.getValue().equals(Type.USE_INT.getName())) {

            if(phase == 2) {
                // Input token
                ABToken inputToken = inputTokens.get(tokenIndex - 1);
                tokenGroupsStack.push(new ABSemanticTokenGroup());
                tokenGroupsStack.peek().addSubGroupToken(inputToken);

                // Generate return list
                List<ABToken> intTokenType = new ArrayList<>(1);
                intTokenType.add(new ABToken(IdentifierHelper.ReservedWords.INT.getToken(), IdentifierHelper.ReservedWords.INT.getMatch(),inputToken.getRow(),inputToken.getCol()));
                tokenGroupsStack.peek().getLastTokenSubGroup().setReturnTypeList(intTokenType);
            }

        } else if(token.getValue().equals(Type.USE_FLOAT.getName())) {

            if(phase == 2) {
                // Input token
                ABToken inputToken = inputTokens.get(tokenIndex - 1);
                tokenGroupsStack.push(new ABSemanticTokenGroup());
                tokenGroupsStack.peek().addSubGroupToken(inputToken);

                // Generate return list
                List<ABToken> floatTokenType = new ArrayList<>(1);
                floatTokenType.add(new ABToken(IdentifierHelper.ReservedWords.FLOAT.getToken(), IdentifierHelper.ReservedWords.FLOAT.getMatch(),inputToken.getRow(),inputToken.getCol()));
                tokenGroupsStack.peek().getLastTokenSubGroup().setReturnTypeList(floatTokenType);
            }

        } else if(token.getValue().equals(Type.MATH_ADD_OP.getName())) {

            if(phase == 2) {

                // Pop data
                ABToken arithOp = arithOpStack.pop();
                ABSemanticTokenGroup RHS = tokenGroupsStack.pop();
                ABSemanticTokenGroup LHS = tokenGroupsStack.pop();

                // Create new group amd push to stack
                tokenGroupsStack.push(new ABSemanticTokenGroup());
                tokenGroupsStack.peek().addSubGroupToken(null);

                // Check if sum has a correct type
                List<ABToken> returnType = checkArithmeticType(LHS, RHS, arithOp);
                tokenGroupsStack.peek().getLastTokenSubGroup().setReturnTypeList(returnType);
            }

        } else if(token.getValue().equals(Type.MATH_MULT_OP.getName())) {

            if(phase == 2) {

                // Pop data
                ABToken arithOp = arithOpStack.pop();
                ABSemanticTokenGroup RHS = tokenGroupsStack.pop();
                ABSemanticTokenGroup LHS = tokenGroupsStack.pop();

                // Create new group amd push to stack
                tokenGroupsStack.push(new ABSemanticTokenGroup());
                tokenGroupsStack.peek().addSubGroupToken(null);

                // Check if multiplication has a correct type
                List<ABToken> returnType = checkArithmeticType(LHS, RHS, arithOp);
                tokenGroupsStack.peek().getLastTokenSubGroup().setReturnTypeList(returnType);
            }

        } else if(token.getValue().equals(Type.MATH_COMPARE_OP.getName())) {

            if(phase == 2) {

                // Pop data
                ABToken arithOp = arithOpStack.pop();
                ABSemanticTokenGroup RHS = tokenGroupsStack.pop();
                ABSemanticTokenGroup LHS = tokenGroupsStack.pop();

                // Create new group amd push to stack
                tokenGroupsStack.push(new ABSemanticTokenGroup());
                tokenGroupsStack.peek().addSubGroupToken(null);

                // Check if multiplication has a correct type
                List<ABToken> returnType = checkArithmeticType(LHS, RHS, arithOp);
                tokenGroupsStack.peek().getLastTokenSubGroup().setReturnTypeList(returnType);
            }

        } else if(token.getValue().equals(Type.MATH_ASSIGN_OP.getName())) {

            if(phase == 2) {

                // Pop data
                ABToken arithOp = arithOpStack.pop();
                ABSemanticTokenGroup RHS = tokenGroupsStack.pop();
                ABSemanticTokenGroup LHS = tokenGroupsStack.pop();

                // Type should math
                checkAssignment(LHS, RHS, arithOp);
            }

        } else if(token.getValue().equals(Type.VAR_INDEX.getName())) {

            if(phase == 2) {

                // Pop data
                ABSemanticTokenGroup index = tokenGroupsStack.pop();

                // Add type to array
                tokenGroupsStack.peek().getLastTokenSubGroup().addArgument(index.getLastTokenSubGroup().getReturnTypeList());
            }

        } else if(token.getValue().equals(Type.FUNCTION_PARAM.getName())) {

            if(phase == 2) {

                // Pop data
                ABSemanticTokenGroup param = tokenGroupsStack.pop();

                // Add type to function
                tokenGroupsStack.peek().getLastTokenSubGroup().addArgument(param.getLastTokenSubGroup().getReturnTypeList());
            }

        } else if(token.getValue().equals(Type.FUNCTION_RETURN.getName())) {

            if(phase == 2) {
                // Pop data
                ABSemanticTokenGroup returnGroup = tokenGroupsStack.pop();

                // Add type to function
                tokenGroupsStack.peek().getLastTokenSubGroup().setReturnTypeList(returnGroup.getLastTokenSubGroup().getReturnTypeList());
            }

        } else if(token.getValue().equals(Type.POP_GROUP_STACK_FUNCTION.getName())) {

            if(phase == 2) {
                ABSemanticTokenGroup functionGroup = tokenGroupsStack.pop();

                // Input token
                ABToken usedToken = functionGroup.getLastTokenSubGroup().getUsedToken();

                // Check if return type was not set or is undefined
                if(functionGroup.getLastReturnType() == null) {
                    addError(usedToken, String.format(ABSemanticMessageHelper.FUNCTION_WRONG_RETURN, usedToken.getValue(),usedToken.getRow(), usedToken.getCol()));

                // If defined
                } else {

                    // Get function entry
                    ABSymbolTableEntry entry = tokenEntryMap.get(usedToken);

                    // If entry found
                    if(entry != null) {

                        // If function group has an array as return type, or if type does not match
                        if(functionGroup.getLastReturnType().size() > 1 || !entry.getType().get(0).getValue().equals(functionGroup.getLastReturnType().get(0).getValue())) {
                            addError(usedToken, String.format(ABSemanticMessageHelper.FUNCTION_WRONG_RETURN, usedToken.getValue(),usedToken.getRow(), usedToken.getCol()));
                        }

                        // If entry was not found
                    } else {
                        addError(usedToken, String.format(ABSemanticMessageHelper.FUNCTION_WRONG_RETURN, usedToken.getValue(),usedToken.getRow(), usedToken.getCol()));
                    }
                }
            }

        } else if(token.getValue().equals(Type.USE_GET.getName())) {

            if(phase == 2) {

                // Get data
                ABSemanticTokenGroup getGroup = tokenGroupsStack.pop();

                // TODO Generate code
            }

        } else if(token.getValue().equals(Type.USE_PUT.getName())) {

            if(phase == 2) {

                // Get data
                ABSemanticTokenGroup putGroup = tokenGroupsStack.pop();

                // TODO Generate code
            }

        } else {
            l.error("Action token: %s not found!", token.getValue());
        }
    }

    /*****************************************************
     *
     *                  PHASES I METHODS
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


    /*****************************************************
     *
     *                  PHASES II METHODS
     *
     *****************************************************/

    /**
     * Check if array dimension is correct
     * @param entry
     * @param subGroup
     * @return true if successful, false otherwise
     */
    public boolean checkArray(ABSymbolTableEntry entry, ABSemanticTokenGroup.ABSemanticTokenSubGroup subGroup) {

        // Get the type
        ABToken usedVarToken = subGroup.getUsedToken();

        // If used as an array but is not
        if(subGroup.getArgumentsSize() > 0 && !entry.isArray()) {
            addError(usedVarToken, String.format(ABSemanticMessageHelper.VARIABLE_NOT_ARRAY, usedVarToken.getValue(), usedVarToken.getRow(), usedVarToken.getCol()));

            // If used as a variable but is not
        } else if(subGroup.getArgumentsSize() == 0 && entry.isArray()) {
            addError(usedVarToken, String.format(ABSemanticMessageHelper.VARIABLE_IS_ARRAY, usedVarToken.getValue(), usedVarToken.getRow(), usedVarToken.getCol()));

            // If both are arrays but have different dimensions
        } else if(subGroup.getArgumentsSize() !=  entry.getArrayDimension()) {
            addError(usedVarToken, String.format(ABSemanticMessageHelper.ARRAYS_UNMATCH_DIMENSION, usedVarToken.getValue(), entry.getArrayDimension(), subGroup.getArgumentsSize(), usedVarToken.getRow(), usedVarToken.getCol()));

            // All good
        } else {
            return true;
        }
        return false;
    }

    /**
     * Check if functions and sub group have the same parameters
     * @param entry
     * @param group
     * @return true if match, false otherwise
     */
    public boolean checkFunctionParameters(ABSymbolTableEntry entry, ABSemanticTokenGroup group) {

        // If match number of parameters
        List<List<ABToken>> entryParameters = entry.getParameters();

        // Check parameter size
        if(entryParameters.size() != group.getLastTokenSubGroup().getArgumentsSize())
            return false;

        // Compare parameters
        for(int i=0; i < entryParameters.size(); i++) {

            List<ABToken> entryParam = entryParameters.get(i);
            List<ABToken> groupParam = group.getLastTokenSubGroup().getArgumentList(i);

            // Parameter is undefined as a result of wrong expression
            if(groupParam == null)
                return false;

            // Compare size
            if(entryParam.size() != groupParam.size())
                return false;

            // Compare each token
            for(int j=0; j < entryParam.size(); j++) {

                // If a token does not match
                if(!entryParam.get(j).getValue().equals(groupParam.get(j).getValue()))
                    return false;
            }
        }

        return true;
    }

    /**
     * Check if the variable was used correctly
     */
    public void checkVariableUse() {
        // Load last entered sub group
        ABSemanticTokenGroup.ABSemanticTokenSubGroup usedVarTokenSubGroup = tokenGroupsStack.peek().getLastTokenSubGroup();

        // Get used token
        ABToken usedVarToken = usedVarTokenSubGroup.getUsedToken();

        ABSymbolTableEntry definedVarEntry = searchEntryInTableStack(tablesStack, usedVarToken.getValue(), ABSymbolTableEntry.Kind.VARIABLE);
        ABSymbolTableEntry definedParamEntry = searchEntryInTableStack(tablesStack, usedVarToken.getValue(), ABSymbolTableEntry.Kind.PARAMETER);

        // If undefined
        if (definedVarEntry == null && definedParamEntry == null) {
            addError(usedVarToken, String.format(ABSemanticMessageHelper.UNDEFINED_VARIABLE, usedVarToken.getValue(), usedVarToken.getRow(), usedVarToken.getCol()));
        } else {

            // Get the defined entry
            ABSymbolTableEntry definedEntry = definedVarEntry != null ? definedVarEntry : definedParamEntry;

            // Check if all arguments are integer, this should not affect the return type
            for(int i=0; i < usedVarTokenSubGroup.getArgumentsSize(); i++) {
                List<ABToken> argument = usedVarTokenSubGroup.getArgumentList(i);

                // If argument is not valid, or is not exactly one token, or is not of type integer
                if(argument == null || argument.size() != 1 || !argument.get(0).getToken().equals(IdentifierHelper.ReservedWords.INT.getToken()))
                    addError(usedVarToken, String.format(ABSemanticMessageHelper.ARRAY_INDEX_NON_INTEGER, usedVarToken.getValue(), i+1, usedVarToken.getRow(), usedVarToken.getCol()));
            }

            // If array dimension is larger than the original dimension
            if(usedVarTokenSubGroup.getArgumentsSize() > definedEntry.getArrayDimension()) {
                addError(usedVarToken, String.format(ABSemanticMessageHelper.ARRAY_LARGER_DIMENSION, usedVarToken.getValue(), usedVarTokenSubGroup.getArgumentsSize(), usedVarToken.getRow(), usedVarToken.getCol()));

            } else {
                // Store in map
                tokenEntryMap.put(usedVarToken, definedEntry);

                // Set return type
                usedVarTokenSubGroup.generateReturnType(definedEntry);
            }
        }
    }

    /**
     * Check variable data member
     */
    public void checkVariableDataMember() {

        ABSemanticTokenGroup group = tokenGroupsStack.peek();

        // Get the second to last token sub group
        ABSemanticTokenGroup.ABSemanticTokenSubGroup baseSubGroup = group.getSecondToLastTokenSubGroup();

        // Get the last token sub group
        ABSemanticTokenGroup.ABSemanticTokenSubGroup memberSubGroup = group.getLastTokenSubGroup();

        // Input token
        ABToken baseInputToken = baseSubGroup.getUsedToken();
        ABToken memberInputToken = memberSubGroup.getUsedToken();

        // Get base token table entry
        ABSymbolTableEntry baseTableEntry = tokenEntryMap.get(baseInputToken);

        // If not found or previous variable doesn't have a complete type
        if(baseTableEntry == null || !checkArray(baseTableEntry, baseSubGroup)) {
            addError(memberInputToken, String.format(ABSemanticMessageHelper.UNDEFINED_MEMBER_OF_UNDEFINED_VARIABLE, memberInputToken.getValue(), memberInputToken.getRow(), memberInputToken.getCol()));
        } else {

            // If primitive type
            if(baseTableEntry.isPrimitiveType()) {
                addError(memberInputToken, String.format(ABSemanticMessageHelper.UNDEFINED_MEMBER_OF_PRIMITIVE_VARIABLE, memberInputToken.getValue(), memberInputToken.getRow(), memberInputToken.getCol()));
            } else {

                // Check if the type is defined
                ABSymbolTableEntry classEntry = searchEntryInTable(globalTable, baseTableEntry.getType().get(0).getValue(), ABSymbolTableEntry.Kind.CLASS);

                // If not found
                if(classEntry == null) {
                    addError(memberInputToken, String.format(ABSemanticMessageHelper.UNDEFINED_MEMBER_OF_UNDEFINED_VARIABLE, memberInputToken.getValue(), memberInputToken.getRow(), memberInputToken.getCol()));
                } else {
                    // Search for data member
                    ABSymbolTableEntry entry = searchEntryInTable(classEntry.getLink(), memberInputToken.getValue(), ABSymbolTableEntry.Kind.VARIABLE);

                    // If not found
                    if(entry == null) {
                        addError(memberInputToken, String.format(ABSemanticMessageHelper.UNDEFINED_MEMBER_OF_CLASS, memberInputToken.getValue(), classEntry.getName(), memberInputToken.getRow(), memberInputToken.getCol()));
                    } else {

                        // Check if all arguments are integer, this should not affect the return type
                        for(int i=0; i < memberSubGroup.getArgumentsSize(); i++) {
                            List<ABToken> argument = memberSubGroup.getArgumentList(i);

                            // If argument is not valid, or is not exactly one token, or is not of type integer
                            if(argument == null || argument.size() != 1 || !argument.get(0).getToken().equals(IdentifierHelper.ReservedWords.INT.getToken()))
                                addError(memberInputToken, String.format(ABSemanticMessageHelper.ARRAY_INDEX_NON_INTEGER, memberInputToken.getValue(), i+1, memberInputToken.getRow(), memberInputToken.getCol()));
                        }

                        // If array dimension is larger than the original dimension
                        if(memberSubGroup.getArgumentsSize() > entry.getArrayDimension()) {
                            addError(memberInputToken, String.format(ABSemanticMessageHelper.ARRAY_LARGER_DIMENSION, memberInputToken.getValue(), memberSubGroup.getArgumentsSize(), memberInputToken.getRow(), memberInputToken.getCol()));

                        } else {
                            // Store in map
                            tokenEntryMap.put(memberInputToken, entry);

                            // Set return type
                            memberSubGroup.generateReturnType(entry);
                        }
                    }
                }
            }
        }
    }

    /**
     * Check if the function was used correctly
     */
    public void checkFunctionUse() {

        // Load last entered group
        ABSemanticTokenGroup usedFunctionTokenGroup = tokenGroupsStack.peek();

        // Get the function name
        ABToken usedFunctionToken = usedFunctionTokenGroup.getLastTokenSubGroup().getUsedToken();

        // Input token
        List<ABSymbolTableEntry> entries = searchEntriesInTableStack(tablesStack, usedFunctionToken.getValue(), ABSymbolTableEntry.Kind.FUNCTION);

        // Found
        boolean found = false;

        // Check if there's any match
        for(ABSymbolTableEntry entry : entries) {

            if(checkFunctionParameters(entry, usedFunctionTokenGroup)) {
                tokenEntryMap.put(usedFunctionToken, entry);

                // Generate return type
                usedFunctionTokenGroup.getLastTokenSubGroup().generateReturnType(entry);

                found = true;
                break;
            }
        }

        // If not found
        if(!found) {
            addError(usedFunctionToken, String.format(ABSemanticMessageHelper.UNDEFINED_FUNCTION, usedFunctionToken.getValue(), usedFunctionToken.getRow(), usedFunctionToken.getCol()));
        }
    }

    /**
     * Check function data member
     */
    public void checkFunctionDataMember() {

        ABSemanticTokenGroup group = tokenGroupsStack.peek();

        // Get the second to last token sub group
        ABSemanticTokenGroup.ABSemanticTokenSubGroup baseSubGroup = group.getSecondToLastTokenSubGroup();

        // Get the last token sub group
        ABSemanticTokenGroup.ABSemanticTokenSubGroup memberSubGroup = group.getLastTokenSubGroup();

        // Input token
        ABToken baseInputToken = baseSubGroup.getUsedToken();
        ABToken memberInputToken = memberSubGroup.getUsedToken();

        // Get last token table entry
        ABSymbolTableEntry baseTableEntry = tokenEntryMap.get(baseInputToken);

        // If not found or previous variable doesn't have a complete type
        if(baseTableEntry == null || !checkArray(baseTableEntry, baseSubGroup)) {
            addError(memberInputToken, String.format(ABSemanticMessageHelper.UNDEFINED_MEMBER_OF_UNDEFINED_VARIABLE, memberInputToken.getValue(), memberInputToken.getRow(), memberInputToken.getCol()));
        } else {

            // If primitive type
            if (baseTableEntry.isPrimitiveType()) {
                addError(memberInputToken, String.format(ABSemanticMessageHelper.UNDEFINED_MEMBER_OF_PRIMITIVE_VARIABLE, memberInputToken.getValue(), memberInputToken.getRow(), memberInputToken.getCol()));
            } else {

                // Check if the type is defined
                ABSymbolTableEntry classEntry = searchEntryInTable(globalTable, baseTableEntry.getType().get(0).getValue(), ABSymbolTableEntry.Kind.CLASS);

                // If not found
                if (classEntry == null) {
                    addError(memberInputToken, String.format(ABSemanticMessageHelper.UNDEFINED_MEMBER_OF_UNDEFINED_VARIABLE, memberInputToken.getValue(), memberInputToken.getRow(), memberInputToken.getCol()));
                } else {
                    // Search for data member
                    List<ABSymbolTableEntry> entries = searchEntriesInTable(classEntry.getLink(), memberInputToken.getValue(), ABSymbolTableEntry.Kind.FUNCTION);

                    // Found
                    boolean found = false;

                    // Search in functions
                    for (ABSymbolTableEntry entry : entries) {

                        // If function match
                        if (checkFunctionParameters(entry, group)) {
                            tokenEntryMap.put(memberInputToken, entry);

                            // Generate return type
                            memberSubGroup.generateReturnType(entry);

                            found = true;
                            break;
                        }
                    }

                    // If not found
                    if (!found) {
                        addError(memberInputToken, String.format(ABSemanticMessageHelper.UNDEFINED_MEMBER_OF_CLASS, memberInputToken.getValue(), classEntry.getName(), memberInputToken.getRow(), memberInputToken.getCol()));
                    }
                }
            }
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
    public ABSymbolTableEntry checkTypeExists(ABSymbolTableEntry entry) {

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

    /**
     * Compare two token groups types to perform an arithmetic operation
     * @param LHS
     * @param RHS
     * @param arithOp
     * @return
     */
    public List<ABToken> checkArithmeticType(ABSemanticTokenGroup LHS, ABSemanticTokenGroup RHS, ABToken arithOp) {
        List<ABToken> LHSType = LHS.getLastReturnType();
        List<ABToken> RHSType = RHS.getLastReturnType();
        String LHSTypeString = LHS.getLastTokenSubGroup().getReturnTypeAsString();
        String RHSTypeString = RHS.getLastTokenSubGroup().getReturnTypeAsString();

        // If both are not defined
        if(LHSType == null && RHSType == null) {
            addError(arithOp, String.format(ABSemanticMessageHelper.ARITHMETIC_TWO_UNDEFINED, arithOp.getValue(), arithOp.getRow(), arithOp.getCol()));

        // If LHS is undefined
        } else if(LHSType == null) {
            addError(arithOp, String.format(ABSemanticMessageHelper.ARITHMETIC_ONE_UNDEFINED, arithOp.getValue(), RHSTypeString, arithOp.getRow(), arithOp.getCol()));

        // If RHS is undefined
        } else if(RHSType == null) {
            addError(arithOp, String.format(ABSemanticMessageHelper.ARITHMETIC_ONE_UNDEFINED, arithOp.getValue(), LHSTypeString, arithOp.getRow(), arithOp.getCol()));

        // If type size is different or they are both not of size 1
        } else if(LHSType.size() != RHSType.size() || LHSType.size() != 1) {
            addError(arithOp, String.format(ABSemanticMessageHelper.ARITHMETIC_TYPE, arithOp.getValue(), LHSTypeString, RHSTypeString, arithOp.getRow(), arithOp.getCol()));

        } else {

            // Get left and right tokens
            String leftTypeToken = LHSType.get(0).getToken();
            String rightTypeToken = RHSType.get(0).getToken();

            // Get primitive types tokens
            String intTypeToken = IdentifierHelper.ReservedWords.INT.getToken();
            String floatTypeToken = IdentifierHelper.ReservedWords.FLOAT.getToken();

            // If both are integer, arbitrary choice because only the token type is important and not the value
            if(leftTypeToken.equals(intTypeToken) && rightTypeToken.equals(intTypeToken)) {
                return LHSType;

            // If both are float, arbitrary choice because only the token type is important and not the value
            } else if(leftTypeToken.equals(floatTypeToken) && rightTypeToken.equals(floatTypeToken)) {
                return LHSType;

            // Else we can't conclude a return value
            } else {
                addError(arithOp, String.format(ABSemanticMessageHelper.ARITHMETIC_TYPE, arithOp.getValue(), LHSTypeString, RHSTypeString, arithOp.getRow(), arithOp.getCol()));
            }
        }

        return null;
    }

    /**
     * Check assignment types if match
     * @param LHS
     * @param RHS
     * @param assignOp
     */
    public void checkAssignment(ABSemanticTokenGroup LHS, ABSemanticTokenGroup RHS, ABToken assignOp) {

        // Get return types
        List<ABToken> LHSType = LHS.getLastReturnType();
        List<ABToken> RHSType = RHS.getLastReturnType();

        if(LHSType == null && RHSType == null) {
            addError(assignOp, String.format(ABSemanticMessageHelper.ASSIGNMENT_TWO_UNDEFINED, assignOp.getRow(), assignOp.getCol()));

        } else if(LHSType == null) {
            addError(assignOp, String.format(ABSemanticMessageHelper.ASSIGNMENT_LHS_UNDEFINED, RHS.getLastTokenSubGroup().getReturnTypeAsString(), LHS.getLastTokenSubGroup().getUsedToken().getValue(), assignOp.getRow(), assignOp.getCol()));

        } else if(RHSType == null) {
            addError(assignOp, String.format(ABSemanticMessageHelper.ASSIGNMENT_RHS_UNDEFINED, LHS.getLastTokenSubGroup().getReturnTypeAsString(), assignOp.getRow(), assignOp.getCol()));

        } else {

            // If different size
            if(LHSType.size() != RHSType.size()) {
                addError(assignOp, String.format(ABSemanticMessageHelper.ASSIGNMENT_INCOMPATIBLE, RHS.getLastTokenSubGroup().getReturnTypeAsString(), LHS.getLastTokenSubGroup().getReturnTypeAsString(), assignOp.getRow(), assignOp.getCol()));

            } else {

                // Compare each type token
                for(int i=0; i < LHSType.size(); i++) {
                    if(!LHSType.get(i).getToken().equals(RHSType.get(i).getToken())) {
                        addError(assignOp, String.format(ABSemanticMessageHelper.ASSIGNMENT_INCOMPATIBLE, RHS.getLastTokenSubGroup().getReturnTypeAsString(), LHS.getLastTokenSubGroup().getReturnTypeAsString(), assignOp.getRow(), assignOp.getCol()));
                        break;
                    }
                }
            }
        }
    }

    /**
     * Generates sizes for tables and entries
     * @param  table
     */
    public void generateTableAndEntrySize(ABSymbolTable table) {

        // If table size was not calculated
        if(table.getSizeInBytes() < 0) {

            // Total size
            int totalSize = 0;

            // Loop on all entries
            for(ABSymbolTableEntry entry : table.getRows()) {

                // If has a table
                if(entry.getLink() != null) {

                    // Generate size for table
                    generateTableAndEntrySize(entry.getLink());

                    // Set size for entry
                    entry.setSizeInBytes(entry.getLink().getSizeInBytes());
                // If variable or parameter
                } else {

                    // Load type
                    List<ABToken> typeList = entry.getType();

                    // Get type
                    ABToken type = typeList.get(0);

                    // If integer
                    if(type.getToken().equals(ABTokenHelper.T_INT_TYPE)) {
                        entry.setSizeInBytes(ABArchitectureHelper.Size.INTEGER.getSizeInByte());

                    // If float
                    } else if(type.getToken().equals(ABTokenHelper.T_FLOAT_TYPE)) {
                        entry.setSizeInBytes(ABArchitectureHelper.Size.FLOAT.getSizeInByte());

                    // If other type
                    } else {

                        // Search for table
                        ABSymbolTableEntry typeClass = searchEntryInTable(globalTable, type.getValue(), ABSymbolTableEntry.Kind.CLASS);

                        // Get size
                        int typeClassSize = typeClass.getSizeInBytes();

                        // If size was not calculated, then calculate it
                        if(typeClassSize < 0) {
                            generateTableAndEntrySize(typeClass.getLink());
                            typeClassSize = typeClass.getSizeInBytes();
                        }
                        entry.setSizeInBytes(typeClassSize);
                    }

                    // Check if it's an array
                    for(int i=1; i < typeList.size(); i++) {
                        entry.setSizeInBytes(entry.getSizeInBytes() * Integer.parseInt(typeList.get(i).getValue()));
                    }

                    // Update table size
                    totalSize += entry.getSizeInBytes();
                }
            }

            // Set total size
            table.setSizeInBytes(totalSize);
        }
    }

    /**
     * Generate label for each entry
     */
    public void generateEntryLabel() {
        int uniqueId = 1;

        Queue<ABSymbolTable> tableQueue = new LinkedList<>();
        tableQueue.offer(globalTable);
        while(!tableQueue.isEmpty()) {

            // Get table
            ABSymbolTable table = tableQueue.poll();

            // Loop on entries
            for(ABSymbolTableEntry entry : table.getRows()) {

                // Generate label
                entry.setLabel(getAlphaLabel(uniqueId++));

                // If has entry, queue it
                if(entry.getLink() != null)
                    tableQueue.offer(entry.getLink());
            }
        }
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

    /**
     * Search all entries in table stack
     * @param tablesStack
     * @param name
     * @return
     */
    public List<ABSymbolTableEntry> searchEntriesInTableStack(Stack<ABSymbolTable> tablesStack, String name) {
        return searchEntriesInTableStack(tablesStack, name, ABSymbolTableEntry.Kind.ANY);
    }


    /**
     * Search all entries in table stack
     * @param tablesStack
     * @param name
     * @param kind
     * @return
     */
    public List<ABSymbolTableEntry> searchEntriesInTableStack(Stack<ABSymbolTable> tablesStack, String name, ABSymbolTableEntry.Kind kind) {
        Stack<ABSymbolTable> closestTables = new Stack<>();
        List<ABSymbolTableEntry> results = new ArrayList<>();

        // Search starting from current table
        while(!tablesStack.isEmpty()) {

            // Entry
            List<ABSymbolTableEntry> entries = tablesStack.peek().getEntries(name, kind);

            // Add all entries found
            for(ABSymbolTableEntry entry : entries) {
                results.add(entry);
            }

            // Push in tmp stack
            closestTables.push(tablesStack.pop());
        }

        // Put all tables back into stack
        while(!closestTables.isEmpty())
            tablesStack.push(closestTables.pop());

        return results;
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
        abTranslation.setGenerateCode(false);
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

    /**
     * Generate unique label for each integer
     * @param n
     * @return
     */
    private String getAlphaLabel(int n) {
        char[] buf = new char[(int) Math.floor(Math.log(25 * (n + 1)) / Math.log(26))];
        for (int i = buf.length - 1; i >= 0; i--) {
            n--;
            buf[i] = (char) ('A' + n % 26);
            n /= 26;
        }
        return new String(buf);
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

        private List<ABSemanticTokenSubGroup> tokensSubGroups;

        public ABSemanticTokenGroup() {
            tokensSubGroups = new ArrayList<>();
        }

        public void addSubGroupToken(ABToken newToken) {
            ABSemanticTokenSubGroup tokenGroup = new ABSemanticTokenSubGroup();
            tokenGroup.setUsedToken(newToken);
            tokensSubGroups.add(tokenGroup);
        }

        public ABSemanticTokenSubGroup getLastTokenSubGroup() {
            return getTokenSubGroup(tokensSubGroups.size()-1);
        }

        public ABSemanticTokenSubGroup getSecondToLastTokenSubGroup() {
            return getTokenSubGroup(tokensSubGroups.size()-2);
        }

        public ABSemanticTokenSubGroup getTokenSubGroup(int index) {
            return tokensSubGroups.get(index);
        }

        public List<ABSemanticTokenSubGroup> getTokensSubGroups() {
            return tokensSubGroups;
        }

        public List<ABToken> getLastReturnType() {
            return getLastTokenSubGroup().getReturnTypeList();
        }

        public List<ABToken> getSecondToLastReturnType() {
            return getSecondToLastTokenSubGroup().getReturnTypeList();
        }

        @Override
        public String toString() {
            return tokensSubGroups.toString();
        }

        /*****************************
         *  AB SEMANTIC TOKEN SUB-GROUP
         *****************************/

        public class ABSemanticTokenSubGroup {
            private ABToken usedToken;
            private List<List<ABToken>> argumentsTypes;
            private List<ABToken> returnTypeList;

            public ABSemanticTokenSubGroup() {
                argumentsTypes = new ArrayList<>();
            }

            public void addArgument(List<ABToken> argumentType) {argumentsTypes.add(argumentType);}

            public List<ABToken> getArgumentList(int index) { return argumentsTypes.get(index); }

            public int getArgumentsSize() { return argumentsTypes.size(); }

            public void setUsedToken(ABToken usedToken) {
            this.usedToken = usedToken;}

            public ABToken getUsedToken() {
                return usedToken;
            }

            public List<ABToken> getReturnTypeList() {
                return returnTypeList;
            }

            public void generateReturnType(ABSymbolTableEntry entry) {
                returnTypeList = new ArrayList<>();
                returnTypeList.add(entry.getType().get(0));
                for(int i = 1 + getArgumentsSize(); i < entry.getType().size(); i++)
                    returnTypeList.add(entry.getType().get(i));
            }

            public void setReturnTypeList(List<ABToken> returnTypeList) {
                this.returnTypeList = returnTypeList;
            }

            public String getReturnTypeAsString() {
                String output = "No type";
                if(returnTypeList != null) {
                    output = returnTypeList.get(0).getValue();
                    for (int i = 1; i < returnTypeList.size(); i++)
                        output += "[" + returnTypeList.get(i).getValue() + "]";
                }
                return output;
            }

            @Override
            public String toString() {
                return usedToken + " Args: " + argumentsTypes + " Return: " + returnTypeList;
            }
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

    /*****************************************************
     *
     *                   DEBUG METHODS
     *
     *****************************************************/

    /**
     * Check if the grammar handles the stack correctly
     */
    public void checkStructureErrors() {
        boolean error = false;

        if(tokenGroupsStack.size() > 0) {
            l.error("Token group stack is not empty: " + tokenGroupsStack.toString());
            error = true;
        }

        if(arithOpStack.size() > 0) {
            l.error("Symbol stack is not empty: " + arithOpStack.toString());
            error = true;
        }

        if(tablesStack.size() > 0) {
            l.error("Table stack is not empty: " + tablesStack.toString());
            error = true;
        }

        if(!error)
            l.debug("No structure error detected");
    }

    /**
     * Get Translation instance
     * @return
     */
    public ABTranslation getAbTranslation() {
        return abTranslation;
    }
}

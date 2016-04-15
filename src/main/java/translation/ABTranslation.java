package translation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import scanner.ABToken;
import scanner.helper.ABTokenHelper;
import semantic.ABSemantic;
import semantic.ABSymbolTable;
import semantic.ABSymbolTableEntry;
import translation.helper.ABArchitectureHelper;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Amir on 4/10/2016.
 */
public class ABTranslation {

    // Logger
    private Logger l = LogManager.getFormatterLogger(getClass());

    // Constants
    private final int MAX_REGISTERS = 16; // 0 ... 15
    private final int ZERO_REGISTER = 0; // Always zero
    private final int ADDRESS_REGISTER = 15; // Store the address
    private final String STACK = "stack";
    private final String BUF = "buf";
    private final String TOP_ADDRESS = "topaddr";
    private final String STACK_SIZE = (4*300) + "";
    private final String BUF_SIZE = 20 + "";

    // Components
    private String entry = "";
    private String functions = "";
    private String footer = "";
    private String buffer = "";
    private ABSemantic abSemantic;
    private Mode mode;
    private Reason errorReason;
    private boolean error = false;

    // Mode
    private Stack<Mode> modes;
    public enum Mode {
        FUNCTION,
        ENTRY,
        BUFFER
    }

    public enum Reason {
        SEMANTIC_ERROR,
        OUT_OF_REGISTERS
    };

    // Map
    private Map<List<ABToken>, Register> resultRegisterMap;
    private Map<ABSemantic.ABSemanticTokenGroup, Register> groupRegisterMap;

    // Labels
    private int unique_id = 0;
    private Stack<String> labels;

    public ABTranslation(ABSemantic abSemantic) {

        // Init components
        this.abSemantic = abSemantic;
        resultRegisterMap = new HashMap<>();
        groupRegisterMap = new HashMap<>();
        labels = new Stack<>();
        Register.reset();
        modes = new Stack<>();
    }

    /**
     * Get header
     * @return
     */
    public String getHeader() {

        // Prepare header
        String header = "";

        // Add data to header
        header += "% The following code is generated automatically by ABCompiler\n";
        header += "%\tTime generated: " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()) + "\n\n";
        header += generateLine(true, Instruction.ENTRY.getName()) + "\n";
        header += generateLine(true, Instruction.ALIGN.getName()) + "\n";

        // Reset pointer
        header += generateLine(true, Instruction.ADDI.getName(), Register.R14.getName(), Register.R0.getName(), TOP_ADDRESS) + "% Set stack pointer\n";

        return header;
    }

    /**
     * Append information on the footer
     * @param table
     */
    public void appendFooter(ABSymbolTable table) {

        // Loop on entries
        for(int i=0; i < table.getRows().size(); i++) {

            // Cache value
            ABSymbolTableEntry entry = table.getRows().get(i);

            // Data
            String instruction;
            int value;

            switch (entry.getKind()) {
                case PARAMETER:
                case VARIABLE:

                    // If 1 word
                    if(entry.getSizeInBytes() == ABArchitectureHelper.Size.INTEGER.getSizeInByte()) {
                        instruction = Instruction.DW.getName();
                        value = 0;
                    } else {
                        instruction = Instruction.RES.getName();
                        value = entry.getSizeInBytes();
                    }

                    footer += String.format("%-15s %-15s %-15s", entry.getLabel(), instruction, value+"") + "% " + entry.getDetails();
                    footer += "\n";
                    break;

                case FUNCTION:

                    // FIXME Handle int as return type
                    instruction = Instruction.DW.getName();
                    value = 0;

                    footer += String.format("%-15s %-15s %-15s", entry.getReturnLabel(), instruction, value+"") + "% " + entry.getDetails();
                    footer += "\n";
                    break;

            }
        }
    }

    /**
     * Disable code generation
     * @param reason
     */
    public void foundError(Reason reason) {
        this.error = true;
        this.errorReason = reason;
    }

    /**
     * Allow code generation
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    /**
     * Get mode
     * @return
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Generate the final code
     * @return
     */
    public String generateCode() {
        if(!error) {

            // Halt
            entry += generateLine(true, Instruction.HLT.getName()) + "\n";

            // Functions comment
            functions = "\n% Functions\n" + functions;

            // Footer comment
            footer = "\n% Footer\n" + footer;

            // Stack
            footer += String.format("%-15s %-15s %-15s", STACK, Instruction.RES.getName(), STACK_SIZE) + "% Allocating memory for the stack\n";
            footer += String.format("%-15s %-15s %-15s", BUF, Instruction.RES.getName(), BUF_SIZE) + "% Allocating memory for the buffer\n";

            return getHeader() + entry + functions + footer;

        } else if(errorReason == Reason.SEMANTIC_ERROR) {
            return "% Couldn't generate code because of one or more semantic errors";

        } else if(errorReason == Reason.OUT_OF_REGISTERS) {
            return "% Couldn't generate code because the program requires more registers";

        } else {
            return "% Unexpected error";
        }
    }

    /*****************************************************
     *
     *                  GENERATE METHODS
     *
     *****************************************************/

    /**
     * Generate code for arithmetic operation
     * @param LHS
     * @param RHS
     * @param arithOp
     * @param result
     */
    public void generateArithmeticOperation(ABSemantic.ABSemanticTokenGroup LHS, ABSemantic.ABSemanticTokenGroup RHS, ABToken arithOp, List<ABToken> result) {

        // If error
        if(error) return;

        switch (arithOp.getToken()) {
            case ABTokenHelper.T_PLUS:
                performArith(LHS, RHS, Instruction.ADD, Instruction.ADDI, result, arithOp);
                break;
            case ABTokenHelper.T_MINUS:
                performArith(LHS, RHS, Instruction.SUB, Instruction.SUBI, result, arithOp);
                break;
            case ABTokenHelper.T_OR:
                performArith(LHS, RHS, Instruction.OR, Instruction.ORI, result, arithOp);
                break;
            case ABTokenHelper.T_MULTIPLY:
                performArith(LHS, RHS, Instruction.MUL, Instruction.MULI, result, arithOp);
                break;
            case ABTokenHelper.T_DIVIDE:
                performArith(LHS, RHS, Instruction.DIV, Instruction.DIVI, result, arithOp);
                break;
            case ABTokenHelper.T_AND:
                performArith(LHS, RHS, Instruction.AND, Instruction.ANDI, result, arithOp);
                break;
            case ABTokenHelper.T_GREATER_THAN:
                performArith(LHS, RHS, Instruction.CGT, Instruction.CGTI, result, arithOp);
                break;
            case ABTokenHelper.T_LESS_THAN:
                performArith(LHS, RHS, Instruction.CLT, Instruction.CLTI, result, arithOp);
                break;
            case ABTokenHelper.T_LESS_OR_EQUAL:
                performArith(LHS, RHS, Instruction.CLE, Instruction.CLEI, result, arithOp);
                break;
            case ABTokenHelper.T_GREATER_OR_EQUAL:
                performArith(LHS, RHS, Instruction.CGE, Instruction.CGEI, result, arithOp);
                break;
            case ABTokenHelper.T_IS_NOT_EQUAL:
                performArith(LHS, RHS, Instruction.CNE, Instruction.CNEI, result, arithOp);
                break;
            case ABTokenHelper.T_IS_EQUAL:
                performArith(LHS, RHS, Instruction.CEQ, Instruction.CEQI, result, arithOp);
                break;
        }
    }

    /**
     * Generate logical check
     * @param expr
     */
    public void generateLogicalCheck(ABSemantic.ABSemanticTokenGroup expr) {

        // If error
        if(error) return;

        // Cache info
        ABToken exprToken = expr.getLastTokenSubGroup().getUsedToken();

        // Register
        Register leftRegister;

        // If expr is a result
        if(exprToken == null) {
            leftRegister = getRegisterOfResult(expr.getLastTokenSubGroup().getReturnTypeList());

        // If expr is not a result
        } else {

            // If expr is an identifier
            if(exprToken.isIdentifier()) {

                // Get register
                leftRegister = Register.getRegisterNotInUse();
                acquire(leftRegister);

                // If can't reserve register
                if(registerNotFound(leftRegister))
                    return;

                // Entry
                ABSymbolTableEntry exprEntry = abSemantic.getEntryOf(exprToken);

                // Load
                addCode(generateLine(true, Instruction.LW.getName(), leftRegister.getName(), getDataAt(exprEntry.getLabel(), Register.R0)) + "% Load " + exprEntry.getDetails());
                newLine();

                // If expr is #
            } else {

                // Get register
                leftRegister = Register.getRegisterNotInUse();
                acquire(leftRegister);

                // If can't reserve register
                if(registerNotFound(leftRegister))
                    return;

                // Load
                addCode(generateLine(true, Instruction.ADDI.getName(), leftRegister.getName(), Register.R0.getName(), exprToken.getValue()) + "% 0 + " + exprToken.getValue());
                newLine();
            }
        }

        // Generate labels
        String zeroLabel = generateUniqueLabel("zero", false);
        String endLabel = generateUniqueLabel("end", false);

        // Branch zero
        addCode(generateLine(true, Instruction.BZ.getName(), leftRegister.getName(), zeroLabel));
        newLine();

        // Adjust value
        addCode(generateLine(true, Instruction.ADDI.getName(), leftRegister.getName(), Register.R0.getName(), "1"));
        newLine();

        // Jump
        addCode(generateLine(true, Instruction.J.getName(), endLabel));
        newLine();

        // Adjust value
        addCode(generateLine(false, zeroLabel, Instruction.ADDI.getName(), leftRegister.getName(), Register.R0.getName(), "0"));
        newLine();

        // End
        addCode(generateLine(false, endLabel, Instruction.SUBI.getName(), Register.R0.getName(), Register.R0.getName(), "0"));
        newLine();

        // Store
        storeGroupAtRegister(expr, leftRegister);
    }

    /**
     * Generate logical check
     * @param expr
     */
    public void generateIfCheck(ABSemantic.ABSemanticTokenGroup expr) {

        // If error
        if(error) return;

        // Register
        Register leftRegister = getRegisterOfGroup(expr);

        // Generate labels
        String elseLabel = generateUniqueLabel("else", true);

        // Branch zero
        addCode(generateLine(true, Instruction.BZ.getName(), leftRegister.getName(), elseLabel));
        newLine();

        // Release registers
        release(leftRegister);
    }

    /**
     * Generate else code
     */
    public void generateElseCheck() {

        // If error
        if(error) return;

        // Labels
        String elseLabel = getLastGeneratedLabel();
        String endIfLabel = generateUniqueLabel("endif", true);

        // Jump
        addCode(generateLine(true, Instruction.J.getName(), endIfLabel));
        newLine();

        // Label
        addCode(generateLine(false, elseLabel, Instruction.SUBI.getName(), Register.R0.getName(), Register.R0.getName(), "0"));
        newLine();
    }

    /**
     * Generate end if code
     */
    public void generateEndIf() {

        // If error
        if(error) return;

        // Labels
        String endIfLabel = getLastGeneratedLabel();

        // Jump
        addCode(generateLine(false, endIfLabel, Instruction.SUBI.getName(), Register.R0.getName(), Register.R0.getName(), "0"));
        newLine();

    }

    /**
     * Generate assignment code
     * @param LHS
     * @param RHS
     */
    public void generateAssignment(ABSemantic.ABSemanticTokenGroup LHS, ABSemantic.ABSemanticTokenGroup RHS) {

        // If error
        if(error) return;

        // Registers
        Register leftRegister = null;

        // Entries
        ABSymbolTableEntry LHSEntry = null;
        ABSymbolTableEntry RHSEntry = null;

        // Cache info
        ABToken LHSToken = LHS.getLastTokenSubGroup().getUsedToken();
        ABToken RHSToken = RHS.getLastTokenSubGroup().getUsedToken();

        // If RHS is a result
        if(RHSToken == null) {

            // Get result register
            leftRegister = getRegisterOfResult( RHS.getLastTokenSubGroup().getReturnTypeList());

            // Get entry
            LHSEntry = abSemantic.getEntryOf(LHSToken);

            // Offset register
            Register offsetLeftRegister = getRegisterOffsetAndAcquire(LHS);

            // If not found
            if(registerNotFound(offsetLeftRegister))
                return;

            // Add them
            addCode(generateLine(true, Instruction.SW.getName(), getDataAt(LHSEntry.getLabel(), offsetLeftRegister), leftRegister.getName()) + "% " + LHSEntry.getName() + " = ANS");
            newLine();

            // Release register
            release(leftRegister);
            release(offsetLeftRegister);

            // If RHS is not a result
        }  else {

            // If RHS is an identifier
            if(RHSToken.isIdentifier()) {

                // Adjust entries
                LHSEntry = abSemantic.getEntryOf(LHSToken);
                RHSEntry = abSemantic.getEntryOf(RHSToken);

                leftRegister = Register.getRegisterNotInUse();
                acquire(leftRegister);

                // If can't reserve registers
                if(registerNotFound(leftRegister))
                    return;

                // Offset register
                Register offsetRightRegister = getRegisterOffsetAndAcquire(RHS);

                if(registerNotFound(offsetRightRegister))
                    return;

                // Load LHS
                addCode(generateLine(true, Instruction.LW.getName(), leftRegister.getName(), getDataAt(RHSEntry.getLabel(), offsetRightRegister)) + "% Load " + RHSEntry.getDetails());
                newLine();

                // Offset register
                Register offsetLeftRegister = getRegisterOffsetAndAcquire(LHS);

                if(registerNotFound(offsetLeftRegister))
                    return;

                // Add them
                addCode(generateLine(true, Instruction.SW.getName(), getDataAt(LHSEntry.getLabel(), offsetLeftRegister),  leftRegister.getName()) + "% " + LHSEntry.getName() + " = " + RHSEntry.getName());
                newLine();

                // Release
                release(leftRegister);
                release(offsetLeftRegister);
                release(offsetRightRegister);

                // If RHS is not an identifier
            } else {

                // Adjust entries
                LHSEntry = abSemantic.getEntryOf(LHSToken);

                // Get register
                leftRegister = Register.getRegisterNotInUse();
                acquire(leftRegister);

                // If can't reserve registers
                if(registerNotFound(leftRegister))
                    return;

                // Load LHS
                addCode(generateLine(true, Instruction.ADDI.getName(), leftRegister.getName(),  Register.R0.getName(), RHSToken.getValue()) + "% " +  "0 + " + RHSToken.getValue());
                newLine();

                // Offset register
                Register offsetLeftRegister = getRegisterOffsetAndAcquire(LHS);
                acquire(offsetLeftRegister);

                // Add them
                addCode(generateLine(true, Instruction.SW.getName(), getDataAt(LHSEntry.getLabel(), offsetLeftRegister),  leftRegister.getName()) + "% " + LHSEntry.getName() + " = ANS");
                newLine();

                // Release
                release(leftRegister);
                release(offsetLeftRegister);
            }
        }

    }

    /**
     * Generate code for get
     * @param group
     */
    public void generateGet(ABSemantic.ABSemanticTokenGroup group) {

        // If error
        if(error) return;

        // Cache info
        ABToken varToken = group.getLastTokenSubGroup().getUsedToken();

        // Get register
        Register leftRegister = Register.getRegisterNotInUse();
        acquire(leftRegister);

        // If can't reserve register
        if(registerNotFound(leftRegister))
            return;

        // Entry
        ABSymbolTableEntry varEntry = abSemantic.getEntryOf(varToken);

        // Get
        addCode(generateLine(true, Instruction.GETC.getName(), leftRegister.getName()));
        newLine();

        // Store
        addCode(generateLine(true, Instruction.SW.getName(), getDataAt(varEntry.getLabel(), Register.R0), leftRegister.getName()));
        newLine();

        // Release registers
        release(leftRegister);
    }

    /**
     * Generate code for get
     * @param expr
     */
    public void generatePut(ABSemantic.ABSemanticTokenGroup expr) {

        // If error
        if(error) return;

        // Cache info
        ABToken exprToken = expr.getLastTokenSubGroup().getUsedToken();

        // Register
        Register leftRegister;

        // If expr is a result
        if(exprToken == null) {
            leftRegister = getRegisterOfResult(expr.getLastTokenSubGroup().getReturnTypeList());

            // If expr is not a result
        } else {

            // If expr is an identifier
            if(exprToken.isIdentifier()) {

                // Get register
                leftRegister = Register.getRegisterNotInUse();
                acquire(leftRegister);

                // If can't reserve register
                if(registerNotFound(leftRegister))
                    return;

                // Entry
                ABSymbolTableEntry exprEntry = abSemantic.getEntryOf(exprToken);

                // Load
                addCode(generateLine(true, Instruction.LW.getName(), leftRegister.getName(), getDataAt(exprEntry.getLabel(), Register.R0)) + "% Load " + exprEntry.getDetails());
                newLine();

                // If expr is #
            } else {

                // Get register
                leftRegister = Register.getRegisterNotInUse();
                acquire(leftRegister);

                // If can't reserve register
                if(registerNotFound(leftRegister))
                    return;

                // Load
                addCode(generateLine(true, Instruction.ADDI.getName(), leftRegister.getName(), Register.R0.getName(), exprToken.getValue()) + "% 0 + " + exprToken.getValue());
                newLine();
            }
        }

        // Add
        addCode(generateLine(true, Instruction.PUTC.getName(), leftRegister.getName()));
        newLine();

        // Release registers
        release(leftRegister);
    }

    /**
     * Generate function header
     * @param entry
     */
    public void generateFunctionHeader(ABSymbolTableEntry entry) {

        // If error
        if(error) return;

        addCode(generateLine(false, entry.getStaticLabel(), Instruction.SUBI.getName(), Register.R0.getName(), Register.R0.getName(), "0") + "% Start of function: " + entry.getDetails());
        newLine();
    }

    /**
     * Generate for init code
     */
    public void generateForInit() {

        // If error
        if(error) return;

        // Labels
        String forLabel = generateUniqueLabel("for", true);
        String condLabel = generateUniqueLabel("cond", false);

        // Jump
        addCode(generateLine(true, Instruction.J.getName(), condLabel));
        newLine();

        // Reset
        addCode(generateLine(false, forLabel, Instruction.SUBI.getName(), Register.R0.getName(), Register.R0.getName(), "0"));
        newLine();

        // Set mode
        modes.push(getMode());

        // Change mode to buffer
        setMode(Mode.BUFFER);

        // Reset
        addCode(generateLine(false, condLabel, Instruction.SUBI.getName(), Register.R0.getName(), Register.R0.getName(), "0"));
        newLine();
    }

    /**
     * Generate for check code
     */
    public void generateForCheck(ABSemantic.ABSemanticTokenGroup expr) {

        // If error
        if(error) return;

        // Register
        Register leftRegister = getRegisterOfGroup(expr);

        // Generate labels
        String endForLabel = generateUniqueLabel("endfor", true);

        // Branch zero
        addCode(generateLine(true, Instruction.BZ.getName(), leftRegister.getName(), endForLabel));
        newLine();

        // Release registers
        release(leftRegister);

        // Change back the mode
        setMode(modes.pop());
    }

    /**
     * Generate for math code
     */
    public void generateForMath() {

        // If error
        if(error) return;

        // Flush buffer
        addCode(getBufferAndFlush());
    }

    /**
     * Generate end for
     */
    public void generateEndFor() {

        // If error
        if(error) return;

        // Labels
        String endForLabel = getLastGeneratedLabel();
        String forLabel = getLastGeneratedLabel();

        // Jump
        addCode(generateLine(true, Instruction.J.getName(), forLabel));
        newLine();

        // Reset
        addCode(generateLine(false, endForLabel, Instruction.SUBI.getName(), Register.R0.getName(), Register.R0.getName(), "0"));
        newLine();
    }

    /**
     * Generate end of function code
     */
    public void generateEndOfFunction() {

        // If error
        if(error) return;

        // Jump
        addCode(generateLine(true, Instruction.JR.getName(), Register.R15.getName()));
        newLine();

        // Reset params
        release(Register.R10);
        release(Register.R11);
    }

    /**
     * Generate logical check code
     * @param expr
     */
    public void generateFunctionReturn(ABSemantic.ABSemanticTokenGroup expr, ABSemantic.ABSemanticTokenGroup function) {

        // If error
        if(error) return;

        // Cache info
        ABToken exprToken = expr.getLastTokenSubGroup().getUsedToken();

        // Register
        Register leftRegister;

        // If expr is a result
        if(exprToken == null) {
            leftRegister = getRegisterOfResult(expr.getLastTokenSubGroup().getReturnTypeList());

            // If expr is not a result
        } else {

            // If expr is an identifier
            if(exprToken.isIdentifier()) {

                // Get register
                leftRegister = Register.getRegisterNotInUse();
                acquire(leftRegister);

                // If can't reserve register
                if(registerNotFound(leftRegister))
                    return;

                // Entry
                ABSymbolTableEntry exprEntry = abSemantic.getEntryOf(exprToken);

                // Load
                addCode(generateLine(true, Instruction.LW.getName(), leftRegister.getName(), getDataAt(exprEntry.getLabel(), Register.R0)) + "% Load " + exprEntry.getDetails());
                newLine();

                // If expr is #
            } else {

                // Get register
                leftRegister = Register.getRegisterNotInUse();
                acquire(leftRegister);

                // If can't reserve register
                if(registerNotFound(leftRegister))
                    return;

                // Load
                addCode(generateLine(true, Instruction.ADDI.getName(), leftRegister.getName(), Register.R0.getName(), exprToken.getValue()) + "% 0 + " + exprToken.getValue());
                newLine();
            }
        }

        // Function token
        ABToken usedFunctionToken = function.getLastTokenSubGroup().getUsedToken();

        // If entry found - (should always be)
        if(entry != null) {

            // Get function entry
            ABSymbolTableEntry functionEntry = abSemantic.getEntryOf(usedFunctionToken);

            // Branch zero
            addCode(generateLine(true, Instruction.SW.getName(), getDataAt(functionEntry.getReturnLabel(), Register.R0), leftRegister.getName()));
            newLine();
        }

        // Release registers
        release(leftRegister);
    }

    /**
     * Generate parameter passing code
     * @param expr
     */
    public void generateParamPass(ABSemantic.ABSemanticTokenGroup expr) {

        // If error
        if(error) return;

        // Cache info
        ABToken exprToken = expr.getLastTokenSubGroup().getUsedToken();

        // Register
        Register leftRegister;

        // If expr is a result
        if(exprToken == null) {
            leftRegister = getRegisterOfResult(expr.getLastTokenSubGroup().getReturnTypeList());

            // If expr is not a result
        } else {

            // If expr is an identifier
            if(exprToken.isIdentifier()) {

                // Get register
                leftRegister = Register.getRegisterNotInUse();
                acquire(leftRegister);

                // If can't reserve register
                if(registerNotFound(leftRegister))
                    return;

                // Entry
                ABSymbolTableEntry exprEntry = abSemantic.getEntryOf(exprToken);

                // Load
                addCode(generateLine(true, Instruction.LW.getName(), leftRegister.getName(), getDataAt(exprEntry.getLabel(), Register.R0)) + "% Load " + exprEntry.getDetails());
                newLine();

                // If expr is #
            } else {

                // Get register
                leftRegister = Register.getRegisterNotInUse();
                acquire(leftRegister);

                // If can't reserve register
                if(registerNotFound(leftRegister))
                    return;

                // Load
                addCode(generateLine(true, Instruction.ADDI.getName(), leftRegister.getName(), Register.R0.getName(), exprToken.getValue()) + "% 0 + " + exprToken.getValue());
                newLine();
            }
        }

        // Get register
        Register paramRegister = Register.getParamNotInUse();
        acquire(paramRegister);

        // If can't reserve register
        if(registerNotFound(paramRegister))
            return;

        // Store in parameter
        addCode(generateLine(true, Instruction.ADD.getName(), paramRegister.getName(), Register.R0.getName(), leftRegister.getName()) + "% Store ANS in parameter register");
        newLine();
    }

    /**
     * Generate parameter code
     * @param entry
     */
    public void generateParameter(ABSymbolTableEntry entry) {

        // If error
        if(error) return;

        // Get register
        Register paramRegister = Register.getParamNotInUse();
        acquire(paramRegister);

        // If can't reserve register
        if(registerNotFound(paramRegister))
            return;

        // Store
        addCode(generateLine(true, Instruction.SW.getName(), getDataAt(entry.getLabel(), Register.R0), paramRegister.getName()));
        newLine();
    }

    /**
     * Generate code for function call
     * @param function
     */
    public void generateFunctionCall(ABSemantic.ABSemanticTokenGroup function) {

        // If error
        if(error) return;

        // Get token
        ABToken usedToken = function.getLastTokenSubGroup().getUsedToken();

        // Get entry
        ABSymbolTableEntry entry = abSemantic.getEntryOf(usedToken);

        // Jump
        addCode(generateLine(true, Instruction.JL.getName(), Register.R15.getName(), entry.getStaticLabel()));
        newLine();

        // Release param registers
        release(Register.R10);
        release(Register.R11);
    }

    /*****************************************************
     *
     *                  ARITHMETIC HELPERS
     *
     *****************************************************/

    /**
     * Perform arithmetic operations
     * @param LHS
     * @param RHS
     * @param nonImmediate
     * @param immediate
     * @param result
     * @param arithOp
     */
    public void performArith(ABSemantic.ABSemanticTokenGroup LHS, ABSemantic.ABSemanticTokenGroup RHS, Instruction nonImmediate, Instruction immediate, List<ABToken> result, ABToken arithOp) {

        // Cache info
        ABToken LHSToken = LHS.getLastTokenSubGroup().getUsedToken();
        ABToken RHSToken = RHS.getLastTokenSubGroup().getUsedToken();

        // TODO Handle float

        // If LHS and RHS are result - Result + Result
        if(LHSToken == null && RHSToken == null) {
            Register leftRegister = getRegisterOfResult( LHS.getLastTokenSubGroup().getReturnTypeList());
            Register rightRegister = getRegisterOfResult( RHS.getLastTokenSubGroup().getReturnTypeList());

            // INST L L R
            addCode(generateLine(true, nonImmediate.getName(), leftRegister.getName(),  leftRegister.getName(), rightRegister.getName()) + "% ANS " + arithOp.getValue() + " ANS");
            newLine();

            // Release register
            release(rightRegister);

            // Store result
            storeResultAtRegister(result, leftRegister);

            // If LHS is a result - Result + ...
        } else if(LHSToken == null) {

            // If RHS is an identifier - Result + ID
            if(RHSToken.isIdentifier()) {

                // Get result register
                Register leftRegister = getRegisterOfResult( LHS.getLastTokenSubGroup().getReturnTypeList());

                Register rightRegister = Register.getRegisterNotInUse();
                acquire(rightRegister);

                if(registerNotFound(rightRegister)) return;

                // Get entry
                ABSymbolTableEntry RHSEntry = abSemantic.getEntryOf(RHSToken);

                // Offset register
                Register offsetRightRegister = getRegisterOffsetAndAcquire(RHS);
                acquire(offsetRightRegister);

                if(registerNotFound(offsetRightRegister))
                    return;

                // Load LHS
                addCode(generateLine(true, Instruction.LW.getName(), rightRegister.getName(), getDataAt(RHSEntry.getLabel(), offsetRightRegister)) + "% Load " + RHSEntry.getDetails());
                newLine();

                // Add them
                addCode(generateLine(true, nonImmediate.getName(), leftRegister.getName(), leftRegister.getName(), rightRegister.getName()) + "% ANS " + arithOp.getValue() + " " + RHSEntry.getName());
                newLine();

                // Release register
                release(rightRegister);
                release(offsetRightRegister);

                // Store result
                storeResultAtRegister(result, leftRegister);

                // If not an identifier - Result + #
            } else {

                // Get result register
                Register leftRegister = getRegisterOfResult( LHS.getLastTokenSubGroup().getReturnTypeList());

                // Add them
                addCode(generateLine(true, immediate.getName(), leftRegister.getName(), leftRegister.getName(), RHSToken.getValue()) + "% ANS " + arithOp.getValue() + " " + RHSToken.getValue());
                newLine();

                // Store result
                storeResultAtRegister(result, leftRegister);
            }

            // If RHS is a result - ... + Result
        } else if(RHSToken == null) {

            // If LHS is an identifier - ID + Result
            if(LHSToken.isIdentifier()) {

                // Get result register
                Register rightRegister = getRegisterOfResult( RHS.getLastTokenSubGroup().getReturnTypeList());

                Register leftRegister = Register.getRegisterNotInUse();
                acquire(leftRegister);

                if(registerNotFound(leftRegister)) return;

                // Get entry
                ABSymbolTableEntry LHSEntry = abSemantic.getEntryOf(LHSToken);

                // Offset register
                Register offsetLeftRegister = getRegisterOffsetAndAcquire(LHS);
                acquire(offsetLeftRegister);

                if(registerNotFound(offsetLeftRegister))
                    return;

                // Load LHS
                addCode(generateLine(true, Instruction.LW.getName(), leftRegister.getName(), getDataAt(LHSEntry.getLabel(), offsetLeftRegister)) + "% Load " + LHSEntry.getDetails());
                newLine();

                // Add them
                addCode(generateLine(true, nonImmediate.getName(), leftRegister.getName(), leftRegister.getName(), rightRegister.getName()) + "% " + LHSEntry.getName() + " " + arithOp.getValue() + " ANS");
                newLine();

                // Release register
                release(rightRegister);
                release(offsetLeftRegister);

                // Store result
                storeResultAtRegister(result, leftRegister);

                // If not an identifier - # + Result
            } else {

                // Get result register
                Register rightRegister = getRegisterOfResult( RHS.getLastTokenSubGroup().getReturnTypeList());

                Register leftRegister = Register.getRegisterNotInUse();
                acquire(leftRegister);

                if(registerNotFound(leftRegister)) return;

                // Add them
                addCode(generateLine(true, Instruction.ADDI.getName(), leftRegister.getName(), Register.R0.getName(), LHSToken.getValue()) + "% 0 + " + LHSToken.getValue());
                newLine();

                // Add them
                addCode(generateLine(true, nonImmediate.getName(), leftRegister.getName(), leftRegister.getName(), rightRegister.getName()) + "% ANS + ANS");
                newLine();

                // Release register
                release(rightRegister);

                // Store result
                storeResultAtRegister(result, leftRegister);
            }

            // If both are new  - ... + ...
        }  else {

            // If both are identifier - ID + ID
            if(LHSToken.isIdentifier() && RHSToken.isIdentifier()) {

                // Adjust entries
                ABSymbolTableEntry LHSEntry = abSemantic.getEntryOf(LHSToken);
                ABSymbolTableEntry RHSEntry = abSemantic.getEntryOf(RHSToken);

                Register leftRegister = Register.getRegisterNotInUse();
                acquire(leftRegister);
                Register rightRegister = Register.getRegisterNotInUse();
                acquire(rightRegister);

                // If can't reserve registers
                if(registerNotFound(leftRegister) || registerNotFound(rightRegister))
                    return;

                // Offset register
                Register offsetLeftRegister = getRegisterOffsetAndAcquire(LHS);
                acquire(offsetLeftRegister);

                if(registerNotFound(offsetLeftRegister))
                    return;

                // Load LHS
                addCode(generateLine(true, Instruction.LW.getName(), leftRegister.getName(), getDataAt(LHSEntry.getLabel(), offsetLeftRegister)) + "% Load " + LHSEntry.getDetails());
                newLine();

                release(offsetLeftRegister);

                // Offset register
                Register offsetRightRegister = getRegisterOffsetAndAcquire(RHS);
                acquire(offsetRightRegister);

                if(registerNotFound(offsetRightRegister))
                    return;

                // Load RHS
                addCode(generateLine(true, Instruction.LW.getName(), rightRegister.getName(), getDataAt(RHSEntry.getLabel(), offsetRightRegister)) + "% Load " + RHSEntry.getDetails());
                newLine();

                // Add them
                addCode(generateLine(true, nonImmediate.getName(), leftRegister.getName(),  leftRegister.getName(), rightRegister.getName()) + "% " + LHSEntry.getName() + " " + arithOp.getValue() + " " + RHSEntry.getName());
                newLine();

                // Release
                release(rightRegister);
                release(offsetRightRegister);

                // Store result
                storeResultAtRegister(result, leftRegister);

                // If LHS only is an identifier - ID + #
            } else if (LHSToken.isIdentifier()) {

                // Adjust entries
                ABSymbolTableEntry LHSEntry = abSemantic.getEntryOf(LHSToken);

                // Get register
                Register leftRegister = Register.getRegisterNotInUse();
                acquire(leftRegister);

                // If can't reserve registers
                if(registerNotFound(leftRegister))
                    return;

                // Offset register
                Register offsetLeftRegister = getRegisterOffsetAndAcquire(LHS);
                acquire(offsetLeftRegister);

                if(registerNotFound(offsetLeftRegister))
                    return;

                // Load LHS
                addCode(generateLine(true, Instruction.LW.getName(), leftRegister.getName(), getDataAt(LHSEntry.getLabel(), offsetLeftRegister)) + "% Load " + LHSEntry.getDetails());
                newLine();

                // Add them
                addCode(generateLine(true, immediate.getName(), leftRegister.getName(),  leftRegister.getName(), RHSToken.getValue()) + "% " + LHSEntry.getName() + " " + arithOp.getValue() + " " + RHSToken.getValue());
                newLine();

                release(offsetLeftRegister);

                // Store result
                storeResultAtRegister(result, leftRegister);

                // If RHS only is an identifier - # + ID
            } else if (RHSToken.isIdentifier()) {

                // Adjust entries
                ABSymbolTableEntry RHSEntry = abSemantic.getEntryOf(RHSToken);

                // Get register
                Register leftRegister = Register.getRegisterNotInUse();
                acquire(leftRegister);
                Register rightRegister = Register.getRegisterNotInUse();
                acquire(rightRegister);

                // If can't reserve registers
                if(registerNotFound(leftRegister) || registerNotFound(rightRegister))
                    return;

                // Load LHS
                addCode(generateLine(true, Instruction.ADDI.getName(), leftRegister.getName(), Register.R0.getName(), LHSToken.getValue()) + "% 0 + " + LHSToken.getValue());
                newLine();

                // Offset register
                Register offsetRightRegister = getRegisterOffsetAndAcquire(RHS);
                acquire(offsetRightRegister);

                if(registerNotFound(offsetRightRegister))
                    return;

                // Load RHS
                addCode(generateLine(true, Instruction.LW.getName(), rightRegister.getName(), getDataAt(RHSEntry.getLabel(), offsetRightRegister)) + "% Load " + RHSEntry.getDetails());
                newLine();

                // Add them
                addCode(generateLine(true, nonImmediate.getName(), leftRegister.getName(),  leftRegister.getName(), rightRegister.getName()) + "% ANS " + arithOp.getValue() + " ANS");
                newLine();

                // Release
                release(rightRegister);
                release(offsetRightRegister);

                // Store result
                storeResultAtRegister(result, leftRegister);

                // If both are non identifiers - # + #
            } else {

                // Get register
                Register leftRegister = Register.getRegisterNotInUse();
                acquire(leftRegister);

                // If can't reserve registers
                if(registerNotFound(leftRegister))
                    return;

                // Load LHS
                addCode(generateLine(true, Instruction.ADDI.getName(), leftRegister.getName(),  Register.R0.getName(), LHSToken.getValue()) + "% 0 + " + LHSToken.getValue());
                newLine();

                // Add them
                addCode(generateLine(true, immediate.getName(), leftRegister.getName(),  leftRegister.getName(), RHSToken.getValue()) + "% " + "ANS " + arithOp.getValue() + " " + RHSToken.getValue());
                newLine();

                // Store result
                storeResultAtRegister(result, leftRegister);
            }
        }
    }

    /*****************************************************
     *
     *                  CODE UTILS
     *
     *****************************************************/

    public Register getRegisterOffsetAndAcquire(ABSemantic.ABSemanticTokenGroup group) {

        // If not an array
        if(group.getLastTokenSubGroup().getArgumentsSize() == 0) {
            return Register.R0;

            // If array
        } else {

            // Load values
            List<List<ABToken>> indecies = abSemantic.getIndicesOf(group.getLastTokenSubGroup().getUsedToken());

            // If function
            if(indecies == null)
                return Register.R0;

            // Store
            Register offsetRegister = Register.getRegisterNotInUse();
            acquire(offsetRegister);

            // Reset
            addCode(generateLine(true, Instruction.ADD.getName(), offsetRegister.getName(), Register.R0.getName(), Register.R0.getName()));
            newLine();

            for(int i=0; i < indecies.size(); i++) {

                // Get index
                List<ABToken> indexTokens = indecies.get(i);

                // Check if it's a result
                if(getRegisterOfResult(indexTokens) != null) {
                    addCode(generateLine(true, Instruction.ADD.getName(), offsetRegister.getName(), offsetRegister.getName(), getRegisterOfResult(indexTokens).getName()));
                    newLine();

                // If variable
                } else if(abSemantic.getEntryOf(indexTokens.get(0)) != null) {

                    // TODO handle array[array[1]]

                    // Entry
                    ABSymbolTableEntry varEntry = abSemantic.getEntryOf(indexTokens.get(0));

                    // Get register
                    Register tmpRegister = Register.getRegisterNotInUse();
                    acquire(tmpRegister);

                    // If not found
                    if(registerNotFound(tmpRegister))
                        return tmpRegister;

                    addCode(generateLine(true, Instruction.LW.getName(), tmpRegister.getName(), getDataAt(varEntry.getLabel(), Register.R0)));
                    newLine();

                    addCode(generateLine(true, Instruction.ADD.getName(), offsetRegister.getName(), offsetRegister.getName(), tmpRegister.getName()));
                    newLine();

                    release(tmpRegister);

                // It's an integer
                } else {

                    // Value
                    addCode(generateLine(true, Instruction.ADDI.getName(), offsetRegister.getName(), offsetRegister.getName(), indexTokens.get(0).getValue()));
                    newLine();
                }
            }

            // Value
            addCode(generateLine(true, Instruction.MULI.getName(), offsetRegister.getName(), offsetRegister.getName(), ABArchitectureHelper.Size.INTEGER.getSizeInByte()+""));
            newLine();

            return offsetRegister;
        }
    }

    /**
     * Add code based on mode
     * @param code
     */
    public void addCode(String code) {
        switch (mode) {
            case ENTRY:
                entry += code;
                break;

            case FUNCTION:
                functions += code;
                break;

            case BUFFER:
                buffer += code;
                break;
        }
    }

    /**
     * Generate a unique label
     * @param label
     * @return
     */
    public String generateUniqueLabel(String label, boolean storeInStack) {
        label = label == null ? null : label + (++unique_id);
        if(storeInStack)
            labels.push(label);
        return label;
    }

    /**
     * Get last generated label
     * @return
     */
    public String getLastGeneratedLabel() {
        return labels.pop();
    }

    /**
     * Generate DATA(R0)
     * @param data
     * @param register
     * @return
     */
    public String getDataAt(String data, Register register) {
        return data + "(" + register.getName() + ")";
    }

    /**
     * Check if register is not null
     * @param register
     * @return
     */
    public boolean registerNotFound(Register register) {
        // If no more resources
        if(register == Register.R_NO_FOUND) {
            l.error("All registers are in use before performing");
            foundError(Reason.OUT_OF_REGISTERS);
            return true;
        }
        return false;
    }

    /**
     * Get the register that stores a result
     * @param result
     * @return
     */
    public Register getRegisterOfResult(List<ABToken> result) {
        return resultRegisterMap.get(result);
    }

    /**
     * Store result at register
     * @param result
     * @param register
     */
    public void storeResultAtRegister(List<ABToken> result, Register register) {
        resultRegisterMap.put(result, register);
    }

    /**
     * Get register of group
     * @param group
     * @return
     */
    public Register getRegisterOfGroup(ABSemantic.ABSemanticTokenGroup group) {
        return groupRegisterMap.get(group);
    }

    /**
     * Store group at register
     * @param group
     * @param register
     */
    public void storeGroupAtRegister(ABSemantic.ABSemanticTokenGroup group, Register register) {
        groupRegisterMap.put(group, register);
    }

    /**
     * Add a new line
     */
    private void newLine() {
        addCode("\n");
    }

    /**
     * Get buffer content and flush it
     * @return
     */
    public String getBufferAndFlush() {
        String tmp = buffer;
        buffer = "";
        return tmp;
    }

    /**
     * Generate a line
     * @param leftPad
     * @param args
     * @return
     */
    public String generateLine(boolean leftPad, String ... args) {
        switch (args.length) {
            case 1:
                if(leftPad)
                    return String.format("%-15s %-15s","", args[0]);
                return String.format("%-15s", args[0]);

            case 2:
                if(leftPad)
                    return String.format("%-15s %-15s %-15s","", args[0], args[1]);
                return String.format("%-15s %-15s",args[0], args[1]);

            case 3:
                if(leftPad)
                    return String.format("%-15s %-15s %-15s %-15s","", args[0], args[1]+",", args[2]);
                return String.format("%-15s %-15s %-15s",args[0], args[1], args[2]);

            case 4:
                if(leftPad)
                    return String.format("%-15s %-15s %-15s %-15s %-15s", "", args[0], args[1]+",", args[2]+",", args[3]);
                return String.format("%-15s %-15s %-15s %-15s",args[0], args[1], args[2]+",", args[3]);

            case 5:
                if(leftPad)
                    return String.format("%-15s %-15s %-15s %-15s %-15s %-15s", "", args[0], args[1]+",", args[2]+",", args[3]+",", args[4]);
                return String.format("%-15s %-15s %-15s %-15s %-15s", args[0], args[1], args[2]+",", args[3]+",", args[4]);

        }
        return null;
    }

    /**
     * Reset register
     * @param register
     * @return
     */
    public String resetRegister(Register register) {
        return generateLine(true, Instruction.SUB.getName(), register.getName(), register.getName());
    }

    /**
     * Reserve register
     * @param register
     * @return
     */
    public boolean acquire(Register register) {
        if(register == Register.R_NO_FOUND) return false;
        addCode(generateLine(true, "% Register " + register.getName() + " acquired"));
        newLine();
        register.setInUse(true);
        return true;
    }

    /**
     * Release register
     * @param register
     * @return
     */
    public boolean release(Register register) {
        if(register == Register.R_NO_FOUND) return false;
        if(register == null) return true;
        if(!register.canReset) return true;
        if(register.isInUse()) {
            addCode(generateLine(true, "% Register " + register.getName() + " released"));
            newLine();
        }
        register.setInUse(false);
        return true;
    }

    // Registers
    enum Register {
        R0("r0", true), // Zero
        R1("r1"),
        R2("r2"),
        R3("r3"),
        R4("r4"),
        R5("r5"),
        R6("r6"),
        R7("r7"),
        R8("r8"),
        R9("r9"),
        R10("r10"), // Param 1
        R11("r11"), // Param 2
        R13("r13", true), // Library
        R14("r14", true), // Stack pointer
        R15("r15", true), // Address
        R_NO_FOUND("not_found") // Marks that there are not more registers
        ;

        String name;
        boolean inUse = false;
        boolean canReset = true;
        Register(String name) {
            this.name = name;
            inUse = false;
        }

        Register(String name, boolean inUse) {
            this.name = name;
            this.inUse = inUse;
            if(inUse) canReset = false;
        }

        /**
         * Reset registers that can be reset
         */
        public static void reset() {
            for(Register register : values())
                if(register.canReset)
                    register.setInUse(false);
        }

        public static Register getParamNotInUse() {
            if(R10.isInUse() && R11.isInUse()) return R_NO_FOUND;
            if(R10.isInUse()) return R11;
            return R10;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isInUse() {
            return inUse;
        }

        public void setInUse(boolean inUse) {
            this.inUse = inUse;
        }

        public static Register getRegisterNotInUse() {
            for(Register r : values())
                if(!r.isInUse())
                    return r;
            return Register.R_NO_FOUND;
        }
    }

    enum Instruction {
        ENTRY("entry"),
        HLT("hlt"),
        ORG("org"),
        DW("dw"),
        DB("db"),
        RES("res"),
        ALIGN("align"),

        ADD("add"),
        SUB("sub"),
        MUL("mul"),
        DIV("div"),
        MOD("mod"),
        AND("and"),
        OR("or"),
        NOT("not"),
        CEQ("ceq"),
        CNE("cne"),
        CLT("clt"),
        CLE("cle"),
        CGT("cgt"),
        CGE("cge"),

        ADDI("addi"),
        SUBI("subi"),
        MULI("muli"),
        DIVI("divi"),
        MODI("modi"),
        ANDI("andi"),
        ORI("ori"),
        CEQI("ceqi"),
        CNEI("cnei"),
        CLTI("clti"),
        CLEI("clei"),
        CGTI("cgti"),
        CGEI("cgei"),
        SL("sl"),
        SR("sr"),

        LW("lw"),
        LB("lb"),
        SW("sw"),
        SB("sb"),

        GETC("getc"),
        PUTC("putc"),

        BZ("bz"),
        BNZ("bnz"),
        J("j"),
        JR("jr"),
        JL("jl"),
        JLR("jlr"),
        NOP("nop");
        String name;
        Instruction(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}

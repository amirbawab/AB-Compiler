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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final String TOP_ADDRESS = "topaddr";
    private final String STACK_SIZE = (4*300) + "";

    // Components
    private boolean[] free_registers;
    private String code = "";
    private String footer = "";
    private ABSemantic abSemantic;

    // Locks
    private boolean generateCode = false;
    private Reason reason;

    public enum Reason {
        END_OF_PROGRAM,
        SEMANTIC_ERROR,
        OUT_OF_REGISTERS
    };

    // Map
    public Map<List<ABToken>, Register> resultRegisterMap;

    // Unique id
    private int unique_id = 0;

    public ABTranslation(ABSemantic abSemantic) {

        // Init components
        this.abSemantic = abSemantic;
        free_registers = new boolean[15];
        resultRegisterMap = new HashMap<>();
    }

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

        // Comment
        footer += "% Table: " + table.getName() + "\n";

        for(ABSymbolTableEntry entry : table.getRows()) {

            // Data
            String instruction;
            int value = 0;

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
        }

        footer += "\n";
    }

    /**
     * Generate code for arithmetic operation
     * @param LHS
     * @param RHS
     * @param arithOp
     * @param result
     */
    public void generateArithmeticOperation(ABSemantic.ABSemanticTokenGroup LHS, ABSemantic.ABSemanticTokenGroup RHS, ABToken arithOp, List<ABToken> result) {

        // If code generation
        if(!generateCode) return;

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
     * Check if can generate code
     * @return
     */
    public boolean isGenerateCode() {
        return generateCode;
    }

    /**
     * Disable code generation
     * @param reason
     */
    public void stop(Reason reason) {
        this.generateCode = false;
        this.reason =reason;
    }

    /**
     * Allow code generation
     */
    public void start() {
        this.generateCode = true;
    }

    /**
     * Generate the final code
     * @return
     */
    public String generateCode() {
        if(generateCode || reason == Reason.END_OF_PROGRAM) {

            // Halt
            code += generateLine(true, Instruction.HLT.getName()) + "\n";

            // Stack
            footer += String.format("%-15s %-15s %-15s", STACK, Instruction.RES.getName(), STACK_SIZE) + "% Allocating memory for the stack\n";

            return getHeader() + code + footer;
        } else if(reason == Reason.SEMANTIC_ERROR) {
            return "% Couldn't generate code because of one or more semantic errors";

        } else if(reason == Reason.OUT_OF_REGISTERS) {
            return "% Couldn't generate code because the program requires more registers";

        } else {
            return "% Unexpected error";
        }
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

        // If LHS and RHS are result - Result + Result
        if(LHSToken == null && RHSToken == null) {
            Register leftRegister = getRegisterOfResult( LHS.getLastTokenSubGroup().getReturnTypeList());
            Register rightRegister = getRegisterOfResult( RHS.getLastTokenSubGroup().getReturnTypeList());

            // INST L L R
            code += generateLine(true, nonImmediate.getName(), leftRegister.getName(),  leftRegister.getName(), rightRegister.getName()) + "% ANS " + arithOp.getValue() + " ANS";
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

                // Load LHS
                code += generateLine(true, Instruction.LW.getName(), rightRegister.getName(), getDataAt(RHSEntry.getLabel(), Register.R0)) + "% Load " + RHSEntry.getDetails();
                newLine();

                // Add them
                code += generateLine(true, nonImmediate.getName(), leftRegister.getName(), leftRegister.getName(), rightRegister.getName()) + "% ANS " + arithOp.getValue() + " " + RHSEntry.getName();
                newLine();

                // Release register
                release(rightRegister);

                // Store result
                storeResultAtRegister(result, leftRegister);

                // If not an identifier - Result + #
            } else {

                // Get result register
                Register leftRegister = getRegisterOfResult( LHS.getLastTokenSubGroup().getReturnTypeList());

                // Add them
                code += generateLine(true, immediate.getName(), leftRegister.getName(), leftRegister.getName(), RHSToken.getValue()) + "% ANS " + arithOp.getValue() + " " + RHSToken.getValue();
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

                // Load LHS
                code += generateLine(true, Instruction.LW.getName(), leftRegister.getName(), getDataAt(LHSEntry.getLabel(), Register.R0)) + "% Load " + LHSEntry.getDetails();
                newLine();

                // Add them
                code += generateLine(true, nonImmediate.getName(), leftRegister.getName(), leftRegister.getName(), rightRegister.getName()) + "% " + LHSEntry.getName() + " " + arithOp.getValue() + " ANS";
                newLine();

                // Release register
                release(rightRegister);

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
                code += generateLine(true, Instruction.ADDI.getName(), leftRegister.getName(), Register.R0.getName(), LHSToken.getValue()) + "% 0 + " + LHSToken.getValue();
                newLine();

                // Add them
                code += generateLine(true, nonImmediate.getName(), leftRegister.getName(), leftRegister.getName(), rightRegister.getName()) + "% ANS + ANS";
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

                // Load LHS
                code += generateLine(true, Instruction.LW.getName(), leftRegister.getName(), getDataAt(LHSEntry.getLabel(), Register.R0)) + "% Load " + LHSEntry.getDetails();
                newLine();

                // Load RHS
                code += generateLine(true, Instruction.LW.getName(), rightRegister.getName(), getDataAt(RHSEntry.getLabel(), Register.R0)) + "% Load " + RHSEntry.getDetails();
                newLine();

                // Add them
                code += generateLine(true, nonImmediate.getName(), leftRegister.getName(),  leftRegister.getName(), rightRegister.getName()) + "% " + LHSEntry.getName() + " " + arithOp.getValue() + " " + RHSEntry.getName();
                newLine();

                // Release
                release(rightRegister);

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

                // Load LHS
                code += generateLine(true, Instruction.LW.getName(), leftRegister.getName(), getDataAt(LHSEntry.getLabel(), Register.R0)) + "% Load " + LHSEntry.getDetails();
                newLine();

                // Add them
                code += generateLine(true, immediate.getName(), leftRegister.getName(),  leftRegister.getName(), RHSToken.getValue()) + "% " + LHSEntry.getName() + " " + arithOp.getValue() + " " + RHSToken.getValue();
                newLine();

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
                code += generateLine(true, Instruction.ADDI.getName(), leftRegister.getName(), Register.R0.getName(), LHSToken.getValue()) + "% 0 + " + LHSToken.getValue();
                newLine();

                // Load RHS
                code += generateLine(true, Instruction.LW.getName(), rightRegister.getName(), getDataAt(RHSEntry.getLabel(), Register.R0)) + "% Load " + RHSEntry.getDetails();
                newLine();

                // Add them
                code += generateLine(true, nonImmediate.getName(), leftRegister.getName(),  leftRegister.getName(), rightRegister.getName()) + "% ANS " + arithOp.getValue() + " ANS";
                newLine();

                // Release
                release(rightRegister);

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
                code += generateLine(true, Instruction.ADDI.getName(), leftRegister.getName(),  Register.R0.getName(), LHSToken.getValue()) + "% 0 + " + LHSToken.getValue();
                newLine();

                // Add them
                code += generateLine(true, immediate.getName(), leftRegister.getName(),  leftRegister.getName(), RHSToken.getValue()) + "% " + "ANS " + arithOp.getValue() + " " + RHSToken.getValue();
                newLine();

                // Store result
                storeResultAtRegister(result, leftRegister);
            }
        }
    }

    /**
     * Generate assignment code
     * @param LHS
     * @param RHS
     */
    public void generateAssignment(ABSemantic.ABSemanticTokenGroup LHS, ABSemantic.ABSemanticTokenGroup RHS) {

        // If code generation
        if(!generateCode) return;

        // Registers
        Register leftRegister = null;

        // Entries
        ABSymbolTableEntry LHSEntry = null;
        ABSymbolTableEntry RHSEntry = null;

        // Cache info
        ABToken LHSToken = LHS.getLastTokenSubGroup().getUsedToken();
        ABToken RHSToken = RHS.getLastTokenSubGroup().getUsedToken();

        // TODO Check if LHS is in stack

        // If RHS is a result
        // FIXME Assuming that LHS has always an entry and is not in the stack
        if(RHSToken == null) {

            // Get result register
            leftRegister = getRegisterOfResult( RHS.getLastTokenSubGroup().getReturnTypeList());

            // Get entry
            LHSEntry = abSemantic.getEntryOf(LHSToken);

            // Add them
            code += generateLine(true, Instruction.SW.getName(), getDataAt(LHSEntry.getLabel(), Register.R0), leftRegister.getName()) + "% " + LHSEntry.getName() + " = ANS";
            newLine();

            // Release register
            release(leftRegister);

            // If RHS is not a result
            // FIXME Assuming that LHS has always an entry and is not in the stack
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

                // Load LHS
                code += generateLine(true, Instruction.LW.getName(), leftRegister.getName(), getDataAt(RHSEntry.getLabel(), Register.R0)) + "% Load " + RHSEntry.getDetails();
                newLine();

                // Add them
                code += generateLine(true, Instruction.SW.getName(), getDataAt(LHSEntry.getLabel(), Register.R0),  leftRegister.getName()) + "% " + LHSEntry.getName() + " = " + RHSEntry.getName();
                newLine();

                // Release
                release(leftRegister);

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
                code += generateLine(true, Instruction.ADDI.getName(), leftRegister.getName(),  Register.R0.getName(), RHSToken.getValue()) + "% " +  "0 + " + RHSToken.getValue();
                newLine();

                // Add them
                code += generateLine(true, Instruction.SW.getName(), getDataAt(LHSEntry.getLabel(), Register.R0),  leftRegister.getName()) + "% " + LHSEntry.getName() + " = ANS";
                newLine();

                // Release
                release(leftRegister);
            }
        }

    }

    /*****************************************************
     *
     *                  CODE UTILS
     *
     *****************************************************/

    /**
     * Generate a unique label
     * @param label
     * @return
     */
    public String generateUniqueLabel(String label) {
        return label == null ? null : label + (++unique_id);
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
            stop(Reason.OUT_OF_REGISTERS);
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
     * Add a new line
     */
    private void newLine() {
        code += "\n";
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
        code += generateLine(true, "% Register " + register.getName() + " acquired");
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
        code += generateLine(true, "% Register " + register.getName() + " released");
        newLine();
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
        R10("r10"),
        R11("r11"),
        R12("r12"),
        R13("r13"),
        R14("r14", true), // Stack pointer
        R15("r15", true), // Address
        R_NO_FOUND("not_found") // Marks that there are not more registers
        ;

        String name;
        boolean inUse = false;
        Register(String name) {
            this.name = name;
            inUse = false;
        }

        Register(String name, boolean inUse) {
            this.name = name;
            this.inUse = inUse;

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
            return null;
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

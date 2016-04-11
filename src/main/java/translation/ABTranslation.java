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

        return header;
    }

    /**
     * Append information on the footer
     * @param table
     */
    public void appendFooter(ABSymbolTable table) {
        for(ABSymbolTableEntry entry : table.getRows()) {

            // Data
            String instruction = null;
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

        // Data
        String instruction = null;

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

    public void performArith(ABSemantic.ABSemanticTokenGroup LHS, ABSemantic.ABSemanticTokenGroup RHS, Instruction nonImmediate, Instruction immediate, List<ABToken> result, ABToken arithOp) {

        // Registers
        Register leftRegister = null;
        Register rightRegister = null;

        // Entries
        ABSymbolTableEntry LHSEntry = null;
        ABSymbolTableEntry RHSEntry = null;

        // Cache info
        ABToken LHSToken = LHS.getLastTokenSubGroup().getUsedToken();
        ABToken RHSToken = RHS.getLastTokenSubGroup().getUsedToken();

        // If LHS and RHS are result
        if(LHSToken == null && RHSToken == null) {
            leftRegister = getRegisterOfResult( LHS.getLastTokenSubGroup().getReturnTypeList());
            rightRegister = getRegisterOfResult( RHS.getLastTokenSubGroup().getReturnTypeList());

            // Add them
            code += generateLine(true, nonImmediate.getName(), leftRegister.getName(),  leftRegister.getName(), rightRegister.getName()) + "% ANS " + arithOp.getValue() + " ANS";
            newLine();

            // Release register
            release(rightRegister);

            // Store result
            storeResultAtRegister(result, leftRegister);

            // If LHS is a result
        } else if(LHSToken == null) {

            // Get result register
            leftRegister = getRegisterOfResult( LHS.getLastTokenSubGroup().getReturnTypeList());

            // If RHS is an identifier
            if(RHSToken.isIdentifier()) {

                rightRegister = Register.getRegisterNotInUse();
                acquire(rightRegister);

                if(registerNotFound(rightRegister)) return;

                // Get entry
                RHSEntry = abSemantic.getEntryOf(RHSToken);

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

                // If not an identifier
            } else {

                // Add them
                code += generateLine(true, immediate.getName(), leftRegister.getName(), leftRegister.getName(), RHSToken.getValue()) + "% ANS " + arithOp.getValue() + " " + RHSToken.getValue();
                newLine();

                // Store result
                storeResultAtRegister(result, leftRegister);
            }

            // If RHS is a result
        } else if(RHSToken == null) {

            // Get result register
            leftRegister = getRegisterOfResult( RHS.getLastTokenSubGroup().getReturnTypeList());

            // If LHS is an identifier
            if(LHSToken.isIdentifier()) {

                rightRegister = Register.getRegisterNotInUse();
                acquire(rightRegister);

                if(registerNotFound(rightRegister)) return;

                // Get entry
                LHSEntry = abSemantic.getEntryOf(LHSToken);

                // Load RHS
                code += generateLine(true, Instruction.LW.getName(), rightRegister.getName(), getDataAt(LHSEntry.getLabel(), Register.R0)) + "% Load " + LHSEntry.getDetails();
                newLine();

                // Add them
                code += generateLine(true, nonImmediate.getName(), leftRegister.getName(), leftRegister.getName(), rightRegister.getName()) + "% ANS " + arithOp.getValue() + " " + LHSEntry.getName();
                newLine();

                // Release register
                release(rightRegister);

                // Store result
                storeResultAtRegister(result, leftRegister);

                // If not an identifier
            } else {

                // Add them
                code += generateLine(true, immediate.getName(), leftRegister.getName(), leftRegister.getName(), LHSToken.getValue()) + "% ANS " + arithOp.getValue() + " " + LHSToken.getValue();
                newLine();

                // Store result
                storeResultAtRegister(result, leftRegister);
            }

            // If both are new
        }  else {

            // If both are identifier
            if(LHSToken.isIdentifier() && RHSToken.isIdentifier()) {

                // Adjust entries
                LHSEntry = abSemantic.getEntryOf(LHSToken);
                RHSEntry = abSemantic.getEntryOf(RHSToken);

                leftRegister = Register.getRegisterNotInUse();
                acquire(leftRegister);
                rightRegister = Register.getRegisterNotInUse();
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

                // If LHS only is an identifier
            } else if (LHSToken.isIdentifier()) {

                // Adjust entries
                LHSEntry = abSemantic.getEntryOf(LHSToken);

                // Get register
                leftRegister = Register.getRegisterNotInUse();
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

                // If RHS only is an identifier
            } else if (RHSToken.isIdentifier()) {

                // Adjust entries
                RHSEntry = abSemantic.getEntryOf(RHSToken);

                // Get register
                leftRegister = Register.getRegisterNotInUse();
                acquire(leftRegister);

                // If can't reserve registers
                if(registerNotFound(leftRegister))
                    return;

                // Load LHS
                code += generateLine(true, Instruction.LW.getName(), leftRegister.getName(), getDataAt(RHSEntry.getLabel(), Register.R0)) + "% Load " + RHSEntry.getDetails();
                newLine();

                // Add them
                code += generateLine(true, immediate.getName(), leftRegister.getName(),  LHSToken.getValue(), leftRegister.getName()) + "% " + LHSToken.getValue() + " " + arithOp.getValue() + " " + RHSEntry.getName();
                newLine();

                // Store result
                storeResultAtRegister(result, leftRegister);

                // If both are non identifiers
            } else {

                // Get register
                leftRegister = Register.getRegisterNotInUse();
                acquire(leftRegister);

                // If can't reserve registers
                if(registerNotFound(leftRegister))
                    return;

                // Load LHS
                code += generateLine(true, immediate.getName(), leftRegister.getName(),  Register.R0.getName(), LHSToken.getValue()) + "% " +  "0 " + arithOp.getValue() + " " + LHSToken.getValue();
                newLine();

                // Add them
                code += generateLine(true, immediate.getName(), leftRegister.getName(),  leftRegister.getName(), RHSToken.getValue()) + "% " + "ANS " + arithOp.getValue() + " " + RHSToken.getValue();
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
                return String.format("%-15s %-15s %-15s",args[0], args[1]+",", args[2]);

            case 4:
                if(leftPad)
                    return String.format("%-15s %-15s %-15s %-15s %-15s", "", args[0], args[1]+",", args[2]+",", args[3]);
                return String.format("%-15s %-15s %-15s %-15s",args[0], args[1]+",", args[2]+",", args[3]);

            case 5:
                if(leftPad)
                    return String.format("%-15s %-15s %-15s %-15s %-15s %-15s", "", args[0], args[1]+",", args[2]+",", args[3]+",", args[4]);
                return String.format("%-15s %-15s %-15s %-15s %-15s", args[0], args[1]+",", args[2]+",", args[3]+",", args[4]);

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
        R14("r14"),
        R15("r15", true), // Address
        R_NO_FOUND(null) // Marks that there are not more registers
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

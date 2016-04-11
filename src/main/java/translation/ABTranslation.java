package translation;

import scanner.ABToken;
import scanner.helper.ABTokenHelper;
import semantic.ABSemantic;
import semantic.ABSymbolTable;
import semantic.ABSymbolTableEntry;
import translation.helper.ABArchitectureHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Amir on 4/10/2016.
 */
public class ABTranslation {

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

    public ABTranslation(ABSemantic abSemantic) {

        // Init components
        this.abSemantic = abSemantic;
        free_registers = new boolean[15];
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

    public void generateArithmeticOperation(ABSemantic.ABSemanticTokenGroup LHS, ABSemantic.ABSemanticTokenGroup RHS, ABToken arithOp) {

        // Data
        String instruction = null;

        // Cache info
        ABToken LHSToken = LHS.getLastTokenSubGroup().getUsedToken();
        ABToken RHSToken = RHS.getLastTokenSubGroup().getUsedToken();

        // Check if identfier
        boolean isLHSIdentifier = LHSToken.getToken().equals(ABTokenHelper.T_IDENTIFIER);
        boolean isRHSIdentifier = RHSToken.getToken().equals(ABTokenHelper.T_IDENTIFIER);

        // Entries
        ABSymbolTableEntry LHSEntry = null;
        ABSymbolTableEntry RHSEntry = null;

        // Adjust entries
        if(isLHSIdentifier) LHSEntry = abSemantic.getEntryOf(LHSToken);
        if(isRHSIdentifier) RHSEntry = abSemantic.getEntryOf(RHSToken);

        // Get registers
        Register leftRegister = Register.getRegisterNotInUse();
        leftRegister.setInUse(true);

        // Get registers
        Register middleRegister = Register.getRegisterNotInUse();
        middleRegister.setInUse(true);

        // Get registers
        Register rightRegister = Register.getRegisterNotInUse();
        middleRegister.setInUse(true);

        switch (arithOp.getToken()) {
            case ABTokenHelper.T_PLUS:
                if(isLHSIdentifier && isRHSIdentifier) {

                    // Load LHS
                    code += generateLine(true, Instruction.LW.getName(), leftRegister.getName(), LHSEntry.getLabel()+"(" + Register.R0.getName() + ")") + "% Load " + LHSEntry.getDetails();
                    newLine();

                    // Load RHS
                    code += generateLine(true, Instruction.LW.getName(), middleRegister.getName(), RHSEntry.getLabel()+"(" + Register.R0.getName() + ")") + "% Load " + RHSEntry.getDetails();
                    newLine();

                    // Add them
                    code += generateLine(true, Instruction.ADD.getName(), rightRegister.getName(),  middleRegister.getName(), leftRegister.getName()) + "% " + LHSEntry.getName() + " + " + RHSEntry.getName();
                    newLine();
                }
                break;
        }

        // Free resources
        leftRegister.setInUse(false);
        middleRegister.setInUse(false);
        rightRegister.setInUse(false);

    }

    /**
     * Check if can generate code
     * @return
     */
    public boolean isGenerateCode() {
        return generateCode;
    }

    /**
     * Allow/Disable code generation
     * @param generateCode
     */
    public void setGenerateCode(boolean generateCode) {
        this.generateCode = generateCode;
    }

    /**
     * Generate the final code
     * @return
     */
    public String generateCode() {
        if(generateCode) {

            // Halt
            code += generateLine(true, Instruction.HLT.getName()) + "\n";

            return getHeader() + code + footer;
        } else {
            return "% Couldn't generate code because of one or more errors";
        }
    }

    /*****************************************************
     *
     *                  CODE UTILS
     *
     *****************************************************/

    private void newLine() {
        code += "\n";
    }

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

    // Registers
    enum Register {
        R0("r0", true),
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
        R115("r15", true);
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

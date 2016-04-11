package translation;

import scanner.ABToken;
import semantic.ABSymbolTable;

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

    // Locks
    private boolean generateCode = false;

    public ABTranslation() {

        // Init components
        free_registers = new boolean[15];
    }

    public String getHeader() {

        // Prepare header
        String header = "";

        // Add data to header
        header += "% The following code is generated automatically by ABCompiler\n";
        header += "%\tTime generated: " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()) + "\n\n";
        header += "entry\n";
        header += "align\n";

        return header;
    }

    public void appendFooter(ABSymbolTable table) {
        // TODO implement
    }

    public boolean isGenerateCode() {
        return generateCode;
    }

    public void setGenerateCode(boolean generateCode) {
        this.generateCode = generateCode;
    }

    /**
     * Generate the final code
     * @return
     */
    public String generateCode() {
        if(generateCode) {
            return getHeader() + code + footer;
        } else {
            return "% Couldn't generate code because of one or more errors";
        }
    }
}

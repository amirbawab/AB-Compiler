package translation;

import scanner.ABToken;

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
    private boolean generate = true;

    public ABTranslation() {

        // Init components
        free_registers = new boolean[15];
    }

    public String getHeader() {

        // Prepare header
        String header = "";

        // Add data to header
        header += "entry\n";
        header += "align\n";

        return header;
    }

    public String generateCode() {
        return getHeader() + code + footer;
    }
}

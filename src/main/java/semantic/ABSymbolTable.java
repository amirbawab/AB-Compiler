package semantic;

import scanner.ABToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amir on 3/25/2016.
 */
public class ABSymbolTable {
    private List<ABSymbolTableEntry> rows;
    public enum Kind {
        FUNCTION("function"),
        CLASS("class"),
        VARIABLE("variable"),
        PARAMETER("parameter");

        private String name;

        Kind(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public ABSymbolTable() {
        rows = new ArrayList<>();
    }

    /**
     * Add a new entry
     * @param name
     * @param kind
     * @return
     */
    public ABSymbolTableEntry addRow(String name, Kind kind) {
        ABSymbolTableEntry row = new ABSymbolTableEntry(name, kind);
        rows.add(row);
        return row;
    }

    class ABSymbolTableEntry {
        private String name;
        private Kind kind;
        private List<ABToken> type;
        private List<List<ABToken>> paramtersTypes;
        private ABSymbolTableEntry link;

        /**
         * Constructor
         * @param name
         * @param kind
         */
        public ABSymbolTableEntry(String name, Kind kind) {
            this.name = name;
            this.kind = kind;
            this.type = new ArrayList<>();
            this.paramtersTypes = new ArrayList<>();
        }

        /**
         * Add function parameter
         * @param tokens
         */
        public void addParameter(List<ABToken> tokens) {
            this.paramtersTypes.add(tokens);
        }

        /**
         * Set function type
         * @param tokens
         */
        public void setType(List<ABToken> tokens) {
            this.type = tokens;
        }
    }
}

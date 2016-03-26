package semantic;

import scanner.ABToken;

import java.util.ArrayList;
import java.util.List;

class ABSymbolTableEntry {
    private String name;
    private Kind kind;
    private List<ABToken> type;
    private List<List<ABToken>> paramtersTypes;
    private ABSymbolTable link;

    /**
     * Enum Kind
     */
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    public List<ABToken> getType() {
        return type;
    }

    public List<List<ABToken>> getParamtersTypes() {
        return paramtersTypes;
    }

    public void setParamtersTypes(List<List<ABToken>> paramtersTypes) {
        this.paramtersTypes = paramtersTypes;
    }

    public ABSymbolTable getLink() {
        return link;
    }

    public void setLink(ABSymbolTable link) {
        this.link = link;
    }
}
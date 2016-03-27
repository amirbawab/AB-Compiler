package semantic;

import parser.grammar.ABGrammarToken;
import scanner.ABToken;

import java.util.ArrayList;
import java.util.List;

class ABSymbolTableEntry {
    private String name;
    private Kind kind;
    private List<ABToken> type;
    private ABSymbolTable link;

    /**
     * Enum Kind
     */
    public enum Kind {
        FUNCTION("function"),
        CLASS("class"),
        VARIABLE("variable"),
        PARAMETER("parameter"),
        PROGRAM("program");

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

    public ABSymbolTable getLink() {
        return link;
    }

    public void setLink(ABSymbolTable link) {
        this.link = link;
    }

    public String getTypeAsString() {

        switch (kind) {
            case CLASS:
            case PROGRAM:
                return "Not applicable";
        }

        String typeStr = "";
        List<ABToken> type = getType();
        for(ABToken token : type)
            typeStr += token.getValue();
        return typeStr;
    }

    public String getParametersAsString() {

        switch (kind) {
            case CLASS:
            case PARAMETER:
            case PROGRAM:
            case VARIABLE:
                return "Not applicable";
        }

        String paramStr = "";
        List<List<ABToken>> tokensList = getParameters();
        for(int i=0; i<tokensList.size(); i++) {

            if(i > 0) paramStr += ", ";

            for(int j=0; j<tokensList.get(i).size(); j++) {
                paramStr += tokensList.get(i).get(j).getValue();
            }
        }
        return paramStr;
    }

    public List<List<ABToken>> getParameters() {
        List<List<ABToken>> parameters = null;
        if(link != null) {
            parameters = new ArrayList<>();
            for(ABSymbolTableEntry entry : link.getRows()) {
                if(entry.getKind() == Kind.PARAMETER)
                    parameters.add(entry.getType());
            }
        }
        return parameters;
    }

    public String toString() {
        return String.format("%s || %s || %s:%s", name, kind.getName(), getTypeAsString(), getParametersAsString());
    }
}
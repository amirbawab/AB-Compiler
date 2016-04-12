package semantic;

import scanner.ABToken;
import scanner.helper.ABTokenHelper;

import java.util.ArrayList;
import java.util.List;

public class ABSymbolTableEntry {
    private ABSymbolTable table;
    private String name;
    private String label;
    private Kind kind;
    private List<ABToken> type;
    private ABSymbolTable link;
    private ABToken token;
    private boolean properlyDefined = true;
    private int offset;
    private int sizeInBytes = Integer.MIN_VALUE;

    /**
     * Enum Kind
     */
    public enum Kind {
        FUNCTION("function"),
        CLASS("class"),
        VARIABLE("variable"),
        PARAMETER("parameter"),
        PROGRAM("program"),
        GLOBAL("global"),
        FOR("for"),
        ANY(null);

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
    public ABSymbolTableEntry(ABSymbolTable table, String name, Kind kind) {
        this.table = table;
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

    public ABToken getToken() {
        return token;
    }

    public void setToken(ABToken token) {
        this.token = token;
    }

    public int getAddress() { return token.hashCode(); }

    public int getArrayDimension() { return type.size()-1; }

    public int getTotalNumberOfElements() {
        int numberOfElements = 1;
        for(int i=1; i < type.size(); i++)
            numberOfElements *= Integer.parseInt(type.get(i).getValue());
        return numberOfElements;
    }

    public boolean isArray() { return type.size() > 1; }

    public boolean isProperlyDefined() {
        return properlyDefined;
    }

    public void setProperlyDefined(boolean properlyDefined) {
        this.properlyDefined = properlyDefined;
    }

    public ABSymbolTable getTable() {
        return table;
    }

    public void setTable(ABSymbolTable table) {
        this.table = table;
    }

    public boolean isPrimitiveType() {
        return !type.get(0).getToken().equals(ABTokenHelper.T_IDENTIFIER);
    }

    public String getStructure() {
        switch (kind) {
            case CLASS:
            case PROGRAM:
                return "Not applicable";
        }

        if( !isArray()) {
            return type.get(0).getToken().equals(ABTokenHelper.T_IDENTIFIER) ? "Class" : "Simple";

        } else {
            return "Array of type '" + type.get(0).getValue() + "' of dimension " + getArrayDimension();
        }
    }

    public String getTypeAsString() {

        switch (kind) {
            case CLASS:
            case PROGRAM:
                return "Not applicable";
        }

        String typeStr = type.get(0).getValue();
        for(int i=1; i<type.size(); i++)
            typeStr += "[" + type.get(i).getValue() + "]";
        return typeStr;
    }

    public String getKindAsString() {
        if(kind == Kind.FUNCTION) {
            return kind.getName() + " with " + getNumberOfParameters() + " parameter(s)";
        } else {
            return kind.getName();
        }
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

    public int getNumberOfParameters() {
        return getParameters().size();
    }

    public String toString() {
        return String.format("%s || %s || %s:%s", name, kind.getName(), getTypeAsString(), getParametersAsString());
    }

    @Override
    public boolean equals(Object obj) {
        ABSymbolTableEntry entry = (ABSymbolTableEntry) obj;

        // Function name
        if(!entry.getName().equals(getName())) return false;

        // Number of arguments
        List<List<ABToken>> parameters = getParameters();
        List<List<ABToken>> entryParameters = entry.getParameters();
        if(entryParameters.size() != parameters.size()) return false;

        // Arguments order and type
        for(int i=0; i < parameters.size(); i++) {

            // Number of tokens
            if(parameters.get(i).size() != entryParameters.get(i).size()) return false;

            // Compare tokens
            for(int j=0; j < parameters.get(i).size(); j++)
                if(!parameters.get(i).get(j).getValue().equals(entryParameters.get(i).get(j).getValue())) return false;
        }

        return true;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getSizeInBytes() {
        if(getLink() != null) return getLink().getSizeInBytes();
        return sizeInBytes;
    }

    public void setSizeInBytes(int sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }

    public String getLabel() {
        return label;
    }

    public String getReturnLabel() {
        return getLabel() + "ret";
    }

    public String getDetails() {
        return getTable().getName() + " > " + getName() + " : " + getTypeAsString();
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
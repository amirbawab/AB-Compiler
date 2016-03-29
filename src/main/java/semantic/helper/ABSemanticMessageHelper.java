package semantic.helper;

/**
 * Created by Amir on 3/27/2016.
 */
public class ABSemanticMessageHelper {

    // Messages
    public final static String MUTLIPLE_DECLARATION = "Multiple declaration found for identifier '%s' at line %d column %d and at line %d column %d";
    public final static String UNDEFINED_VARIABLE = "Undefined variable '%s' at line %d column %d";
    public final static String UNDEFINED_TYPE = "Undefined identifier type '%s' at line %d column %d";
    public final static String UNDEFINED_FUNCTION = "Call to undefined function '%s' at line %d column %d";
    public final static String RECURSIVE_DECLARATION = "Recursive declaration found '%s' at line %d column %d";
    public final static String UNDEFINED_MEMBER_OF_PRIMITIVE_OR_UNDEFINED_VAR = "Undefined data member '%s' of a primitive type or undefined variable at line %d column %d";
    public final static String UNDEFINED_MEMBER_OF_CLASS = "Undefined data member '%s' of the class '%s' at line %d column %d";
}

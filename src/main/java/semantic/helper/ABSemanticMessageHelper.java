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
    public final static String UNDEFINED_MEMBER_OF_PRIMITIVE_VARIABLE = "Undefined data member '%s' of a primitive type at line %d column %d";
    public final static String UNDEFINED_MEMBER_OF_UNDEFINED_VARIABLE = "Undefined data member '%s' of an undefined variable at line %d column %d";
    public final static String UNDEFINED_MEMBER_OF_CLASS = "Undefined data member '%s' of the class '%s' at line %d column %d";
    public final static String SAME_SIGNATURE_FUNCTION = "Cannot overload function '%s' with same signature at line %d column %d and at line %d column %d";
    public final static String VARIABLE_NOT_ARRAY = "Variable '%s' is not an array at line %d column %d";
    public final static String VARIABLE_IS_ARRAY = "Variable '%s' is an array at line %d column %d";
    public final static String ARRAYS_UNMATCH_DIMENSION = "Variable '%s' must be an array of %d instead of %d dimension at line %d column %d";
    public final static String ARRAY_LARGER_DIMENSION = "Array '%s' does not have dimension %d at line %d column %d";
    public final static String ARRAY_INDEX_NON_INTEGER = "Array '%s' does not have an integer at dimension %d at line %d column %d";
    public final static String ARITHMETIC_TYPE = "Cannot perform '%s' on the types %s and %s at line %d column %d";
    public final static String ARITHMETIC_ONE_UNDEFINED = "Cannot perform '%s' on the %s and an undefined type at line %d column %d";
    public final static String ARITHMETIC_TWO_UNDEFINED = "Cannot perform '%s' on two undefined types at line %d column %d";
    public final static String ASSIGNMENT_TWO_UNDEFINED = "Invalid equation because of two undefined types at line %d column %d";
    public final static String ASSIGNMENT_LHS_UNDEFINED = "Cannot assign type '%s' to an undefined variable '%s' at line %d column %d";
    public final static String ASSIGNMENT_RHS_UNDEFINED = "Cannot assign an undefined type to a variable of type '%s' at line %d column %d";
    public final static String ASSIGNMENT_INCOMPATIBLE = "Cannot assign type '%s' to type '%s' at line %d column %d";
    public final static String FUNCTION_WRONG_RETURN = "Function '%s' must have a return statement that is compatible with the function declaration type at line %d column %d";
}

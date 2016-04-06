package parser.grammar;

import scanner.ABToken;

import java.util.ArrayList;
import java.util.List;

public class ABGrammarToken {
	
	// Enum types
	private enum Type {
		TERMINAL,
		NON_TERMINAL,
		END_OF_STACK,
		ACTION,
		EPSILON
	}
	
	// Epsilon
	public static final String EPSILON = "EPSILON";
	public static final String END_OF_STACK = "$";
	
	// Variables
	private String originalValue;
	private String value;
	private Type type;
	private ABGrammarToken parent;
	private List<ABGrammarToken> children;
	private ABToken terminalValue;
	
	/**
	 * Create grammar token
	 * @param value
	 */
	public ABGrammarToken(String value) {

		// Store original value
		this.originalValue = value;

		// Decide the type
		if(value.equals(EPSILON)){
			this.value = value;
			this.type = Type.EPSILON;
		
		} else if(value.equals(END_OF_STACK)) {
			this.value = value;
			this.type = Type.END_OF_STACK;
			
		} else if(value.charAt(0) == '\'' && value.charAt(value.length()-1) == '\'') {
			this.value = value.substring(1, value.length()-1);
			this.type = Type.TERMINAL;
		
		} else if(value.charAt(0) == '#' && value.charAt(value.length()-1) == '#') {
			this.value = value.substring(1, value.length()-1);
			this.type = Type.ACTION;

		} else {
			this.value = value;
			this.type = Type.NON_TERMINAL;
			this.children = new ArrayList<>();
		}
	}

	/**
	 * Check if is terminal
	 * @return true if terminal
	 */
	public boolean isTerminal() {
		return type == Type.TERMINAL;
	}
	
	/**
	 * Check if is non terminal
	 * @return true if non terminal
	 */
	public boolean isNonTerminal() {
		return type == Type.NON_TERMINAL;
	}
	
	/**
	 * Check if is epsilon
	 * @return true if epsilon
	 */
	public boolean isEpsilon() {
		return type == Type.EPSILON;
	}
	
	/**
	 * Check if is end of stack symbol
	 * @return true if end of stack symbol
	 */
	public boolean isEndOfStack() {
		return type == Type.END_OF_STACK;
	}

	/**
	 * Check if is action
	 * @return true if action token
     */
	public boolean isAction() { return type == Type.ACTION; }

	/**
	 * Get value
	 * @return value
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 * Get detailed value
	 * @return
     */
	public String getDetailedValue() {
		if(isTerminal())
			return value + "\n" + terminalValue.getValue();
		return getValue();
	}

	/**
	 * Get terminal value
	 * @return
     */
	public ABToken getTerminalValue() {
		return terminalValue;
	}

	/**
	 * Set terminal value
	 * @param terminalValue
     */
	public void setTerminalValue(ABToken terminalValue) {
		this.terminalValue = terminalValue;
	}

	/**
	 * To String
	 * @return String to String
	 */
	public String toString() {
		switch (type) {
		case TERMINAL:
			return "'" + value + "'";

		case ACTION:
			return "#" + value + "#";

		case EPSILON:
		case END_OF_STACK:
		case NON_TERMINAL:
		default:
			return value;
		}
	}

	/**
	 * Add child
	 * @param token
     */
	public void addChild(ABGrammarToken token) {
		token.parent = this;
		children.add(token);
	}

	/**
	 * Get children
	 * @return
     */
	public List<ABGrammarToken> getChildren() {
		return children;
	}

	/**
	 * Create a copy of the current token
	 * @return a copy
	 */
	public ABGrammarToken makeCopy() {
		return new ABGrammarToken(originalValue);
	}
}

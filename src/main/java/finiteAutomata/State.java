package finiteAutomata;
import graph.Edge;
import graph.Vertex;
import graph.doublyLinkedList.NodeIterator;

/**
* Finite Automata
* Coded by Amir El Bawab
* Date: 6 January 2015
* License: MIT License ~ Please read License.txt for more information about the usage of this software
* */
public class State{

	// Status of the state
	public final static int NORMAL = 0;
	public final static int INITIAL = 1;
	public final static int FINAL = 2;
	public final static int INITIAL_FINAL = 3;
	private int status = NORMAL; // By default
	
	// Vertex that stores this state
	private Vertex<State, Transition> vertex;
	
	// State name
	private String name;
	
	// Token
	private String token;
	private boolean backtrack = false;
	
	/**
	 * Constructor
	 * @param status
	 * */
	protected State(int status) {
		if(status < NORMAL || status > INITIAL_FINAL)
			throw new FiniteAutomataException("Please enter a valid state status");
		this.status = status;
	}
	
	/**
	 * Get status
	 * @return status
	 * */
	public int getStatus() {
		return status;
	}
	
	/**
	 * Checks if a state is final
	 * @return boolean
	 * */
	public boolean isFinal(){
		return status == INITIAL_FINAL || status == FINAL;
	}

	/**
	 * Checks if a state is initial
	 * @return boolean
	 * */
	public boolean isInitial(){
		return status == INITIAL_FINAL || status == INITIAL;
	}
	
	/**
	 * Checks if a state is normal
	 * @return boolean
	 * */
	public boolean isNormal(){
		return status == NORMAL;
	}
	
	/**
	 * Get the name of a state
	 * @return name
	 * */
	public String getName() {
		return name;
	}

	/**
	 * Set name of the state. Ex: q0, p0 etc..
	 * @param name
	 */
	protected void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Get the vertex that stores this state
	 * @return vertex
	 * */
	protected Vertex<State, Transition> getVertex() {
		return vertex;
	}
	
	/**
	 * Set the vertex that stores this state
	 * @param vertex
	 * */
	protected void setVertex(Vertex<State, Transition> vertex) {
		this.vertex = vertex;
	}
	
	/**
	 * Set the status of the state
	 * @param status
	 * */
	protected void setStatus(int status){
		this.status = status;
	}
	
	/**
	 * Set token name
	 * @param token
	 */
	public void setToken(String token) {
		this.token = token;
	}
	
	/**
	 * Get token name
	 * @return token name
	 */
	public String getToken() {
		return token;
	}
	
	public void setBacktrack(boolean backtrack) {
		this.backtrack = backtrack;
	}
	
	public boolean getBacktrack() {
		return this.backtrack;
	}
	
	/**
	 * Get vertex id
	 */
	public int getVID() {
		return vertex.getID();
	}
	
	/**
	 * Get transition from this state
	 * @return all transition from this state
	 */
	public Transition[] getTransition() {
		Transition[] transitions = new Transition[getVertex().getOutEdges().size()];
		NodeIterator<Edge<State,Transition>> iterE = getVertex().getOutEdges();
		int index = 0;
		while(iterE.hasNext())
			transitions[index++] = iterE.next().getLabel();
		return transitions;
	}
	
	/**
	 * Get on read state
	 * @param c
	 * @return state or null if not found
	 */
	public State getOnRead(char c) {
		Transition[] transition = getTransition();
		for(int i=0; i < transition.length; i++) {
			if(c == transition[i].getRead())
				return transition[i].getToState();
		}
		return null;
	}
	
	/**
	 * To string
	 * */
	public String toString(){
		String output = String.format("(%s)", name);
		
		if (status == INITIAL)
			output = String.format("->%s", output);
		else if(status == FINAL)
			output = String.format("(%s)", output);
		else if(status == INITIAL_FINAL)
			output = String.format("->(%s)", output);
		return output;
	}
}

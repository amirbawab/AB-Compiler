package finiteAutomata;
import graph.Edge;
import graph.Graph;
import graph.Vertex;
import graph.doublyLinkedList.DoublyLinkedList;
import graph.doublyLinkedList.NodeIterator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* Finite Automata
* Coded by Amir El Bawab
* Date: 6 January 2015
* License: MIT License ~ Please read License.txt for more information about the usage of this software
* */
public class FiniteAutomata {
	
	// Attributes
	private Graph<State,Transition> FA;
	private char vertexPrefix;
	private Transition[] processTransitions;
	private State initialState;
	
	public static final char 	OTHER = 'O', 
								NON_ZERO = 'N', // 1-9
								SPACE = 'S',
								LETTER = 'L';
	
	/**
	 * Constructor
	 * @param vertexPrefix
	 */
	public FiniteAutomata(char vertexPrefix) {
		
		// DiGraph
		FA = new Graph<>(true);
		this.vertexPrefix = vertexPrefix;
	}
	
	/**
	 * Add a state
	 * @param status
	 * @return added state
	 */
	public State addState(int status){
		
		// Only one initial state
		if(initialState != null && (status == State.INITIAL || status == State.INITIAL_FINAL))
			throw new FiniteAutomataException("You cannot have more than one initial state");
		
		State state = new State(status);
		Vertex<State, Transition> vertex = FA.addVertex(state);
		state.setName(String.format("%c%d", vertexPrefix, vertex.getID()));
		state.setVertex(vertex);
		
		// Assign the initial state
		if(state.isInitial())
			initialState = state;
		
		// Return the added states
		return state;
	}
	
	/**
	 * Add a transition
	 * @param s1
	 * @param s2
	 * @param read
	 * @return Added transition
	 */
	public Transition addTransition(State s1, State s2, char read){
		
		// Create the transition
		Transition transition = new Transition(s1,s2,read);
		Edge<State, Transition> edge[] = FA.addEdge(s1.getVertex(), s2.getVertex(),transition,0.0);
		transition.setEdge(edge[0]);
		
		// Return added transition
		return transition;
	}
	
	/**
	 * Remove a state
	 * @param state
	 */
	public void removeState(State state){
		if(state.isInitial())
			initialState = null;
		FA.removeVertex(state.getVertex());
	}
	
	/**
	 * Remove a transition
	 * @param transition
	 */
	public void removeTransition(Transition transition){
		FA.removeEdge(transition.getEdge());
	}
	
	/**
	 * Get the array of states
	 * @return array of states
	 */
	public State[] getStates(){
		
		// Create the states array
		State[] states = new State[FA.vertices().size()];
		NodeIterator<Vertex<State, Transition>> iter = FA.vertices();
		int index=0;
		while(iter.hasNext())
			states[index++] = iter.next().getData();
		return states;
	}
	
	/**
	 * Get the array of transitions
	 * @return array of transitions
	 */
	public Transition[] getTransitions(){
		
		// Create the transitions array
		Transition[] transition = new Transition[FA.edges().size()];
		NodeIterator<Edge<State, Transition>> iter = FA.edges();
		int index=0;
		while(iter.hasNext())
			transition[index++] = iter.next().getLabel();
		return transition;
	}
	
	/**
	 * Get the initial state
	 * @return initial state
	 */
	public State getInitialState(){
		return initialState;
	}
	
	/**
	 * Process a string
	 * @param input
	 */
	public boolean process(String input){
		
		// An initial state is required to start
		if(initialState == null)
			throw new FiniteAutomataException("An initial state is required");
		
		// Store correct transitions
		DoublyLinkedList<Transition> list = new DoublyLinkedList<Transition>();
		boolean process = process_step(input, initialState, list);
		
		// Store transitions in an array
		processTransitions = new Transition[list.size()];
		int index = 0;
		NodeIterator<Transition> iterT = list.iterator();
		while(iterT.hasNext())
			processTransitions[index++] = iterT.next();
		
		// return process
		return process;
	}
	
	/**
	 * Process recursive call
	 * @param input
	 * @param state
	 * @param list
	 * @return boolean
	 */
	private boolean process_step(String input, State state, DoublyLinkedList<Transition> list){
		
		// If empty string and final state
		if(input.length() == 0 && state.isFinal())
			return true;
		
		// Iterate on outgoing transitions
		NodeIterator<Edge<State,Transition>> iterE = state.getVertex().getOutEdges();
		while(iterE.hasNext()){
			Transition transition = iterE.next().getLabel();
			
			// Check for lambda transitions
			if(transition.getRead() == Transition.LAMBDA){
				if(process_step(input, transition.getEdge().getV2().getData(), list)){
					list.addFirst(transition);
					return true;
				}
			
			// If not empty string and read match
			} else if(input.length() > 0 && transition.getRead() == input.charAt(0)){
				if(process_step(input.substring(1), transition.getEdge().getV2().getData(), list)){
					list.addFirst(transition);
					return true;
				}
			}
		}
		
		// No transition found
		return false;
	}
	
	
	/**
	 * Remove old initial state (if any). Choose an initial state
	 * @param stateID
	 */
	public void chooseInitialState(State state){
		
		// If the new initial state is the same as the old one, return
		if(initialState == state)
			return;
		
		// If there was an initial state
		if(initialState != null){
			
			// Adjust the old 
			if(initialState.getStatus() == State.INITIAL_FINAL)
				initialState.setStatus(State.FINAL);
			else
				initialState.setStatus(State.NORMAL);
		}
		
		// Adjust the initial state
		initialState = state;
		
		// Adjust the new initial state status
		if(state.getStatus() == State.FINAL)
			state.setStatus(State.INITIAL_FINAL);
		else
			state.setStatus(State.INITIAL);
	}
	
	/**
	 * Add a final state
	 * @param state
	 */
	public void addFinalState(State state){
		if(state.getStatus() == State.INITIAL){
			state.setStatus(State.INITIAL_FINAL);
		}else if(state.getStatus() == State.NORMAL){
			state.setStatus(State.FINAL);
		}
	}
	
	/**
	 * Remove a final state
	 * @param state
	 */
	public void removeFinalState(State state){
		if(state.getStatus() == State.FINAL)
			state.setStatus(State.NORMAL);
		else if(state.getStatus() == State.INITIAL_FINAL)
			state.setStatus(State.INITIAL);
	}
	
	/**
	 * Get last process transitions
	 * @return array of last process transitions
	 */
	public Transition[] getProcessTransitions() {
		return processTransitions;
	}
	
	/**
	 * Checks if a Finite automata is Deterministic:
	 * > No lambda transition. 
	 * > Every state has exactly one transition for each alphabet.
	 * @param alphabet
	 * @return boolean
	 */
	public boolean isDFA(char alphabet[]){
		
		// Keep track of the alphabet used for each state
		int alphabetCount[];
		
		// Iterate on states
		NodeIterator<Vertex<State, Transition>> iterS = FA.vertices();
		while(iterS.hasNext()){
			State currentState = iterS.next().getData();
			
			// Reset counter
			alphabetCount = new int[alphabet.length];
			
			// Iterate on all transitions for the current state
			NodeIterator<Edge<State,Transition>> iterT = currentState.getVertex().getOutEdges();
			while(iterT.hasNext()){
				char read = iterT.next().getLabel().getRead();
				
				// If lambda transition found
				if(read == Transition.LAMBDA)
					return false;
				
				// Loop on alphabets
				int i;
				for(i=0; i<alphabet.length; i++){
					if(read == alphabet[i]){
						if(++alphabetCount[i] == 2)
							return false;
						else 
							break;
					}
				}
				
				// If a character used but not in the alphabet
				if(i == alphabet.length)
					return false;
			}
			
			// Verify that all alphabets are used
			for(int i=0; i<alphabet.length; i++){
				if(alphabetCount[i] == 0)
					return false;
			}
		}
		
		// If passes all the tests
		return true;
	}
	
	/**
	 * Get a list of states and transitions
	 * @return List of states and transitions
	 */
	public String toString(){
		String output = "States:\n";
		NodeIterator<Vertex<State, Transition>> iterV = FA.vertices();
		while(iterV.hasNext())
			output += String.format("%s ", iterV.next().getData());
		
		output += "\n\nTransitions:\n";
		NodeIterator<Edge<State, Transition>> iterE = FA.edges();
		while(iterE.hasNext())
			output += String.format("%s\n", iterE.next().getLabel());

		return output;
	}
	
	/**
	 * Convert state machine to 
	 * @return
	 */
	public String toDot() {
		String output = "digraph finite_state_machine {\n";
		output += "	rankdir=LR;\n";

		// Final states
		output += "	node [shape = doublecircle];\n";
		output += "	";
		State[] states = getStates();
		for(State state : states)
			if(state.isFinal())
				output += String.format("\"%s\" ", state.getName() + " [" + state.getToken() + "]");
		output += ";\n";
		
		// Transitions
		output += "	node [shape = circle];\n";
		NodeIterator<Edge<State, Transition>> iterE = FA.edges();
		while(iterE.hasNext()){
			Edge<State, Transition> edge = iterE.next();
			Transition transition = edge.getLabel();
			State from = edge.getV1().getData();
			State to = edge.getV2().getData();
			String fromStr = from.getName();
			String toStr = to.getName();
			
			if(from.getToken() != null)
				fromStr = from.getName() + " [" + from.getToken() + "]";
			
			if(to.getToken() != null)
				toStr = to.getName() + " [" + to.getToken() + "]";
			
			output += String.format("	\"%s\" -> \"%s\" [label=\"%c\"];\n", fromStr, toStr, transition.getRead());
		}
		
		output += "}";
		return output;
	}
	
	/**
	 * Get all the labels on edges
	 * Definition:
	 * 	- N 1-9
	 * 	- L a-zA-Z
	 * 	- S space
	 * 	- O other
	 * @return
	 */
	public char[] getAllTransitionLabels() {
		Set<Character> labels = new HashSet<>();
		
		NodeIterator<Edge<State, Transition>> iterE = FA.edges();
		while(iterE.hasNext())
			labels.add(iterE.next().getLabel().getRead());
		
		// Remove the O label
		labels.remove(OTHER); // This is all the other characters
		
		// Convert to char[]
		char[] labelsArray = new char[labels.size()];
		int index = 0;
		for(Character c : labels)
			labelsArray[index++] = c;
		
		return labelsArray;
	}
	
	/////////////////////////// I/O HELPER ///////////////////////////////
	
	/**
	 * Parse input to populate the finite automata machine
	 * @param file
	 * @return Finite automata machine
	 * @throws IOException 
	 */
	public static FiniteAutomata inParser(String file) throws IOException{
		FiniteAutomata machine;
		State states[];
		Scanner scanFile = new Scanner(FiniteAutomata.class.getClass().getResource(file).openStream());
		String readLine;
		Pattern pattern;
		Matcher matcher;
		int[] statesStatus;
		String[] statesToken;
		char lambdaChar;
		
		// Read the machine prefix
		readLine = scanFile.nextLine();
		pattern = Pattern.compile("prefix\\s*=\\s*(.)");
		matcher = pattern.matcher(readLine);
		matcher.find();
		machine = new FiniteAutomata(matcher.group(1).charAt(0));
		
		// Read number of states
		readLine = scanFile.nextLine();
		pattern = Pattern.compile("states\\s*=\\s*(\\d+)");
		matcher = pattern.matcher(readLine);
		matcher.find();
		states = new State[Integer.parseInt(matcher.group(1))];
		statesStatus = new int[states.length];
		statesToken = new String[states.length];
		
		// Read the special lambda character
		readLine = scanFile.nextLine();
		pattern = Pattern.compile("lambda\\s*=\\s*(.)");
		matcher = pattern.matcher(readLine);
		matcher.find();
		lambdaChar = matcher.group(1).charAt(0);
		
		// Read the initial state
		readLine = scanFile.nextLine();
		pattern = Pattern.compile("initial\\s*=\\s*(\\d+)");
		matcher = pattern.matcher(readLine);
		matcher.find();
		statesStatus[Integer.parseInt(matcher.group(1))] = State.INITIAL;
		
		// While there more final states
		while(!(readLine = scanFile.nextLine()).equals(";") ){
			
			// Read the final states
			pattern = Pattern.compile("final\\s*=\\s*(\\d+)\\s*,\\s*(.+)");
			matcher = pattern.matcher(readLine);
			matcher.find();
			
			int index = Integer.parseInt(matcher.group(1));
			if(statesStatus[index] == State.INITIAL)
				statesStatus[index] = State.INITIAL_FINAL;
			else
				statesStatus[index] = State.FINAL;
			
			statesToken[index] = matcher.group(2);
		}
				
		// Initialize all states
		for(int i=0; i<states.length; i++){
			
			// Set state
			if(statesStatus[i] == 0)
				states[i] = machine.addState(State.NORMAL);
			else
				states[i] = machine.addState(statesStatus[i]);
			
			// Set label
			if(statesToken[i] != null)
				states[i].setToken(statesToken[i]);
		}
		
		// While there more backtrack states
		while(!(readLine = scanFile.nextLine()).equals(";") ){
			
			// Read the final states
			pattern = Pattern.compile("backtrack\\s*=\\s*(\\d+)");
			matcher = pattern.matcher(readLine);
			matcher.find();
			
			int index = Integer.parseInt(matcher.group(1));
			states[index].setBacktrack(true);
		}
		
		// Read all transitions
		while(!(readLine = scanFile.nextLine()).equals(";") ){
			
			// Read the final states
			pattern = Pattern.compile("(\\d+)\\s*,\\s*(\\d+)\\s*:\\s*([^\\s]+)\\s*");
			matcher = pattern.matcher(readLine);
			matcher.find();
			
			char read = matcher.group(3).charAt(0) == lambdaChar ? Transition.LAMBDA: matcher.group(3).charAt(0);
			machine.addTransition(states[Integer.parseInt(matcher.group(1))], states[Integer.parseInt(matcher.group(2))], read);
		}
		
		scanFile.close();
		return machine;
	}
	
	/**
	 * Export Finite Automata machine to input file
	 * @param filename
	 * @throws FileNotFoundException 
	 */
	public void export(String filename) throws FileNotFoundException{
		
		// Print writer
		PrintWriter write = new PrintWriter(filename);
		
		// Store all vertices of the machine
		Vertex<State, Transition> statesV[] = FA.vertices_array();
		
		write.println(String.format("prefix = %c", vertexPrefix));
		write.println(String.format("states = %d", FA.vertices().size()));
		write.println(String.format("lambda = %c", Transition.LAMBDA));
		if(initialState != null)
			write.println(String.format("initial = %d", FA.getIndexOfVertexByID(statesV, initialState.getVertex().getID())));
		else
			write.println("initial = <Enter a state id>");
		
		// Write all final states
		for(int i=0; i<statesV.length; i++){
			if(statesV[i].getData().isFinal())
				write.println(String.format("final = %d", i));
		}
		
		write.println(";");
		
		// Write all transitions
		for(int i=0; i<statesV.length; i++){
			
			NodeIterator<Edge<State,Transition>> iterE = statesV[i].getOutEdges();
			while(iterE.hasNext()){
				Transition transition = iterE.next().getLabel();
				write.println(String.format("%d,%d : %c", i, FA.getIndexOfVertexByID(statesV, transition.getEdge().getV2().getID()), transition.getRead()));
			}
		}
		
		write.println(";");
		write.close();
	}
}

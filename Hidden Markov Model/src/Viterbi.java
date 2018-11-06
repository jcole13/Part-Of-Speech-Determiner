import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Library class for part-of-speech implementation of the Viterbi algorithm.
 * Performs the core operations to tag parts of speech for given inputs.
 *
 * @author Jared Cole, Aaron Lee
 */
public class Viterbi {
	
	public static final double UNOBSERVED = -100; // Unobserved penalty
	public static final double a = 0.25; // Parameters for weighted sum (trigram)
	public static final double b = 0.70;
	public static final double c = 0.05;
	
	/**
	 * Runs the Viterbi algorithm on the given input sequence of observations given as a String[],
	 * and returns the corresponding parts-of-speech as a String[].
	 * 
	 * @param observations	the String[] containing as sequence of observations (input sentence)
	 * @param transitions	the transition map for parts-of-speech transitions to their transition scores
	 * @param emissions		the emissions map for words and parts-of-speech to observation scores
	 * @return	String[] containing the sequence of parts-of-speech tags as found by the Viterbi algorithm
	 */
	public static String[] viterbiAlgorithm(String[] observations, Map<String, Map<String, Double>> transitions,
			Map<String, Map<String, Double>> emissions){
		// Array to hold the path that results in the best final score; the sequence of tags to be returned
		String[] bestPath = new String[observations.length];
		
		// ArrayList of maps mapping each state at position i to the best predecessor state at position i-1
		List<Map<String, String>> backtrace = new ArrayList<Map<String, String>>();
		
		// Map of current states to current scores; currScores also makes a currStates set moot
		Map<String, Double> currScores = new HashMap<String, Double>();
		currScores.put("#", 0D); // Initial state with initial score (double) 0
		Map<String, Double> nextScores; // Variable to hold the currScores for the next observation
		double nextScore; // Variable to hold the next score
		
		// Iterate over all observations in the given array
		for (int i = 0; i < observations.length; i++) {
			nextScores = new HashMap<String, Double>(); // Initialize nextScores (or reset it)
			backtrace.add(new HashMap<String, String>()); // Add an entry in backtrace for i to i-1
			for (String state : currScores.keySet()) { // Iterate over all current states in currScores
				// If transitions doesn't contain a mapping for the current state, skip it
				if (!transitions.containsKey(state)) continue;
				
				// Iterate over all possible next states (all states that this state can transition to)
				for (String nextState : transitions.get(state).keySet()) {
					// Compute the next score for the given next state
					// Expression: current score + transition score + emissions (observation) score if the emissions
					// matrix has a valid entry for the current observation, or UNOBSERVED otherwise (? operator)
					nextScore = currScores.get(state) + Math.log(transitions.get(state).get(nextState))
							+ (!emissions.containsKey(observations[i]) || !emissions.get(observations[i])
							.containsKey(nextState) ? UNOBSERVED : emissions.get(observations[i]).get(nextState));
					
					// If nextScores doesn't already have a value for the current next state, or if the transition
					// from current state -> next state is the best one so far,
					if (!nextScores.containsKey(nextState) || nextScores.get(nextState) < nextScore) {
						nextScores.put(nextState, nextScore); // Add a mapping for this next state to its score
						backtrace.get(i).put(nextState, state); // Map next state back to this state in backtrace
					}
				}
			}
			currScores = nextScores; // Replace current scores with next scores
		}
		// Variable to hold the best final score found so far
		double bestScore = Double.NEGATIVE_INFINITY;
		// Variable to hold the current state in backtracing; should start at the best final state
		String backtraceState = null;
		// Iterate over all states (final states) in the final currScores for the last observation
		for (String finalState : currScores.keySet()) {
			if (currScores.get(finalState) > bestScore) { // If this final state has the best score so far
				backtraceState = finalState; // Update backtraceState to the current final state
				bestScore = currScores.get(finalState); // Update the best score so far
			}
		}
		// Iterate for i = observations.length-1 down to 0
		for (int i = observations.length-1; i >= 0; i--) {
			bestPath[i] = backtraceState; // Put backtraceState into the best path
			backtraceState = backtrace.get(i).get(backtraceState); // Update backtraceState to its own backtraced state
		}
		return bestPath;
	}
	
	/**
	 * Runs the Viterbi algorithm on the given input sequence of observations given as a String[],
	 * and returns the corresponding parts-of-speech as a String[]; then generates n additional most likely
	 * parts-of-speech after the observed values.
	 * 
	 * @param observations	the String[] containing as sequence of observations (input sentence)
	 * @param transitions	the transition map for parts-of-speech transitions to their transition scores
	 * @param emissions		the emissions map for words and parts-of-speech to observation scores
	 * @param n				the number of additional most-likely POS tags to generate
	 * @return	String[] containing the sequence of parts-of-speech tags as found by Viterbi decoding and generation
	 */
	public static String[] viterbiGenerate(String[] observations, Map<String, Map<String, Double>> transitions,
			Map<String, Map<String, Double>> emissions, int n) {
		// Array to hold the path that results in the best final score; the sequence of tags to be returned
		String[] bestPath = new String[observations.length+n];
		
		// ArrayList of maps mapping each state at position i to the best predecessor state at position i-1
		List<Map<String, String>> backtrace = new ArrayList<Map<String, String>>();
		
		// Map of current states to current scores; currScores also makes a currStates set moot
		Map<String, Double> currScores = new HashMap<String, Double>();
		currScores.put("#", 0D); // Initial state with initial score (double) 0
		Map<String, Double> nextScores; // Variable to hold the currScores for the next observation
		double nextScore; // Variable to hold the next score
		
		// Iterate over all observations in the given array
		for (int i = 0; i < observations.length; i++) {
			nextScores = new HashMap<String, Double>(); // Initialize nextScores (or reset it)
			backtrace.add(new HashMap<String, String>()); // Add an entry in backtrace for i to i-1
			for (String state : currScores.keySet()) { // Iterate over all current states in currScores
				// If transitions doesn't contain a mapping for the current state, skip it
				if (!transitions.containsKey(state)) continue;
				
				// Iterate over all possible next states (all states that this state can transition to)
				for (String nextState : transitions.get(state).keySet()) {
					// Compute the next score for the given next state
					// Expression: current score + transition score + emissions (observation) score if the emissions
					// matrix has a valid entry for the current observation, or UNOBSERVED otherwise (? operator)
					nextScore = currScores.get(state) + Math.log(transitions.get(state).get(nextState))
							+ (!emissions.containsKey(observations[i]) || !emissions.get(observations[i])
							.containsKey(nextState) ? UNOBSERVED : emissions.get(observations[i]).get(nextState));
					
					// If nextScores doesn't already have a value for the current next state, or if the transition
					// from current state -> next state is the best one so far,
					if (!nextScores.containsKey(nextState) || nextScores.get(nextState) < nextScore) {
						nextScores.put(nextState, nextScore); // Add a mapping for this next state to its score
						backtrace.get(i).put(nextState, state); // Map next state back to this state in backtrace
					}
				}
			}
			currScores = nextScores; // Replace current scores with next scores
		}
		// Now basically do the same thing but without observations for the next n states
		for (int i = observations.length; i < observations.length+n; i++) {
			nextScores = new HashMap<String, Double>(); // Initialize nextScores (or reset it)
			backtrace.add(new HashMap<String, String>()); // Add an entry in backtrace for i to i-1
			for (String state : currScores.keySet()) { // Iterate over all current states in currScores
				// If transitions doesn't contain a mapping for the current state, skip it
				if (!transitions.containsKey(state)) continue;
				
				// Iterate over all possible next states (all states that this state can transition to)
				for (String nextState : transitions.get(state).keySet()) {
					// Compute the next score based on the most likely transition
					// No observation score since there are no observations to go off of
					// Expression: current score + transition score
					nextScore = currScores.get(state) + Math.log(transitions.get(state).get(nextState));
					
					// If nextScores doesn't already have a value for the current next state, or if the transition
					// from current state -> next state is the best one so far,
					if (!nextScores.containsKey(nextState) || nextScores.get(nextState) < nextScore) {
						nextScores.put(nextState, nextScore); // Add a mapping for this next state to its score
						backtrace.get(i).put(nextState, state); // Map next state back to this state in backtrace
					}
				}
			}
			currScores = nextScores; // Replace current scores with next scores
		}
		// Variable to hold the best final score found so far
		double bestScore = Double.NEGATIVE_INFINITY;
		// Variable to hold the current state in backtracing; should start at the best final state
		String backtraceState = null;
		// Iterate over all states (final states) in the final currScores for the last observation
		for (String finalState : currScores.keySet()) {
			if (currScores.get(finalState) > bestScore) { // If this final state has the best score so far
				backtraceState = finalState; // Update backtraceState to the current final state
				bestScore = currScores.get(finalState); // Update the best score so far
			}
		}
		// Iterate for i = observations.length+n-1 down to 0
		for (int i = observations.length+n-1; i >= 0; i--) {
			bestPath[i] = backtraceState; // Put backtraceState into the best path
			backtraceState = backtrace.get(i).get(backtraceState); // Update backtraceState to its own backtraced state
		}
		return bestPath;
	}
	
	/**
	 * Runs the Viterbi algorithm with trigram interpolation on the given input sequence of observations 
	 * given as a String[], and returns the corresponding parts-of-speech as a String[].
	 * 
	 * @param observations	a String[] containing the sequence of observations to be fed into the algorithm
	 * @param trigram	the trigram probability map as given by the Trainer
	 * @param bigram	the bigram probability map (or transitions map) as given by the Trainer
	 * @param unigram	the unigram probability map as given by the Trainer
	 * @param emissions	the emissions probability map (observations map) as given by the Trainer
	 * @return	a String[] representing the best sequence of POS tags generated by the algorithm
	 */
	public static String[] viterbiTrigramInterpolate(String[] observations, Map<Pair, Map<String, Double>> trigram,
			Map<String, Map<String, Double>> bigram, Map<String, Double> unigram,
			Map<String, Map<String, Double>> emissions){
		// Array to hold the path that results in the best final score; the sequence of tags to be returned
		String[] bestPath = new String[observations.length];
		
		// ArrayList of maps mapping each state at position i to the best predecessor state at position i-1
		List<Map<String, String>> backtrace = new ArrayList<Map<String, String>>();
		
		// Map of current states to current scores; currScores also makes a currStates set moot
		Map<String, Double> currScores = new HashMap<String, Double>();
		currScores.put("#", 0D); // Initial state with initial score (double) 0
		Map<String, Double> nextScores; // Variable to hold the currScores for the next observation
		double nextScore; // Variable to hold the next score
		double compoundProbability; // Variable to hold the interpolated probability
		// Iterate over all observations in the given array
		for (int i = 0; i < observations.length; i++) {
			nextScores = new HashMap<String, Double>(); // Initialize nextScores (or reset it)
			backtrace.add(new HashMap<String, String>()); // Add an entry in backtrace for i to i-1
			for (String state : currScores.keySet()) { // Iterate over all current states in currScores
				// If transitions doesn't contain a mapping for the current state, skip it
				if (!bigram.containsKey(state)) continue;
				
				// Iterate over all possible next states (all states that this state can transition to)
				for (String nextState : bigram.get(state).keySet()) {
					// First compute the weighted compound transition score
					compoundProbability = a * (i > 1 ? // If i is greater than 1 (two observations already exist)
							(trigram.containsKey(new Pair(observations[i-1], observations[i])) ? // If trigram has an entry for the pair
							(trigram.get(new Pair(observations[i-1], observations[i])).containsKey(nextState) ? // and it has the next state
							(trigram.get(new Pair(observations[i-1], observations[i])).get(nextState)) // then get it
							: 0) : 0) : 0) + // Otherwise it's unobserved; no need to assign a value to unobserved result
							// Since we'll be logging the overall compound probability anyway
							b * bigram.get(state).get(nextState) + c * unigram.get(nextState); // Weighted sum
					
					// Compute the next score for the given next state
					// Expression: current score + compound transition score + emissions (observation) score assuming
					// matrix has a valid entry for the current observation, or UNOBSERVED otherwise (? operator)
					nextScore = currScores.get(state) + Math.log(compoundProbability)
							+ (!emissions.containsKey(observations[i]) || !emissions.get(observations[i])
							.containsKey(nextState) ? UNOBSERVED : emissions.get(observations[i]).get(nextState));
					
					// If nextScores doesn't already have a value for the current next state, or if the transition
					// from current state -> next state is the best one so far,
					if (!nextScores.containsKey(nextState) || nextScores.get(nextState) < nextScore) {
						nextScores.put(nextState, nextScore); // Add a mapping for this next state to its score
						backtrace.get(i).put(nextState, state); // Map next state back to this state in backtrace
					}
				}
			}
			currScores = nextScores; // Replace current scores with next scores
		}
		// Variable to hold the best final score found so far
		double bestScore = Double.NEGATIVE_INFINITY;
		// Variable to hold the current state in backtracing; should start at the best final state
		String backtraceState = null;
		// Iterate over all states (final states) in the final currScores for the last observation
		for (String finalState : currScores.keySet()) {
			if (currScores.get(finalState) > bestScore) { // If this final state has the best score so far
				backtraceState = finalState; // Update backtraceState to the current final state
				bestScore = currScores.get(finalState); // Update the best score so far
			}
		}
		// Iterate for i = observations.length-1 down to 0
		for (int i = observations.length-1; i >= 0; i--) {
			bestPath[i] = backtraceState; // Put backtraceState into the best path
			backtraceState = backtrace.get(i).get(backtraceState); // Update backtraceState to its own backtraced state
		}
		return bestPath;
	}
}
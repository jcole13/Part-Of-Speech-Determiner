import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Generates and stores the maps that represent the hidden Markov model for parts-of-speech.
 * The map values are trained based off of input data in the forms of words and their respective parts of speech.
 * 
 * @author Jared Cole, Aaron Lee
 */
public class Trainer{
	Map<String, Map<String, Double>> transitions; // map of string to map, where string is a POS and the map is of next POS and probabilities
	Map<String, Map<String, Double>> observed; // map of string to map where string is a word, and the map is of POS and probabilities
	Map<Pair, Map<String, Double>> trigram; // map of a pair of strings (POS 1 and POS 2) to a map mapping POS 3 to the probability
	Map<String, Double> unigram; // map of a string (POS) to the probability that a randomly selected POS is that one
	
	public Map<String, Map<String, Double>> getTransitions(){
		return transitions;
	}
	public Map<String, Map<String, Double>> getObservations(){
		return observed;
	}
	public Map<Pair, Map<String, Double>> getTrigram(){
		return trigram;
	}
	public Map<String, Double> getUnigram(){
		return unigram;
	}
	
	public Trainer(){ //create empty hashmaps
		transitions = new HashMap<String, Map<String, Double>>();
		observed = new HashMap<String, Map<String, Double>>();
		trigram = new HashMap<Pair, Map<String, Double>>();
		unigram = new HashMap<String, Double>();
	}
	
	/**
	 * The training algorithm that develops the Hidden Markov Model for the data (input sentences
	 * and parts of speech tags) provided
	 * 
	 * @param POSPath		path of the file with the training Parts of Speech
	 * @param wordPath		path of the file with the training sentences
	 * @throws FileNotFoundException thrown if either file located at the given path isn't found
	 * @throws IOException	thrown if input is invalid
	 */
	public void fileTrainer(String POSPath, String wordPath) throws FileNotFoundException, IOException {
		// Initialize sets and maps to hold counts (which later are used to generate probabilities)
		Set<String> seenWords = new HashSet<String>(); // Used to remember which words have already been seen
		
		// When transitions are calculated, only include POS that aren't the last in the sequence, since those that
		// are the last POS in the sequence can't be the first POS in a transition (since it's the last one)
		Map<String, Double> POSCounts = new HashMap<String, Double>(); // POS counts for the transitions calculation
		Map<String, Double> allPOS = new HashMap<String, Double>(); // POS counts for everything else (including last POS tag)
		Map<Pair, Double> pairCounts = new HashMap<Pair,Double>(); // Counts of all pairs so far, for trigram
		
		BufferedReader POSInput, wordInput;
		POSInput = new BufferedReader(new FileReader(POSPath));
		wordInput = new BufferedReader(new FileReader(wordPath));
		String currentTags, currentWords; // the current sentence / tags (should be the same amount of them)
		String word, tag; // Variable to hold current word and tag
		while((currentTags = POSInput.readLine()) != null && (currentWords = wordInput.readLine()) != null){
			String[] tagArray = currentTags.split("\\s+");
			String[] wordArray = currentWords.toLowerCase().split("\\s+");
			for(int i = 0; i < tagArray.length;i++){
				word = wordArray[i];
				tag = tagArray[i];
			
				if(i == 0) { // if the first word in the sentence
					if(!seenWords.contains("#")) { // putting the # in so the first word can be considered properly
						seenWords.add("#");
						transitions.put("#", new HashMap<String,Double>()); // if # has not been seen
						transitions.get("#").put(tag, 1.0);
						POSCounts.put("#", 1.0);
					}
					else {
						if(transitions.get("#").containsKey(tag)){ // If # has been seen and is in transitions
							transitions.get("#").put(tag, transitions.get("#").get(tag) + 1); // update its value
						}
						else{ // Otherwise make its entry
							transitions.get("#").put(tag, 1.0);
						}
						POSCounts.put("#", POSCounts.get("#") + 1); // and increment POSCounts value
					}					
				}
				//trigram adding
				if(i > 0 && i < tagArray.length - 1){ //while there is a previous and a next
					Pair pair = new Pair(tagArray[i-1], tagArray[i]); // Initialize a temporary pair
					String nextTag = tagArray[i+1]; // and next tag;
					if(pairCounts.containsKey(pair)){// if pair has been seen
						pairCounts.put(pair, pairCounts.get(pair) + 1); // Increment value
						if(trigram.get(pair).containsKey(nextTag)){ // If the next tag has been seen
							trigram.get(pair).put(nextTag, trigram.get(pair).get(nextTag)+1); // Increment transition count
						}
						else{ //not seen next tag
							//trigram.put(pair, new HashMap<String, Double>());
							trigram.get(pair).put(nextTag, 1D); // Put a new entry for transition count
						}
					}
					else{ //not seen pair
						pairCounts.put(pair, 1D); // Put a new entry into pairCounts
						trigram.put(pair, new HashMap<String,Double>()); // And a new entry into trigram
						trigram.get(pair).put(nextTag, 1D); // Set its value to 1.0
					}
				}
				
				if(seenWords.contains(word)){ // If word has been seen
					//seenWords.put(word, seenWords.get(word)+1);
					if(observed.get(word).containsKey(tag))observed.get(word).put(tag, observed.get(word).get(tag) + 1);
					else observed.get(word).put(tag, 1.0);
				} else { // If word has not been seen
					seenWords.add(word);
					observed.put(word, new HashMap<String,Double>());
					observed.get(word).put(tag, 1.0);
					//if(!transitions.containsKey(tag)) transitions.put(tag, new HashMap)
				}

				// update tags
				if(i < tagArray.length-1){ // everything but the last word (usually punctuation)
					// last word not included because there is no transition from last word
					if(POSCounts.containsKey(tag)){
						POSCounts.put(tag, POSCounts.get(tag) + 1);
						//totalPOS.put(tag, totalPOS.get(tag) +1);
					}
					
					if(!POSCounts.containsKey(tag)){
						POSCounts.put(tag, 1.0);
						//totalPOS.put(tag, 1.0));
					} 
					String nextTag = tagArray[i+1]; // Store next tag
					if (transitions.containsKey(tag)){ // If the transitions map has the current tag
						if (transitions.get(tag).containsKey(nextTag)){ // and it has the next tag
							// Increase count by 1
							transitions.get(tag).put(nextTag, transitions.get(tag).get(nextTag) + 1);
						}
						else { // Otherwise make a new entry for next tag
							transitions.get(tag).put(nextTag, 1.0);
						}
					}
					else { // Otherwise make a new entry for the current tag
						transitions.put(tag, new HashMap<String,Double>());
						transitions.get(tag).put(nextTag, 1.0);
					}
				}
				if(allPOS.containsKey(tag)){ // If allPOS has an entry for the current tag
					allPOS.put(tag, allPOS.get(tag) +1); // Increment it
				}
				else { // Otherwise make a new entry for it
					allPOS.put(tag, 1.0);
				}
				
			} // end for
		} // end while
		POSInput.close();
		wordInput.close();
		//System.out.println("pair counts: " + pairCounts);
		/*for(String[] sa : trigram.keySet()){
			for(String s : sa){
				System.out.print(s + " ");
			}
			System.out.print(trigram.get(sa));
			System.out.println();
		}*/
		//System.out.println("Trigram: " + trigram);
		
		// divide transitions by number of total times see that part of speech
		// No longer take natural log! In order for weighted sum of probability to work, we must
		// leave raw probabilities in these maps (we'll log the results later in Viterbi library).
		for(String POS : transitions.keySet()){
			//System.out.println("POS: " + POS);
			double totalPOS = POSCounts.get(POS);
			for(String subPOS : transitions.get(POS).keySet()){
				transitions.get(POS).put(subPOS, transitions.get(POS).get(subPOS) / totalPOS);
			}
		}
		
		// divide observed by number of times you see that word and take the natural log --
		// We SHOULD take the natural log for observed because it isn't part of the interpolation weighted sum.
		for(String observation : observed.keySet()){
			//double totalWord = seenWords.get(observation);
			for(String POSWord : observed.get(observation).keySet()){
				observed.get(observation).put(POSWord, Math.log(observed.get(observation)
						.get(POSWord) / allPOS.get(POSWord)));
			}
		}
		
		// Iterate over all trigram entries
		for(Pair p : trigram.keySet()){
			double totalSeenPair = pairCounts.get(p);
			for(String PosPair : trigram.get(p).keySet()){
				// Divide to get raw probabilities (will log later)
				trigram.get(p).put(PosPair, trigram.get(p).get(PosPair) / totalSeenPair);
			}
		}
		
		// Summing all POS occurrences
		double total = 0D;
		for(String POS : allPOS.keySet()){
			total += allPOS.get(POS);
		}
		// Now fill unigram with all its relevant raw probabilities
		for(String pos : allPOS.keySet()){
			unigram.put(pos, allPOS.get(pos) / total);
		}
	}
}
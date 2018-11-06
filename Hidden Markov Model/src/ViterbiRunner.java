import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Console-based interactive runner for the Viterbi algorithm for parts-of-speech tagging.
 * 
 * @author Jared Cole, Aaron Lee
 */
public class ViterbiRunner {
	
	public static void main(String [] args){
		Trainer trainer = new Trainer(); // Trainer object, holding necessary maps
		Scanner console = new Scanner(System.in); // Scanner to accept user input from console
		boolean trigram;
		
		// To catch any input/output run-time exceptions that might occur
		try {
			System.out.println("Welcome the the Viterbi Decoder\n\n");
			System.out.println("Which training corpus would you like to use? i.e. simple, brown etc...");
			System.out.print(">>>");
			String corpus = console.nextLine(); // Training corpus to use
			System.out.println();
			
			// Train trainer based on the given inputs; corpi must be given in the format "name"-train-tags.txt
			// for training tags and "name"-train-sentences.txt for the associated training sentences
			trainer.fileTrainer("inputs/" + corpus + "-train-tags.txt", "inputs/" + corpus + "-train-sentences.txt");
			System.out.println("Viterbi training successfully completed.");
			System.out.print("Yes or No: you would like to use the trigram method [y/n]:");
			String answer = console.nextLine().toLowerCase();
			if (answer.equals("y")) trigram = true;
			else trigram = false;
			
			System.out.println("Commands:");
			System.out.println("\tc or console\trun Viterbi on sentences directly through console");
			System.out.println("\tf or file\trun Viterbi on sentences from a file in disk");
			System.out.println("\ta or accuracy\tcheck the accuracy of the Viterbi algorithm on testing sentences");
			System.out.println("\t\t\tand correct tags from files in disk");
			System.out.println("\tg or generate\ttag sentence, then generate most likely following tags");

			System.out.print(">>>"); // To prompt the user
			String response = console.nextLine();
			if(response.toLowerCase().equals("f") || response.toLowerCase().equals("file")) fileInput(trainer,trigram); // file input
			else if(response.toLowerCase().equals("c") || response.toLowerCase().equals("console")) { // direct user input
				System.out.println();
				consoleInput(trainer, trigram);
			}
			// Check the accuracy
			else if(response.toLowerCase().equals("a") || response.toLowerCase().equals("accuracy")) {
				System.out.println();
				computeLoss(trainer, trigram);
			}
			else if(response.toLowerCase().equals("g") || response.toLowerCase().equals("generate")){
				System.out.println();
				consoleGenerate(trainer);
			}
			else System.out.println("Invalid command, quitting..."); // Only three valid options
		} catch (FileNotFoundException e) { // Catch a FileNotFoundException
			System.err.println("No such training files for the given corpus exist.");
		} catch (IOException e) { // Catch any other IOExceptions
			System.err.println("Exception occurred while reading the training file.");
		} finally {
			console.close(); // Close scanner
		}
	}
	
	/**
	 * Runs the Viterbi algorithm as trained by the given Trainer on a file found at the
	 * path given by the user through the console.
	 * 
	 * @param trainer	Trainer object with the maps necessary for Viterbi algorithm
	 * @param trigram	whether the user is in trigram mode or in default bigram mode
	 */
	public static void fileInput(Trainer trainer, boolean trigram) {
		Scanner sc = new Scanner(System.in); // Scanner object to accept console input
		 // For convenience, inputs are assumed to be located in the inputs/ directory
		System.out.print("Input the path (inputs/ assumed as root directory):");
		String inputPath = sc.nextLine(); // File name
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("inputs/" + inputPath)); // Read the test sentences
			String currentSentence;
			while((currentSentence = br.readLine()) != null) { // Each line (sentence)
				String[] sentenceArray = currentSentence.toLowerCase().split("\\s+"); // Split on whitespace
				String[] viterbiTags;
				if(trigram){
				viterbiTags = Viterbi.viterbiTrigramInterpolate(sentenceArray,trainer.getTrigram(),
						trainer.getTransitions(), trainer.getUnigram(), 
						trainer.getObservations()); // Run the Viterbi algorithm on the line
				}
				else viterbiTags = Viterbi.viterbiAlgorithm(sentenceArray,trainer.getTransitions(),trainer.getObservations());
				// For each state in the parts-of-speech sequence given by the Viterbi algorithm best path
				for (String state : viterbiTags)
					System.out.print(state + " "); // Print a line containing the sequence of POS states
				System.out.println(); // New line
			}
		}	
		catch (FileNotFoundException e) { // If the file at the given input path was not found
			System.out.println("No file found at the path input/" + inputPath);
		} 
		catch (IOException e) { // If another IOException occurred
			System.out.println("Exception occurred while reading the file at inputs/" + inputPath);
		}  
		finally {
			sc.close(); // Close the scanner
			try { // Try to close the file
				if (br == null) return; // the BufferedReader was never initialized anyway
				br.close(); // Close br
			} catch (IOException e) { // If an IOException was thrown
				System.err.println("Exception occurred while closing the file at inputs/" + inputPath);
			}
		}
	}
	
	/**
	 * Runs the Viterbi algorithm as trained by the given Trainer on lines given directly
	 * by the user through console input.
	 * 
	 * @param trainer	Trainer object with the maps necessary for Viterbi algorithm
	 * @param trigram 	whether the user is in trigram mode or default bigram mode
	 */
	public static void consoleInput(Trainer trainer, boolean trigram) {
		Scanner sc = new Scanner(System.in);
		System.out.println("Input the sentence, with spacing between each word and punctuation mark");
		System.out.println("\t-- >>> prompts a new sentence");
		System.out.println("\t-- type q to quit");
		String sentence;
		System.out.print(">>>");
		while(!(sentence = sc.nextLine()).equals("q")) { // If q, loop terminates
			String[] sentenceArray = sentence.toLowerCase().split("\\s+"); // Transform the given input into an array
			String[] viterbiTags;
			if(trigram){
			viterbiTags = Viterbi.viterbiTrigramInterpolate(sentenceArray,trainer.getTrigram(),
					trainer.getTransitions(), trainer.getUnigram(), 
					trainer.getObservations()); // Run the Viterbi algorithm on the line
			}
			else viterbiTags = Viterbi.viterbiAlgorithm(sentenceArray,trainer.getTransitions(),trainer.getObservations());
			// For each state in the parts-of-speech best path given by the Viterbi algorithm, 
			for (String state : viterbiTags)
				System.out.print(state + " "); // Print a line with the sequence of states
			System.out.println(); // Print a new line
			System.out.print(">>>");
		}
		System.out.println();
		System.out.println("Quitting"); // After the loop
		sc.close(); // Close scanner
	}
	
	/**
	 * Compares the tags given by the Viterbi algorithm on a testing file, whose path is given
	 * by user input through console, to the correct tags located in a second file whose path is given
	 * by user input through the console.
	 * 
	 * @param trainer	Trainer object with the maps necessary for Viterbi algorithm
	 * @param trigram 	whether the user is in trigram mode or default bigram mode
	 */
	public static void computeLoss(Trainer trainer, boolean trigram) {
		Scanner sc = new Scanner(System.in);
		System.out.print("Input the path of the test sentences file (inputs/ assumed as root directory): ");
		String testPath = sc.nextLine(); //test file
		System.out.println();
		System.out.print("Input the path of the correct tags file (inputs/ assumed as root directory): ");
		String tagPath = sc.nextLine(); //tag file

		int correct = 0; // To hold the number of correct tags
		int wrong = 0; // To hold the number of wrong tags

		BufferedReader tagFile = null; BufferedReader testFile = null; // Declare br here for exception handling
		try{
			tagFile = new BufferedReader(new FileReader("inputs/" + tagPath));
			testFile = new BufferedReader(new FileReader("inputs/" + testPath));
			String currentSentence, currentTags; //the tags and sentence that are being looked at
			
			// Use && instead of ||, so no short-circuiting occurs and both lines are instantiated
			while ((currentSentence = testFile.readLine()) != null && (currentTags = tagFile.readLine()) != null) {
				String[] sentenceArray = currentSentence.toLowerCase().split("\\s+"); // Whitespace split
				String[] tagArray = currentTags.split("\\s+"); // Whitespace split
				String[] viterbiTags;
				if(trigram){
				viterbiTags = Viterbi.viterbiTrigramInterpolate(sentenceArray,trainer.getTrigram(),
						trainer.getTransitions(), trainer.getUnigram(), 
						trainer.getObservations()); // Run the Viterbi algorithm on the line
				}
				else viterbiTags = Viterbi.viterbiAlgorithm(sentenceArray,trainer.getTransitions(),trainer.getObservations());
				if (tagArray.length != viterbiTags.length){ // Lines are not equal, something wrong with input files
					System.err.println("Mismatched line found in the two input files. Terminating...");
					return;
				}
				for (int i = 0; i < tagArray.length; i++) { // Iterate over all tags
					if(tagArray[i].equals(viterbiTags[i])) correct++; // Keep track of matching and wrong tags
					else wrong++;
				}
			}
			
			// Print the results from the testing to the console
			System.out.println("# Correct: " + correct);
			System.out.println("# Wrong: " + wrong);
			double percentage = ((double) correct / (correct + wrong)) * 100;
			System.out.println(percentage + "% correct");
		} catch (FileNotFoundException e) { // If one the files doesn't exist, notify the user
			System.err.println("Exception occurred as one or more input files do not exist.");
		} catch (IOException e) { // If any other generic IOExceptions occurred, notify the user
			System.err.println("Exception occurred while reading one of the input files.");
		}
		finally {
			sc.close(); // Close the scanner
			try { // Try to close the tags file
				if (tagFile == null) return; // the BufferedReader was never initialized anyway
				tagFile.close(); // Close br
			} catch (IOException e) { // If an IOException was thrown
				System.err.println("Exception occurred while closing the file at inputs/" + tagPath);
			}
			try { // Try to close the test file
				if (testFile == null) return; // the BufferedReader was never initialized anyway
				testFile.close(); // Close br
			} catch (IOException e) { // If an IOException was thrown
				System.err.println("Exception occurred while closing the file at inputs/" + testPath);
			}
		}
	}
	
	/**
	 * Runs the Viterbi algorithm as trained by the given Trainer on lines given directly
	 * by the user through console input, then generates additional most-likely POS.
	 * 
	 * @param trainer	Trainer object with the maps necessary for Viterbi algorithm
	 */
	public static void consoleGenerate(Trainer trainer) {
		Scanner sc = new Scanner(System.in);
		System.out.println("How many parts-of-speech tags would you like to generate per line?");
		int n = Integer.parseInt(sc.nextLine()); // Don't use nextInt or nextInt will overlap with nextLine below
		System.out.println("Input the sentence, with spacing between each word and punctuation mark");
		System.out.println("\t-- >>> prompts a new sentence");
		System.out.println("\t-- type q to quit");
		String sentence;
		System.out.print(">>>");
		while(!(sentence = sc.nextLine()).equals("q")) { // If q, loop terminates
			String[] sentenceArray = sentence.toLowerCase().split("\\s+"); // Transform the given input into an array
			
			int i = 0;
			// For each state in the parts-of-speech best path given by the Viterbi algorithm, 
			for (String state : Viterbi.viterbiGenerate(sentenceArray,trainer.getTransitions(),trainer.getObservations(), n)) {
				if (i == sentenceArray.length) System.out.print("| "); // Let the user know which tags are generated
				System.out.print(state + " "); // Print a line with the sequence of states
				i++;
			}
			System.out.println(); // Print a new line
			System.out.print(">>>");
		}
		System.out.println();
		System.out.println("Quitting"); // After the loop
		sc.close(); // Close scanner
	}

//	
}


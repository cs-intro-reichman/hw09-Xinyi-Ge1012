import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
		In read = new In(fileName);
        String txt = read.readAll();
        for(int i = 0; i < txt.length()-windowLength; ++i){
            String key = txt.substring(i, i+windowLength);
            if(CharDataMap.get(key) == null){
                CharDataMap.put(key, new List());
            }
        CharDataMap.get(key).update(txt.charAt(i+windowLength));
        calculateProbabilities(CharDataMap.get(key));    
        }
	}

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	public void calculateProbabilities(List probs) {				
		int total = 0;
        for(int i = 0; i < probs.getSize(); i++){
            CharData curr = probs.get(i);
            total += curr.count;
        }
        double cp = 0.0;
        for(int i = 0; i < probs.getSize(); i++){
            CharData curr = probs.get(i);
            curr.p = (double) curr.count/total;
            cp += curr.p;
            curr.cp = cp;
        }
	}

    // Returns a random character from the given probabilities list.
	public char getRandomChar(List probs) {
		double r = randomGenerator.nextDouble();
        for(int i = 0; i < probs.getSize(); i++){
            CharData curr = probs.get(i);
            if(curr.cp > r){
                return curr.chr;
            }
        }
        return probs.get(probs.getSize()-1).chr;
	}

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
		char chr;
        String gentxt = initialText;
        String window = "";
        if(initialText.length() >= textLength || initialText.length() < windowLength){
            return initialText;
        } else {
            window = initialText.substring(initialText.length()-windowLength);
            while(gentxt.length()-windowLength < textLength){
                if(CharDataMap.containsKey(window)){
                    chr = getRandomChar(CharDataMap.get(window));
                    gentxt += chr;
                    window = window.substring(1)+chr;
                } else {
                    return gentxt;
                }
            }
            return gentxt;
        }
	}

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
        String initialText = args[1];
        int gentxtLength = Integer.parseInt(args[2]);
		int windowLength = Integer.parseInt(args[0]);
        Boolean randomGeneration = args[3].equals("random");
        String fileName = args[4];
        LanguageModel lm;
        if(randomGeneration){
            lm = new LanguageModel(windowLength);
        } else {
            lm = new LanguageModel(windowLength, 20);
            lm.train(fileName);
            System.out.println(lm.generate(initialText, gentxtLength));
        }
    }
}

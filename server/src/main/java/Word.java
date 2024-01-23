import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Word {
	
	private static ArrayList<String> dictionary = new ArrayList<>();
	
	public static void init() {
		try {
			dictionary = new ArrayList<String>(Files.readAllLines(Paths.get("./src/main/resources/words/words.txt")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String source, output;

	public Word() {
		assert(dictionary.size() > 0);
		next();
	}
	
	public void next() {
		this.source = dictionary.get(Util.randomIndex(dictionary.size()));
		this.output = "";
		for (int i=0; i<this.source.length(); i++) {
			char secret = this.source.charAt(i);
			if (secret == '-' || secret == ' ')
				this.output+= secret;
			else
				this.output+= '_';
		}
	}
	
	public void revealLetter() {
		ArrayList<Integer> options = new ArrayList<Integer>();
		char[] map = this.output.toCharArray();
		for (int i=0; i<map.length; i++) {
			char letter = map[i];
			if (letter != '_' && letter != ' ' && letter != '-')
				options.add(i);
		}
		
		if (options.size() < 1) return;
		
		int luckyIndex = options.get(Util.randomIndex(options.size()));
		map[luckyIndex] = this.source.toCharArray()[luckyIndex];
		
		this.output = "";
		for (char letter: map)
			this.output+= letter;
	}
	
	public void revealAll() {
		this.output = this.source + "";
	}
	
	public String get() {
		return this.output;
	}
	
	public String expose() {
		return this.source;
	}
	
	public boolean check(String test) {
		return this.source.toLowerCase().equals(test.toLowerCase());
	}
	
}

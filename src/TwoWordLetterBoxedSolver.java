import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/*
ASSUMPTIONS:
-case-insensitive
-words can be >=1 letters
-multiple terms per line (phrase) are considered multiple words
-accepting each entry as-is, even ones like "sale m s", which would result in "sale", "m", and "s" all being words
-no need to discriminate between proper nouns and actual dictionary words
*/

public class TwoWordLetterBoxedSolver {

	private static Set<Character> puzzleLetters;
	private static Map<Character, Set<Character>> letterToValidNextLetters;
	private static Map<Character, Set<String>> validPuzzleWords;

	public static void main(String[] args) throws IOException {

		String dictionaryFilePath = args[0];
		String puzzleGrid = args[1];

		puzzleLetters = getCharSetFromString(puzzleGrid); //init the set of puzzle letters to be used later
		processPuzzleGrid(puzzleGrid);                    //create a map of <letter, [validNextLetters]>
		processDictionaryFile(dictionaryFilePath);        //extract solutions to the puzzle from a dictionary file
		printTwoWordSolutions();                          //print solutions

	}

	/*Process the given puzzle grid String into a map of <each letter, Set of all letters not on the same side>
	Ex.: "RME,WCL,KGT,IPA", where 'R' -> {'W','C','L','K','G','T','I','P','A'}*/
	private static void processPuzzleGrid(String puzzleGrid) {

		letterToValidNextLetters = new HashMap<>();

		for(String side : puzzleGrid.split(",")){
			for(char c : side.toCharArray()) {
				letterToValidNextLetters.put(c, getValidNextChars(side));
			}
		}

	}

	/*Read the dictionary file and initialize the map of <first letter, valid puzzle words>*/
	private static void processDictionaryFile(String dictionaryFilePath) throws IOException {

		validPuzzleWords = new HashMap<>();
		BufferedReader br = new BufferedReader(new FileReader(dictionaryFilePath));

		String line;
		while((line = br.readLine()) != null){

			//Lines can contain "phrases" with multiple words separated by a space
			for(String word : line.split(" ")){

				word = word.toUpperCase(); //Puzzle is case-insensitive

				if(isWordValid(word)){
					validPuzzleWords.computeIfAbsent(word.charAt(0), k -> new HashSet<>()).add(word);
				}

			}

		}

	}

	/*Given a map containing all valid words for the puzzle and the set of puzzle letters, return all
	two-word solutions in the format "word1,word2"*/
	private static void printTwoWordSolutions(){

		validPuzzleWords.values().stream().flatMap(Collection::stream).forEach(word -> {

			char lastLetter = word.charAt(word.length() - 1);
			Set<Character> missingLetters = getMissingLetters(word);

			//Add pairs of (word,match) for all words starting with this word's last letter and containing its missing letters
			validPuzzleWords.get(lastLetter)
					.stream()
					.filter(potentialMatch -> getCharSetFromString(potentialMatch).containsAll(missingLetters))
					.map(match -> word + "," + match)
					.forEach(System.out::println);

		});

	}

	/*Given a side and the set of puzzle letters, return the set of letters that can follow any letter on the side*/
	private static Set<Character> getValidNextChars(String side) {
		return puzzleLetters.stream().filter(x -> side.indexOf(x) == -1).collect(Collectors.toSet());
	}

	/*Convert the String into a set of Characters*/
	private static Set<Character> getCharSetFromString(String s) {
		return s.toUpperCase().chars().filter(Character::isLetter).mapToObj(c -> (char) c).collect(Collectors.toSet());
	}

	/*Returns the complementary set of letters of {validPuzzleLetters} - {key}*/
	private static TreeSet<Character> getMissingLetters(String word) {
		TreeSet<Character> keyPrime = new TreeSet<>(puzzleLetters);
		keyPrime.removeAll(getCharSetFromString(word));
		return keyPrime;
	}

	/*This checks if the given word is possible to spell using the rules of the puzzle*/
	private static boolean isWordValid(String word){

		if(word == null || word.isEmpty()) return false; //Null/empty
		if(!puzzleLetters.contains(word.charAt(0))) return false; //first letter isn't in the puzzle

		char prev = word.charAt(0), cur;
		for(int i=1; i<word.length(); i++){
			cur = word.charAt(i);
			if(!letterToValidNextLetters.get(prev).contains(cur)) return false; //invalid letter sequence
			prev = cur;
		}

		return true;

	}

}
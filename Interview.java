import java.util.*;
import java.io.*;
import java.net.*;

// Custom exception for if a user enters an invalid query.
class InvalidInputException extends Exception{
	public InvalidInputException(String errorMessage) {
		System.out.println("Invalid input :"+errorMessage);
	}
}

// Custom exception to indicate that a test case has failed.
class TestFailedException extends Exception{
	public TestFailedException(String test) {
		System.out.println("TEST FAILED: "+test);
	}
}


// Implements a search engine which takes a list of seed words and builds a trie out of it. Allows a user to
// enter a query and get a list of words which completes the query.
// Example:-
// Seed words: ["table","top","tank"]
// User Query: ["ta"]
// Output: ["table","tank"] 
class SearchEngine{
	
	// Node of a trie. Stores information about the |word| stored in it and a list of child |TrieNode|s.
	private class TrieNode{
		
		public boolean isFinished() {
			return finished;
		}

		public void setFinished(boolean finished) {
			this.finished = finished;
		}

		public HashMap<Character, TrieNode> getChildren() {
			return children;
		}

		public String getWord() {
			return word;
		}

		public void setWord(String word) {
			this.word = word;
		}

		// True when this node completes a word.
		private boolean finished;
		
		// List of children for this given node.
	    private HashMap<Character, TrieNode> children;
	    
	    // Stores the word if |finished| is true. Otherwise an empty string.
	    private String word;
	    
	    TrieNode(){
	    	this.finished = false;
	    	this.children = new HashMap<Character, TrieNode>();
	    	this.word = "";
	    }
	    
	}
	
	// Implements a trie data structure. Exposes utility functions like |insert()| and |search()|.
	private class Trie {
		
		// Root of the trie.
	    private TrieNode root;
	    
	    public Trie(){
	        root = new TrieNode();
	    }
	    
	    // Inserts a word in the trie.
	    public void insert(String word){
	        TrieNode rootPtr = root;
	        
	        // Iterate through all the characters of the given word. Each character acts as a member of the Trie and is used
	        // to traverse through the trie.
	        for(int i=0;i<word.length();i++){
	        	
	        	// If there is a |TrieNode| for the corresponding character, jump to that node, otherwise create a |TrieNode| for it
	        	// and then jump to the newly created node.
	            if(rootPtr.getChildren().containsKey(word.charAt(i))){
	            	rootPtr = rootPtr.getChildren().get(word.charAt(i));
	            }else{
	                TrieNode node = new TrieNode();
	                rootPtr.getChildren().put(word.charAt(i),node);
	                rootPtr = node;
	            }
	        }
	        
	        // Store the word in the TrieNode.
	        rootPtr.setFinished(true);
	        rootPtr.setWord(word);
	    }
	    
	    // Traverses through |prefix| in the trie and returns the |TrieNode| where the prefix ends if valid, otherwise returns null. 
	    public TrieNode search(String prefix){
	        TrieNode rootPtr = root;
	        
	        // Use the prefix to traverse the trie. If a character is not present in the trie, return null because the prefix did not
	        // exist in the seed word.
	        for(int i=0;i<prefix.length();i++){
	            if(rootPtr.getChildren().containsKey(prefix.charAt(i))){
	            	rootPtr = rootPtr.getChildren().get(prefix.charAt(i));
	            }else{
	                return null;
	            }
	        }
	        
	        return rootPtr;
	    }
	}
	
    private Trie trie;
    
    // Performs a DFS and populates |result| with a list of words which are |finished| in the subtree of |root|.
    private void dfs(final TrieNode root, List<String> result){
        if(root==null) return;
        
        // Add the finished word in the result list.
        if(root.isFinished()){
            result.add(root.getWord());
        }
        
        // Recursively traverse down the subtree to find other |finished| words.
        for(Character child : root.getChildren().keySet()){
            dfs(root.getChildren().get(child),result);
        }
    }
    
    public SearchEngine(){
        trie = new Trie();
    }
    
    // Builds the trie from the list of seed words.
    public void build(final List<String> seedWords){
        for(String word:seedWords){
            trie.insert(word);
        }
    }
    
    // Finds a list of words with the prefix |query|. If there are none, an empty list is returned.
    public List<String> query(final String query) throws InvalidInputException {
    	if(query==null) throw new InvalidInputException("null");
        List<String> result = new ArrayList<>();
        dfs(trie.search(query), result);
        return result;
    }
    
    
}

// TestSuite for SearchEngine.
class TestSuite{
	
	private SearchEngine engine;
	private List<String> seedWords;
	
	public TestSuite(final List<String> seedWords){
		this.engine = new SearchEngine();
		this.seedWords = seedWords;
		this.engine.build(seedWords);
	}
	
	public void executeTestSuite() throws TestFailedException {
		emptyStringTest();
		invalidQueryTest();
		parameterizedQueryTest();
	}
	
	// All the seed words should be returned when the query is empty.
	private void emptyStringTest() throws TestFailedException {
		final String query = "";
		try {
				List<String> actualSuggestions = engine.query(query);
				ArrayList<String> expectedSuggestions = (ArrayList<String>) ((ArrayList<String>) this.seedWords).clone();
				
				Collections.sort(actualSuggestions);
				Collections.sort(expectedSuggestions);
				
				if(!actualSuggestions.equals(expectedSuggestions)) {
					throw new TestFailedException("emptyStringTest");
				}
			}catch(InvalidInputException e) {
				throw new TestFailedException("emptyStringTest: "+e);
			}
		
		System.out.println("PASSED TEST: emptyStringTest");
			
	}
	
	// InvalidInputException should be thrown when users enters an invalid query.
	private void invalidQueryTest() throws TestFailedException{
		final String query = null;
		try {
			List<String> actualSuggestions = engine.query(query);
			throw new TestFailedException("InvalidQueryTest");
		}catch(InvalidInputException e) {
			System.out.println("PASSED TEST: invalidQueryTest");
		}
	}
	
	private void parameterizedQueryTest() throws TestFailedException {
		for(int i=0;i<26;i++) {
			characterQueryTest(Character.toString(i+'a'));
		}
	}
	
	// Search engine should be able to return a list of all words having the same prefix as |query|.
	private void characterQueryTest(final String query) throws TestFailedException{
		try {
				List<String> actualSuggestions = engine.query(query);
				List<String> expectedSuggestions = new ArrayList<>();
				
				for(String word: seedWords) {
					if(word.substring(0,query.length()).equals(query)) {
						expectedSuggestions.add(word);
					}
				}
				
				Collections.sort(actualSuggestions);
				Collections.sort(expectedSuggestions);
				
				if(!actualSuggestions.equals(expectedSuggestions)) {
					throw new TestFailedException("characterQueryTest: Query "+query);
				}
		}catch(InvalidInputException e) {
				throw new TestFailedException("characterQueryTest: "+e);
		}
		
		System.out.println("PASSED TEST characterQueryTest: "+query);
	}
	
	
}

public class Interview {
	
	// Utility function to fetch a list of words from a URL through a GET.
	static List<String> getHTML(String urlToRead) throws Exception {
      List<String> result = new ArrayList<>();
      URL url = new URL(urlToRead);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      try (BufferedReader reader = new BufferedReader(
                  new InputStreamReader(conn.getInputStream()))) {
          for (String line; (line = reader.readLine()) != null; ) {
              result.add(line);
              
          }
      }
      return result;
   }
	
    public static void main(String[] args) {
        String url = "https://raw.githubusercontent.com/dwyl/english-words/master/words_alpha.txt";
        try{
            List<String> wordList = getHTML(url);
            TestSuite suite = new TestSuite(wordList);
            suite.executeTestSuite();
        }catch(TestFailedException e){
            System.out.println("TEST FAILED "+e);
        }catch(Exception e) {
        	System.out.println(e.getMessage());
        }
    }
}

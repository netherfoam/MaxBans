package org.maxgamer.maxbans.banmanager;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;


public class TrieSet{
	private TrieNode top = new TrieNode();
	
	public TrieSet(){
		
	}
	
	/**
	 * Returns the node at a given key
	 */
	private TrieNode getNode(String s){
		TrieNode node = top;
		for(int i = 0; i < s.length(); i++){
			Character c = s.charAt(i);
			node = node.get(c);
			if(node == null) return null;
		}
		return node;
	}
	
	/**
	 * Calculates the nearest path to a point.
	 * This algorithm moves only down the tree.
	 * For example, 'Burg' is a key, 'Burgers'
	 * is another key, and 'Burge' is the String.
	 * This method will return a path to 'Burgers'
	 * instead of 'Burg'.
	 * @param s The partial key to search for
	 * @return The path to it. Note, this path is bottom -> up.
	 */
	private Path nearestPath(String s){
		List<Path> potential = new ArrayList<Path>();
		List<Path> next = new ArrayList<Path>();
		
		TrieNode node = getNode(s);
		
		//That node doesn't exist.
		if(node == null) return null;
		
		Path top = new Path(node);
		potential.add(top);
		
		
		while(!potential.isEmpty()){
			for(Path p : potential){
				if(p.getNode().isWord()){
					//Success! We're a valued node!
					return p;
				} else{
					//We have potential paths under us, even if we're not successful.
					//So, add those potential paths. If we have no children, this path
					//will just be ignored.  But WTF, how did we get a null at the
					//leaf of the tree?
					for(Entry<Character, TrieNode> entry : p.getNode().getChildMap().entrySet()){
						Character c = entry.getKey();
						TrieNode child = entry.getValue();
						Path q = new Path(p, child, c);
						next.add(q);
					}
				}
			}
			//Move next into potential.
			potential.clear();
			potential.addAll(next);
			next.clear();
		}
		//This will occur when some moron puts null values in our leaves
		return null;
	}
	
	/**
	 * Returns the shortest key that starts with the given string
	 * @param s The string that the desired key starts with
	 * @return The string of the nearest valued node
	 */
	public String nearestKey(String s){
		Path p = nearestPath(s);
		if(p == null) return null;
		
		char[] key = new char[p.getDepth()];
		
		while(p.getChar() != null){
			key[p.getDepth() - 1] = p.getChar();
			p = p.getPrevious();
		}
		return s + new String(key);
	}
	
	/**
	 * Returns true if this map contains the EXACT key String s
	 * @param s The key to check
	 * @return true if they're already here
	 */
	public boolean contains(String s){
		TrieNode node = this.getNode(s);
		return (node != null && node.isWord());
	}
	/**
	 * Removes a key/value set from this map
	 * @param s The key to remove
	 * @return The old value of the key
	 */
	public boolean remove(String s){
		//The length of the given string
		int length = s.length();
		
		//If they gave us an empty string, just delete the top.
		if(length == 0){
			return top.setWord(false);
		}
		//the current working node
		TrieNode node = top;
		//The last node that we cannot remove anything above
		TrieNode fork = node;
		//The character of the node below the fork we CAN remove
		Character forkChar = s.charAt(0); //Initially, it will be the first letter
		
		//Loop through the searched string
		for(int i = 0; i < length; i++){
			//The next character in the string
			Character c = s.charAt(i);
			
			//If the node has a value, or it has more than one child
			if(node.isWord() || node.getChildMap().size() > 1){
				fork = node;
				forkChar = c;
			}
			
			node = node.get(c);
			if(node == null) return false;
		}
		
		//The key wasn't in the map anyway
		if(node == null) return false;
		
		if(node.getChildMap().isEmpty()){
			//We have no children, so it's okay to remove us at the last fork
			fork.remove(forkChar);
			return node.isWord();
		}
		else{
			return node.setWord(false);
		}
	}
	
	/**
	 * Stores the specified value at the specified key
	 * @param key The String to store it under
	 * @param value The value to place there.
	 * @return True
	 * This will overwrite values if one exists.
	 */
	public boolean add(String key){
		int length = key.length();
		
		//Empty string handling
		if(length == 0){
			if(top.isWord()){
				return true;
			}
			else{
				top.setWord(true);
				return false;
			}
		}
		
		TrieNode parent;
		TrieNode node = top;
		Character c;
		int i = 0;
		
		do{
			//Get the next character
			c = key.charAt(i);
			parent = node;
			//Get the next child
			node = node.get(c);
			
			//If there was no child, make it
			if(node == null){
				node = parent.put(c);
			}
			
			i++;
		} while(i < length);
		
		//This is the bottom child. Set it's value.
		return node.setWord(true);
	}
	
	/**
	 * Returns a hash set of strings that all start with the given one.
	 * @param s The given string
	 * @return a hash set of strings that begin with the given string.
	 */
	public HashSet<String> matches(String s){
		//Values in this section of the map
		HashSet<String> keys = new HashSet<String>();
		
		TrieNode node = top;
		//Find the node matching String s
		for(int i = 0; i < s.length(); i++){
			Character c = s.charAt(i);
			node = node.get(c);
			//We don't have that node!
			if(node == null) return keys; //Empty hashmap
		}
		
		//Is this node a valid word?
		if(node.isWord()){
			keys.add(s);
		}
		
		HashSet<String> nodes = node.getChildKeys();
		//Get the values of every child node and store them
		for(String key : nodes){
			keys.add(s + key);
		}
		
		//Return the keys
		return keys;
	}
	
	/**
	 * Empties the map of all values
	 */
	public void clear(){
		top = new TrieNode();
	}
	
	public void debug(){
		PrintStream out = System.out;
		
		out.println("Size: " + this.size() + ", Empty: " + this.isEmpty());
		
		LinkedList<TrieNode> nodes = new LinkedList<TrieNode>();
		nodes.add(top);
		
		while(!nodes.isEmpty()){
			LinkedList<TrieNode> next = new LinkedList<TrieNode>();
			
			for(TrieNode node : nodes){
				System.out.print("   "+node.getChildMap().size()+"   ");
				next.addAll(node.getChildMap().values());
			}
			System.out.println();
			nodes = next;
		}
	}
	
	public boolean isAmbiguous(String s){
		TrieNode base = this.getNode(s);
		
		//No such value is stored... Not ambiguous.. I guess..
		if(base == null) return false;
		
		LinkedList<TrieNode> nodes = new LinkedList<TrieNode>();
		nodes.add(base);
		
		while(!nodes.isEmpty()){
			LinkedList<TrieNode> next = new LinkedList<TrieNode>();
			
			if(nodes.size() > 1){
				return true;
			}
			
			for(TrieNode node : nodes){
				next.addAll(node.getChildMap().values());
			}
			
			nodes = next;
		}
		return false;
	}

	/**
	 * Returns the number of values in this map that aren't null.
	 * @return The number of values in this map that aren't null.
	 */
	public int size() {
		int size = 0;
		
		LinkedList<TrieNode> nodes = new LinkedList<TrieNode>();
		
		//Start at the top node
		nodes.add(top);
		
		
		while(!nodes.isEmpty()){
			//Build a list of the nodes we need to search next
			LinkedList<TrieNode> subNodes = new LinkedList<TrieNode>();
			
			//Loop through all current nodes
			for(TrieNode node : nodes){
				//If the node is a word, increase size.
				if(node.isWord()){
					size++;
				}
				
				//Add the next set of nodes from this node to the next set
				subNodes.addAll(node.getChildMap().values());
			}
			//Do the next lot of nodes
			nodes = subNodes;
		}
		
		return size;
	}
	
	/**
	 * Returns all strings in this map
	 * @return all strings in this map
	 */
	public HashSet<String> values(){
		return this.matches("");
	}

	/**
	 * Returns true if the trie is empty.  Very fast.
	 * @return True if the trie is empty.  Very fast.
	 */
	public boolean isEmpty() {
		return (!top.isWord() && top.getChildMap().isEmpty());
	}
	
	/** 
	 * Represents a path in a trieMap to a particular TriNode.
	 * The path itself holds all required values to assemble a word.
	 * @author netherfoam
	 */
	private class Path{
		private Path previous;
		private TrieNode node;
		private Character c;
		private int depth;
		
		/**
		 * Represents a linking path.  This path links to the previous one.
		 * @param previous The path above this.
		 * @param node The node at this point on the path
		 * @param c The value of that node.
		 */
		public Path(Path previous, TrieNode node, Character c){
			if(node == null){
				throw new NullPointerException("Null node given");
			}
			this.c = c;
			this.previous = previous;
			this.node = node;
			this.depth = previous.getDepth() + 1;
		}
		/**
		 * Represents a beginning of a path. This path does not
		 * refer to any other paths, and does not have a valid character.
		 * @param node The node this path represents.  It is always the topmost path. 
		 */
		public Path(TrieNode node){
			if(node == null){
				throw new NullPointerException("Null path given");
			}
			this.node = node;
			this.depth = 0;
		}
		
		/**
		 * @return The depth from the initial path. Also the length of the path.
		 */
		public int getDepth(){
			return this.depth;
		}
		/**
		 * @return The path above this one
		 */
		public Path getPrevious(){
			return this.previous;
		}
		/**
		 * @return The TrieNode this path leads to
		 */
		public TrieNode getNode(){
			return node;
		}
		/**
		 * @return The character represented by the node at this path
		 */
		public Character getChar(){
			return this.c;
		}
	}
	
	private class TrieNode{
		private boolean isWord;
		private HashMap<Character, TrieNode> children = new HashMap<Character, TrieNode>(5);
		
		public TrieNode(){
			this(false);
		}
		public TrieNode(boolean isWord){
			this.isWord = isWord;
		}
		
		/**
		 * Returns true if this node represents the last letter in a complete word
		 * @return true if this node represents the last letter in a complete word
		 */
		public boolean isWord(){
			return isWord;
		}
		
		/**
		 * Returns the child node with the given character
		 * @param c The character to get
		 * @return the child node with the given character
		 */
		public TrieNode get(Character c){
			return children.get(c);
		}
		public TrieNode remove(Character c){
			return children.remove(c);
		}
		/**
		 * Adds a character to this nodes children
		 * @param c The character to add
		 * @return The new node
		 */
		public TrieNode put(Character c){
			//Build a new node
			TrieNode node = new TrieNode();
			//Put it in this one's children
			this.children.put(c, node);
			
			//Return the node we just created
			return node;
		}
		
		public HashSet<String> getChildKeys(){
			HashSet<String> values = new HashSet<String>();
			
			//Loop through this nodes children
			for(Entry<Character, TrieNode> entry : this.children.entrySet()){
				//The word so far
				String word = "";
				
				//The childs values
				TrieNode node = entry.getValue();
				Character key = entry.getKey();
				
				//If the child has something to contribute, add it
				if(node.isWord()){
					values.add(key.toString());
				}
				
				word += key;
				
				//Recursive
				for(String child : node.getChildKeys()){
					String childKey = word + child;
					values.add(childKey);
				}
			}
			
			return values;
		}
		
		public HashMap<Character, TrieNode> getChildMap(){
			return this.children;
		}
		
		/**
		 * Sets this to be a word (or not) based on the given value
		 * @param isWord Whether or not this should be a word
		 * @return The old value of isWord().
		 */
		public boolean setWord(boolean isWord){
			boolean old = this.isWord;
			this.isWord = isWord;
			return old;
		}
	}
}
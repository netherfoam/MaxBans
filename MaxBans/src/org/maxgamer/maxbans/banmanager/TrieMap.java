package org.maxgamer.maxbans.banmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;


public class TrieMap<V>{
	private TrieNode<V> top = new TrieNode<V>();
	
	public TrieMap(){
		
	}
	
	public V get(String s){
		TrieNode<V> node = getNode(s);
		if(node == null) return null;
		return node.getValue();
	}
	
	private TrieNode<V> getNode(String s){
		TrieNode<V> node = top;
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
		
		TrieNode<V> node = getNode(s);
		
		//That node doesn't exist.
		if(node == null) return null;
		
		Path top = new Path(node);
		potential.add(top);
		
		
		while(potential.size() > 0){
			for(Path p : potential){
				if(p.getNode().getValue() != null){
					//Success! We're a valued node!
					return p;
				} else{
					//We have potential paths under us, even if we're not successful.
					//So, add those potential paths. If we have no children, this path
					//will just be ignored.  But WTF, how did we get a null at the
					//leaf of the tree?
					for(Entry<Character, TrieNode<V>> entry : p.getNode().getChildMap().entrySet()){
						Character c = entry.getKey();
						TrieNode<V> child = entry.getValue();
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
		return get(s) != null;
	}
	/**
	 * Removes a key/value set from this map
	 * @param s The key to remove
	 * @return The old value of the key
	 */
	public V remove(String s){
		//the current working node
		TrieNode<V> node = top;
		//The last node that we cannot remove anything above
		TrieNode<V> fork = node;
		//The character of the node below the fork we CAN remove
		Character forkChar = null;
		
		//Loop through the searched string
		for(int i = 0; i < s.length(); i++){
			//The next character in the string
			Character c = s.charAt(i);
			
			//If the node has a value, or it has more than one child
			if(node.getValue() != null || node.getChildMap().size() > 1){
				fork = node;
				forkChar = c;
			}
			
			node = node.get(c);
			if(node == null) return null;
		}
		
		
		//The key wasn't in the map anyway
		if(node == null) return null;
		
		//The nodes current value
		V value = node.getValue();
		
		
		if(node.getChildMap().size() == 0){
			//We have no children, so it's okay to remove us at the last fork
			fork.remove(forkChar);
		}
		else{
			//We have children which we want to keep
			node.setValue(null);
		}
		
		//Return old value
		return value;
	}
	
	/**
	 * Stores the specified value at the specified key
	 * @param key The String to store it under
	 * @param value The value to place there.
	 * This will overwrite values if one exists.
	 */
	public void put(String key, V value){
		int length = key.length();
		
		//Empty string handling
		if(length == 0){
			this.top.setValue(value);
			return;
		}
		
		TrieNode<V> parent;
		TrieNode<V> node = top;
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
		node.setValue(value);
	}
	
	/**
	 * Returns a hashmap of key, values
	 * @param s The string to search for
	 * @return an hashmap of all values in the map
	 */
	public HashMap<String, V> matches(String s){
		//Values in this section of the map
		HashMap<String, V> values = new HashMap<String, V>();
		
		TrieNode<V> node = top;
		//Find the node matching String s
		for(int i = 0; i < s.length(); i++){
			Character c = s.charAt(i);
			node = node.get(c);
			//We don't have that node!
			if(node == null) return values; //Empty hashmap
		}
		
		//Does this node have a value to add?
		if(node.getValue() != null){
			values.put(s, node.getValue());
		}
		
		HashMap<String, TrieNode<V>> nodes = node.getChildValues();
		//Get the values of every child node and store them
		for(Entry<String, TrieNode<V>> entry : nodes.entrySet()){
			values.put(s + entry.getKey(), entry.getValue().getValue());
		}
		
		//Return the values
		return values;
	}
	
	/**
	 * Empties the map of all values
	 */
	public void clear(){
		top = new TrieNode<V>();
	}
	
	/** 
	 * Represents a path in a trieMap to a particular TriNode.
	 * The path itself holds all required values to assemble a word.
	 * @author netherfoam
	 */
	private class Path{
		private Path previous;
		private TrieNode<V> node;
		private Character c;
		private int depth;
		
		/**
		 * Represents a linking path.  This path links to the previous one.
		 * @param previous The path above this.
		 * @param node The node at this point on the path
		 * @param c The value of that node.
		 */
		public Path(Path previous, TrieNode<V> node, Character c){
			if(node == null){
				throw new NullPointerException("Idiot. Path(path, node, char)");
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
		public Path(TrieNode<V> node){
			if(node == null){
				throw new NullPointerException("Idiot. Path(node)");
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
		public TrieNode<V> getNode(){
			return node;
		}
		/**
		 * @return The character represented by the node at this path
		 */
		public Character getChar(){
			return this.c;
		}
	}
}
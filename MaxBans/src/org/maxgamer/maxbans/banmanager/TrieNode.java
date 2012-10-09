package org.maxgamer.maxbans.banmanager;

import java.util.HashMap;
import java.util.Map.Entry;

public class TrieNode<V>{
	private V value;
	private HashMap<Character, TrieNode<V>> children = new HashMap<Character, TrieNode<V>>(5);
	
	public TrieNode(){
	}
	
	public TrieNode(V value){
		this.value = value;
	}
	
	public V getValue(){
		return value;
	}
	
	public TrieNode<V> get(Character c){
		return children.get(c);
	}
	public TrieNode<V> remove(Character c){
		return children.remove(c);
	}
	/**
	 * Adds a character to this nodes children
	 * @param c The character to add
	 * @param value The value of that character
	 * @return The new node
	 */
	public TrieNode<V> put(Character c, V value){
		//Build the new node
		TrieNode<V> node = new TrieNode<V>(value);
		
		//Put it in this one's children
		this.children.put(c, node);
		
		//Return the node we just created
		return node;
	}
	/**
	 * Adds a character to this nodes children
	 * @param c The character to add
	 * @return The new node
	 */
	public TrieNode<V> put(Character c){
		//Build a new node
		TrieNode<V> node = new TrieNode<V>();
		//Put it in this one's children
		this.children.put(c, node);
		
		//Return the node we just created
		return node;
	}
	public HashMap<String, TrieNode<V>> getChildValues(){
		HashMap<String, TrieNode<V>> values = new HashMap<String, TrieNode<V>>();
		
		//Loop through this nodes children
		for(Entry<Character, TrieNode<V>> entry : this.children.entrySet()){
			//The word so far
			String word = "";
			
			//The childs values
			TrieNode<V> node = entry.getValue();
			Character key = entry.getKey();
			
			//If the child has something to contribute, add it
			if(node.getValue() != null){
				values.put(key.toString(), node);
			}
			
			word += key;
			
			for(Entry<String, TrieNode<V>> children : node.getChildValues().entrySet()){
				String childKey = word + children.getKey();
				values.put(childKey, children.getValue());
			}
		}
		
		return values;
	}
	
	public HashMap<Character, TrieNode<V>> getChildMap(){
		return this.children;
	}
	
	public void setValue(V value){
		this.value = value;
	}
}
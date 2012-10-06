package org.maxgamer.maxbans.banmanager;

import java.util.HashMap;
import java.util.Map.Entry;


public class TrieMap<V>{
	private TrieNode<V> top = new TrieNode<V>();
	
	public TrieMap(){
		
	}
	
	public V get(String s){
		TrieNode<V> node = top;
		for(int i = 0; i < s.length(); i++){
			Character c = s.charAt(i);
			node = node.get(c);
			if(node == null) return null;
		}
		return node.getValue();
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
		TrieNode<V> parent;
		TrieNode<V> node = top;
		Character c;
		int i = 0;
		
		do{
			//Get the next character
			//Get the next node's child
			//Return the value of the last child
			c = s.charAt(i);
			parent = node;
			
			node = node.get(c);
			if(node == null){
				return null;
			}
			
			i++;
		} while(i < s.length());
		
		parent.remove(c);
		return node.getValue();
	}
	
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
	 * @return an unlinked hashmap of all values in the map
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
	
	public void clear(){
		top = new TrieNode<V>();
	}
}
package org.maxgamer.maxbans.sync;

import java.util.HashMap;
import java.util.Map.Entry;

public class Packet{
	/** The command for this packet - Prefixed by the @ symbol when serialized. */
	private String command;
	/** The values for this packet.  These values are guaranteed to be the ones that were added, as long as a string was added. */
	private HashMap<String, String> values = new HashMap<String, String>();
	
	/**
	 * Represents a new packet with no command and no values. 
	 */
	public Packet(){}
	/**
	 * Represents a new packet with the given command and no values.
	 * This is the same as {@link Packet#setCommand(String)}.
	 * @param command The command for this packet.
	 */
	public Packet(String command){ super(); this.command = command; }
	
	/**
	 * Puts the given key to the given value in this packet, ready for
	 * sending.  These values are automatically escaped.
	 * @param key The key
	 * @param value The value
	 * @return This packet, for chaining.
	 */
	public Packet put(String key, String value){
		values.put(key, value);
		return this;
	}
	/**
	 * Deletes the given key and its value from this packet.
	 * @param key The key
	 * @return This packet, for chaining.
	 */
	public Packet remove(String key){
		values.remove(key);
		return this;
	}
	/**
	 * Puts the given key to the given value in this packet, ready for
	 * sending.  These values are automatically escaped.
	 * @param key The key
	 * @param value The value. This will be converted to a string using
	 * {@link String#valueOf(Object)}
	 * @return This packet, for chaining.
	 */
	public Packet put(String key, Object value){
		put(key, String.valueOf(value));
		return this;
	}
	/**
	 * Returns the value of the given key.
	 * @param key The key
	 * @return The string value.
	 */
	public String get(String key){
		return values.get(key);
	}
	/**
	 * Returns the command for this packet.
	 * @return The command
	 */
	public String getCommand(){
		return command;
	}
	/**
	 * Returns true if this has the given key. This is not the same as Packet.get(String) != null,
	 * as Packet.get(String) may return null if the key is associated with a null value, or if there
	 * is no key in the packet at all.
	 * @param key The key to search for
	 * @return True if the key is there, false if it is not there.
	 */
	public boolean has(String key){ return values.containsKey(key); }
	public Packet setCommand(String type){
		this.command = type;
		return this;
	}
	/**
	 * Returns a hashmap of properties. This is generally safe to edit yourself.
	 * @return The hashmap of properties.
	 */
	public HashMap<String, String> getProperties(){ return values; }
	
	/**
	 * Serializes this packet into string form.
	 * This is escaped, but may have characters like
	 * ChatColor.COLOR_CHAR which are not in ASCII
	 * @return The serial form.
	 */
	public String serialize(){
		StringBuilder sb = new StringBuilder("@").append(this.getCommand());
		
		for(Entry<String, String> entry : values.entrySet()){
			sb.append(" -"+entry.getKey());
			//Not all keys have values. If the value is null or empty, don't put it there.
			if(entry.getValue() != null && !entry.getValue().isEmpty()){
				sb.append(" ").append(escape(entry.getValue()));
			}
		}
		return sb.toString();
	}
	/**
	 * Reverses the effect of Packet.serialize() so
	 * that: <br/><br/>
	 * 
	 * Packet p = new Packet().setCommand("MaxBans")
	 * .put("reason", "is awesome!");<br/><br/>
	 * 
	 * Packet.unserialize(p.serialize).serialize.equals(p.serialize()) == true
	 * 
	 * <br/><br/>
	 * @param serial The serialized form.
	 * @return The serial in packet form.
	 */
	public static Packet unserialize(String serial){
		Packet prop = new Packet();
		String[] parts = serial.split(" -");
		prop.setCommand(parts[0].substring(1));
		
		for(int i = 1; i < parts.length; i++){ //Start at 1, because we already used the first part (above) as @command!
			String part = parts[i];
			String[] pieces = part.split(" ");
			String key = pieces[0];
			
			StringBuilder value = new StringBuilder();
			if(pieces.length > 1){
				value.append(pieces[1]);
				for(int j = 2; j < pieces.length; j++){
					value.append(" ").append(pieces[j]);
				}
			}
			
			//Store the key, and unescape the value.
			prop.put(key, unescape(value.toString()));
		}
		return prop;
	}
	
	//Note that there is no need to escape "exotic" characters like ChatColor.COLOR_CHAR, as data will be written using Connection.CHARSET.
	/** These character sequences will cause issues. */
	private static String[] escapers = {"-", "\\n", "@"};
	/** These character sequences will not. */
	private static String[] unescapers = {"\\-", "\\\\n", "\\@"};
	
	/** Escapes the given string and returns it */
	private static String escape(String s){
		for(int i = 0; i < escapers.length; i++){
			s = s.replace(escapers[i], unescapers[i]);
		}
		
		return s;
	}
	/** Escapes the given string and returns it */
	private static String unescape(String s){
		for(int i = 0; i < escapers.length; i++){
			s = s.replace(unescapers[i], escapers[i]);
		}
		
		return s;
	}
	
	/**
	 * Returns true if the given object is equal to this one.
	 * @param o The object to compare with.
	 */
	@Override
	public boolean equals(Object o){
		if(o != null && (o instanceof Packet)){
			Packet p = (Packet) o;
			if(p.getProperties().equals(this.getProperties()) && p.getCommand().equals(this.getCommand())){
				return true;
			}
		}
		return false;
	}
}
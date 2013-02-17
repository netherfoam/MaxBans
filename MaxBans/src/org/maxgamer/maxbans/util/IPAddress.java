package org.maxgamer.maxbans.util;

import java.util.Arrays;

public class IPAddress implements Comparable<IPAddress>{
	/** The numbers of the IP. Unfortunately, java has no unsigned byte type. */
	private int[] bytes = new int[4]; // [0] = first digits.
	
	public IPAddress(String s){
		String[] t = s.split("\\.");
		if(t.length != 4) throw new NumberFormatException("The given IP was invalid! ("  + s + ") (Only " + t.length + " byte sets given)"); 
		for(int i = 0; i < 4; i++){
			bytes[i] = Integer.parseInt(t[i]);
		}
	}
	
	/**
	 * Returns a copy of this IPAddress's bytes.
	 * @return The bytes. These can be edited and this
	 * object will not be modified.
	 */
	public int[] getBytes(){
		return bytes.clone();
	}
	
	/**
	 * Returns 1 if this address is greater than the given address.<br/>
	 * Returns 0 if they are the same address<br/>
	 * Returns -1 if this address is less than the given address.<br/>
	 */
	public int compareTo(IPAddress ip) {
		int[] bytes = ip.getBytes();
		
		for(int i = 0; i < bytes.length; i++){
			if(this.bytes[i] == bytes[i]) continue; // Same bytes
			if(this.bytes[i] > bytes[i]) return 1;
			if(this.bytes[i] < bytes[i]) return -1;
		}
		
		return 0; //The same the whole way through.
	}
	
	public boolean isGreaterThan(IPAddress ip){
		return this.compareTo(ip) == 1;
	}
	public boolean isLessThan(IPAddress ip){
		return this.compareTo(ip) == -1;
	}
	
	@Override
	public boolean equals(Object o){
		if(this == o) return true;
		if(o instanceof IPAddress){
			IPAddress ip = (IPAddress) o;
			return ip.bytes.equals(bytes); 
		}
		return false;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder(String.valueOf(bytes[0]));
		for(int i = 1; i < bytes.length; i++){
			sb.append("." + bytes[i]);
		}
		return sb.toString();
	}
	
	@Override
	public int hashCode(){
		return Arrays.hashCode(bytes);
	}
}
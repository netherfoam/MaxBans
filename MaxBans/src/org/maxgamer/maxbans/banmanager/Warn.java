package org.maxgamer.maxbans.banmanager;

public class Warn{
	private String reason;
	private String banner;
	private long expires;
	public Warn(String reason, String banner, long expires){
		this.reason = reason;
		this.banner = banner;
		this.expires = expires;
	}
	
	/**
	 * The reason they received the warning
	 * @return The reason they received the warning
	 */
	public String getReason(){
		return this.reason;
	}
	
	/**
	 * The name of the player who warned them.
	 * @return The name of the player who warned them.
	 */
	public String getBanner(){
		return this.banner;
	}
	
	/**
	 * Returns the epoch time in milliseconds this should become invalid
	 * @return the epoch time in milliseconds this should expire
	 */
	public long getExpires(){
		return this.expires;
	}
}
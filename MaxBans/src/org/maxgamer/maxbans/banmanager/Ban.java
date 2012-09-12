package org.maxgamer.maxbans.banmanager;

/**
 * The ban class
 * This will be stored in a hashmap in BanManager <String, Ban>
 * A ban is a ban, and not an ip ban or a temporary ban.  
 *
 */
public class Ban{
	public Ban(){
		
	}
	
	public String getReason(){
		return null;
	}
	public String getBanner(){
		return null;
	}
	public long getTimeOfBan(){
		return 0;
	}
}
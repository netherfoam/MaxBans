package org.maxgamer.maxbans.banmanager;

import java.util.HashMap;

import org.maxgamer.maxbans.MaxBans;

public class BanManager{
	private MaxBans plugin;
	private HashMap<String, Ban> bans = new HashMap<String, Ban>(10);
	private HashMap<String, TempBan> tempbans = new HashMap<String, TempBan>(10);
	private HashMap<String, IPBan> ipbans = new HashMap<String, IPBan>(10);
	private HashMap<String, TempIPBan> tempipbans = new HashMap<String, TempIPBan>(10);
	private HashMap<String, Mute> mutes = new HashMap<String, Mute>(10);
	private HashMap<String, TempMute> tempmutes = new HashMap<String, TempMute>(10);
	
	
	public BanManager(MaxBans plugin){
		this.plugin = plugin;
	}
	
	
	/**
	 * Reloads from the database.
	 * Don't use this except when starting up.
	 */
	public void reload(){
		
	}
    
    public boolean isMuted(String name) {
        return mutes.containsKey(name); 
    }
    
    public boolean isBanned(String name){
    	name = name.toLowerCase();
    	
    	Ban ban = bans.get(name);
    	if(ban != null){
    		return true;
    	}
    	
    	TempBan tempBan = tempbans.get(name);
    	if(tempBan != null){
    		if(System.currentTimeMillis() < tempBan.getTimeOfUnban()){
    			//TODO: Should we delete the ban from the DB/memory, or leave it as history?
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    public boolean isIPBanned(String ip){
    	IPBan ipBan = ipbans.get(ip);
    	if(ipBan != null){
    		return true;
    	}
    	
    	TempIPBan tempIPBan = tempipbans.get(ip);
    	if(tempIPBan != null){
    		if(System.currentTimeMillis() < tempIPBan.getTimeOfBan()){
    			//TODO: Should we delete the ban from the DB/memory, or leave it as history?
    			return true;
    		}
    	}
    	return false;
    }
    
    public void ban(String name, String reason, String banner){
    	Ban ban = new Ban(reason, banner, System.currentTimeMillis());
    	this.bans.put(name, ban);
    	//TODO: SQL and set up tables
    	plugin.getDB().getBuffer().addString("");
    }
}
package org.maxgamer.maxbans.banmanager;

import java.util.HashMap;
import org.maxgamer.maxbans.MaxBans;

public class BanManager{
	private MaxBans plugin;
	private HashMap<String, Ban> bans = new HashMap<>();
	private HashMap<String, TempBan> tempbans = new HashMap<>();
	private HashMap<String, IPBan> ipbans = new HashMap<>();
	private HashMap<String, TempIPBan> tempipbans = new HashMap<>();
	private HashMap<String, Mute> mutes = new HashMap<>();
	private HashMap<String, TempMute> tempmutes = new HashMap<>();
	
	
	public BanManager(MaxBans plugin){
		this.plugin = plugin;
	}
	
	
	/**
	 * Reloads from the database.
	 * Don't use this except when starting up.
	 */
	public void reload(){
		
	}
    
    public Mute isMuted(String name) {
        Mute mute = mutes.get(name);
        if (mute !=null) {
            return mute;
        }
        TempMute tempm = tempmutes.get(name);
        if (tempm !=null) {
            if (System.currentTimeMillis() < tempm.getTimeOfUnmute()) {
                return tempm;
            }
        }
        return null;
    }
    
    public Ban isBanned(String name){
    	name = name.toLowerCase();
    	
    	Ban ban = bans.get(name);
    	if(ban != null){
    		return ban;
    	}
    	
    	TempBan tempBan = tempbans.get(name);
    	if(tempBan != null){
    		if(System.currentTimeMillis() < tempBan.getTimeOfUnban()){
    			//TODO: Should we delete the ban from the DB/memory, or leave it as history?
    			return tempBan;
    		}
    	}
    	
    	return null;
    }
    
    public IPBan isIPBanned(String ip){
    	IPBan ipBan = ipbans.get(ip);
    	if(ipBan != null){
    		return ipBan;
    	}
    	
    	TempIPBan tempIPBan = tempipbans.get(ip);
    	if(tempIPBan != null){
    		if(System.currentTimeMillis() < tempIPBan.getTimeOfBan()){
    			//TODO: Should we delete the ban from the DB/memory, or leave it as history?
    			return tempIPBan;
    		}
    	}
    	return null;
    }
}
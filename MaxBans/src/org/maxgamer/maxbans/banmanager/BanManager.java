package org.maxgamer.maxbans.banmanager;

import java.util.HashMap;

import org.maxgamer.maxbans.MaxBans;

public class BanManager{
	private MaxBans plugin;
	private HashMap<String, Ban> bans = new HashMap<String, Ban>(10);
	private HashMap<String, IPBan> tempbans = new HashMap<String, IPBan>(10);
	private HashMap<String, TempIPBan> ipbans = new HashMap<String, TempIPBan>(10);
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
}
package org.maxgamer.maxbans.commands.bridge;

import java.io.File;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.util.Util;

public class DynamicBanBridge implements Bridge{

	@Override
	public void export() {
		throw new NotImplementedException("DynamicBan export is not implemented.");
	}

	@Override
	public void load() {
		MaxBans plugin = MaxBans.instance;
		File folder = new File("plugins/DynamicBan/data");
		FileConfiguration cfg;
		
		if(folder.exists() == false){
			System.out.println("Invalid folder!");
		}
		
		//Banned players
		cfg = YamlConfiguration.loadConfiguration(new File(folder, "banned-players.dat"));
		Set<String> bannedPlayers = cfg.getKeys(false);
		System.out.println("Loading " + bannedPlayers.size() + " players.");
		for(String s : bannedPlayers){
			plugin.getBanManager().ban(s, cfg.getString(s), "Console");
		}
		
		//Banned IPs
		cfg = YamlConfiguration.loadConfiguration(new File(folder, "banned-ips.dat"));
		Set<String> bannedIps = cfg.getKeys(false);
		System.out.println("Loading " + bannedIps.size() + " players.");
		for(String s : bannedIps){
			plugin.getBanManager().ipban(s, cfg.getString(s), "Console");
		}
		
		//Tempbans - DynamicBan stores times in seconds, not milliseconds.
		cfg = YamlConfiguration.loadConfiguration(new File(folder, "temp-bans.dat"));
		Set<String> tempbans = cfg.getKeys(false);
		System.out.println("Loading " + tempbans.size() + " players.");
		for(String s : tempbans){
			if(Util.isIP(s)){
				plugin.getBanManager().tempipban(s, "Misconduct", "Console", cfg.getLong(s) * 1000);
			}
			else{
				plugin.getBanManager().tempban(s, "Misconduct", "Console", cfg.getLong(s) * 1000);
			}
		}
		
		//Mutes - DynamicBan stores times in seconds, not milliseconds.
		cfg = YamlConfiguration.loadConfiguration(new File(folder, "muted-players.dat"));
		Set<String> mutes = cfg.getKeys(false);
		System.out.println("Loading " + mutes.size() + " players.");
		for(String s : mutes){
			plugin.getBanManager().tempmute(s, "Console", "DynamicBan Mute", cfg.getLong(s) * 1000);
		}
	}
	
}
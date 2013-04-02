package org.maxgamer.maxbans.commands.bridge;

import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.Ban;
import org.maxgamer.maxbans.banmanager.IPBan;
import org.maxgamer.maxbans.banmanager.TempBan;
import org.maxgamer.maxbans.banmanager.TempIPBan;

public class VanillaBridge implements Bridge{

	@Override
	public void export() {
		System.out.println("Exporting to Vanilla bans...");
		
		MaxBans plugin = MaxBans.instance;
		//Import vanilla.
		for(Entry<String, Ban> entry : plugin.getBanManager().getBans().entrySet()){
			if(entry.getValue() instanceof TempBan){
				//So we skip it in good faith.
				continue;
			}
			
			OfflinePlayer p = Bukkit.getOfflinePlayer(entry.getKey());
			if(!p.isBanned()) p.setBanned(true);
		}
		
		for(Entry<String, IPBan> entry : plugin.getBanManager().getIPBans().entrySet()){
			if(entry.getValue() instanceof TempIPBan){
				//So we skip it in good faith.
				continue;
			}
			
			Bukkit.banIP(entry.getKey());
		}
	}

	@Override
	public void load() {
		System.out.println("Importing from Vanilla bans...");
		
		MaxBans plugin = MaxBans.instance;
		//Export vanilla.
		for(OfflinePlayer p : Bukkit.getBannedPlayers()){
			plugin.getBanManager().ban(p.getName(), "Vanilla Ban", "Console");
		}
		for(String ip : Bukkit.getIPBans()){
			plugin.getBanManager().ipban(ip, "Vanilla IP Ban", "Console");
		}
	}
	
}
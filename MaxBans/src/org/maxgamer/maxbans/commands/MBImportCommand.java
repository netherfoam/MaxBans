package org.maxgamer.maxbans.commands;

import java.io.File;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.Util;


public class MBImportCommand extends CmdSkeleton{
    public MBImportCommand(){
        super("mbimport", "maxbans.import");
        namePos = -1;
    }
    
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length == 0){
			sender.sendMessage(Formatter.primary + "MaxBans Importer:");
			sender.sendMessage(Formatter.secondary + "/mbimport vanilla " + Formatter.primary + " - Imports vanilla bans.");
			sender.sendMessage(Formatter.secondary + "/mbimport dynamicban " + Formatter.primary + " - Imports dynamicBan bans.");
		}
		else{
			if(args[0].equalsIgnoreCase("vanilla")){
				//Import vanilla.
				for(OfflinePlayer p : Bukkit.getBannedPlayers()){
					plugin.getBanManager().ban(p.getName(), "Vanilla Ban", "Console");
				}
				for(String ip : Bukkit.getIPBans()){
					plugin.getBanManager().ipban(ip, "Vanilla IP Ban", "Console");
				}
				
				sender.sendMessage(Formatter.secondary + "Success.");
			}
			else if(args[0].equalsIgnoreCase("dynamicban")){
				sender.sendMessage(ChatColor.RED + "Importing bans. This may take a while!");
				try{
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
						plugin.getBanManager().tempmute(s, "Console", cfg.getLong(s) * 1000);
					}
					
					sender.sendMessage(ChatColor.GREEN + "Import successful!");
				}
				catch(Exception e){
					sender.sendMessage(ChatColor.RED + "Error importing: " + e.getMessage());
				}
			}
			else{
				sender.sendMessage(Formatter.secondary + "Failed.  No known importer: " + args[0]);
			}
		}
		
		return true;
	}
}

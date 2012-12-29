package org.maxgamer.maxbans.commands;

import java.util.HashSet;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.util.Util;

public class DupeIPCommand implements CommandExecutor{
    private MaxBans plugin;
	ChatColor banned = ChatColor.RED;
	ChatColor online = ChatColor.GREEN;
	ChatColor offline = ChatColor.GRAY;
    
    public DupeIPCommand(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("maxbans.dupeip")){
			sender.sendMessage(plugin.color_secondary + "You don't have permission to do that");
			return true;
		}
		String usage = plugin.color_secondary + "Usage: /dupeip <player>";
		
		if(args.length > 0){
			String name = args[0];
			
			String ip;
			if(!Util.isIP(name)){
				name = plugin.getBanManager().match(name);
				if(name == null){
					name = args[0]; //Use exact name then.
				}
				ip = plugin.getBanManager().getIP(name);
				
				if(ip == null){
					sender.sendMessage(plugin.color_primary + "Player " + plugin.color_secondary + name + plugin.color_primary + " has no IP history.");
					return true;
				}
			}
			else{
				ip = name;
			}
			

			
			sender.sendMessage(plugin.color_primary + "Scanning " + getChatColor(name) + name + plugin.color_primary + " on " + plugin.color_secondary + ip + ChatColor.WHITE + ".  [" + banned + "Banned" + ChatColor.WHITE + "] [" + online + "Online" + ChatColor.WHITE + "] [" + offline + "Offline" + ChatColor.WHITE + "]");
			
			StringBuilder sb = new StringBuilder();
			
			HashSet<String> ips = plugin.getBanManager().getUsers(ip);
			if(ips != null){ //Could be null if we're looking up an invalid IP
				for(String dupe : ips){
					if(dupe.equalsIgnoreCase(name)) continue; //Same guy is not a duplicate of himself.
					sb.append(getChatColor(dupe).toString()+ dupe + ", ");
				}
			}
			
			if(sb.length() > 0){
				sb.replace(sb.length() - 2, sb.length(), "");
				sender.sendMessage(sb.toString());
			}
			else{
				sender.sendMessage(plugin.color_primary + "No duplicates!");
			}
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
	
	public ChatColor getChatColor(String name){
		if(Util.isIP(name)){
			if(plugin.getBanManager().getIPBan(name) != null) return banned;
			else return offline; //Well...
		}
		else{
			if(plugin.getBanManager().getBan(name) != null) return banned;
			if(Bukkit.getPlayerExact(name) != null) return online;
			return offline;
		}
	}
}

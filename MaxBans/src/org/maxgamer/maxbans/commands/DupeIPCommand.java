package org.maxgamer.maxbans.commands;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.MaxBans;

public class DupeIPCommand implements CommandExecutor{
    private MaxBans plugin;
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
			
			name = plugin.getBanManager().match(name);
			if(name == null){
				name = args[0]; //Use exact name then.
			}
			
			String ip = plugin.getBanManager().getIP(name);
			
			if(ip == null){
				sender.sendMessage(plugin.color_primary + "Player " + plugin.color_secondary + name + plugin.color_primary + " has no IP history.");
			}
			
			sender.sendMessage(plugin.color_primary + "Scanning " + plugin.color_secondary + name + plugin.color_primary + " on " + plugin.color_secondary + ip);
			
			HashMap<String, String> ipHistory = plugin.getBanManager().getIPHistory();
			StringBuilder sb = new StringBuilder();
			
			for(Entry<String, String> entry : ipHistory.entrySet()){
				if(entry.getValue().equals(ip) && !entry.getKey().equals(name.toLowerCase())){
					String dupe = entry.getKey();
					if(plugin.getBanManager().getBan(dupe) != null){
						sb.append(ChatColor.RED);
						sb.append(dupe);
						sb.append(", ");
					}
					else{
						sb.append(ChatColor.GREEN);
						sb.append(dupe);
						sb.append(", ");
					}
				}
			}
			
			if(sb.length() > 0){
				sb.replace(sb.length() - 1, sb.length(), "");
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
}

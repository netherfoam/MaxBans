package org.maxgamer.maxbans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.MaxBans;

public class CheckIPCommand implements CommandExecutor{
    private MaxBans plugin;
    public CheckIPCommand(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("maxbans.checkip")){
			sender.sendMessage(plugin.color_secondary + "You don't have permission to do that");
			return true;
		}
		
		String usage = plugin.color_secondary + "Usage: /checkip <player>";
		
		if(args.length > 0){
			String name = args[0];
			
			name = plugin.getBanManager().match(name);
			if(name == null){
				name = args[0]; //Use exact name then.
			}
			
			String ip = plugin.getBanManager().getIP(name);
			
			if(ip == null){
				sender.sendMessage(plugin.color_primary + "Player " + plugin.color_secondary + name + plugin.color_primary + " has no recorded IP history.");
			}
			else{
				sender.sendMessage(plugin.color_primary + "Player " + plugin.color_secondary + name + plugin.color_primary + " last used the IP " + plugin.color_secondary + ip);
			}
			
			
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}

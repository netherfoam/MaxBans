package org.maxgamer.maxbans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.util.Formatter;

public class CheckIPCommand implements CommandExecutor{
    private MaxBans plugin;
    public CheckIPCommand(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("maxbans.checkip")){
			sender.sendMessage(Formatter.secondary + "You don't have permission to do that");
			return true;
		}
		
		String usage = Formatter.secondary + "Usage: /checkip <player>";
		
		if(args.length > 0){
			String name = args[0];
			
			name = plugin.getBanManager().match(name);
			if(name == null){
				name = args[0]; //Use exact name then.
			}
			
			String ip = plugin.getBanManager().getIP(name);
			
			if(ip == null){
				sender.sendMessage(Formatter.primary + "Player " + Formatter.secondary + name + Formatter.primary + " has no recorded IP history.");
			}
			else{
				sender.sendMessage(Formatter.primary + "Player " + Formatter.secondary + name + Formatter.primary + " last used the IP " + Formatter.secondary + ip);
			}
			
			
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}

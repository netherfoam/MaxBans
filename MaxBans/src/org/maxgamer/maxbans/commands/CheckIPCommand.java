package org.maxgamer.maxbans.commands;

import org.bukkit.ChatColor;
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
		String usage = ChatColor.RED + "Usage: /checkip <player>";
		
		if(args.length > 0){
			String name = args[0];
			
			name = plugin.getBanManager().match(name);
			if(name == null){
				name = args[0]; //Use exact name then.
			}
			
			String ip = plugin.getBanManager().getIP(name);
			
			sender.sendMessage(ChatColor.AQUA + "Player " + ChatColor.RED + name + ChatColor.AQUA + " last used the IP " + ChatColor.RED + ip);
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}

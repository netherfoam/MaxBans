package org.maxgamer.maxbans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.util.Util;

public class WarnCommand implements CommandExecutor{
    private MaxBans plugin;
    public WarnCommand(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("maxbans.warn")){
			sender.sendMessage(plugin.color_secondary + "You don't have permission to do that");
			return true;
		}
		String usage = plugin.color_secondary + "Usage: /warn <player> <reason>";
		
		if(args.length > 1){
			String name = args[0];
			
			name = plugin.getBanManager().match(name);
			if(name == null){
				name = args[0]; //Use exact name then.
			}
			
			String reason = Util.buildReason(args);
			String banner = Util.getName(sender);
			
			plugin.getBanManager().announce(plugin.color_secondary + name + plugin.color_primary + " has been warned for " + plugin.color_secondary + reason + plugin.color_primary + " by " + plugin.color_secondary + banner + plugin.color_primary + ".");
			plugin.getBanManager().warn(name, reason, banner);
			
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}

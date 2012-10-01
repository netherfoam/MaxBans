package org.maxgamer.maxbans.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.MaxBans;

public class WarnCommand implements CommandExecutor{
    private MaxBans plugin;
    public WarnCommand(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String usage = ChatColor.RED + "Usage: /warn <player> <reason>";
		
		if(args.length > 1){
			String name = args[0];
			String banner;
			
			String reason = plugin.getBanManager().buildReason(args);
			
			if(sender instanceof Player){
				banner = ((Player) sender).getName();
			}
			else{
				banner = "Console";
			}
			
			plugin.getBanManager().warn(name, reason, banner);
			plugin.getBanManager().announce(ChatColor.RED + name + ChatColor.AQUA + " has been warned for " + ChatColor.RED + reason + ChatColor.AQUA + " by " + ChatColor.RED + banner + ChatColor.AQUA + ".");
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}

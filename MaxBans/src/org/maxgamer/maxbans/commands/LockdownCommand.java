package org.maxgamer.maxbans.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.MaxBans;

public class LockdownCommand implements CommandExecutor{
    private MaxBans plugin;
    public LockdownCommand(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length > 0){
			String reason = args[0];
			
			plugin.getBanManager().lockdown = true;
			plugin.getBanManager().lockdownReason = reason;
			sender.sendMessage(ChatColor.AQUA + "Lockdown enabled.  Reason: " + ChatColor.RED + reason + ChatColor.AQUA + ".");
			
			plugin.getConfig().set("lockdown", true);
			plugin.getConfig().set("lockdown-reason", reason);
		}
		else{
			if(plugin.getBanManager().lockdown){
				plugin.getBanManager().lockdown = false;
				plugin.getBanManager().lockdownReason = "";
				sender.sendMessage(ChatColor.AQUA + "Lockdown disabled.");
				
				plugin.getConfig().set("lockdown", false);
				plugin.getConfig().set("lockdown-reason", "");
			}
			else{
				plugin.getBanManager().lockdown = true;
				plugin.getBanManager().lockdownReason = "Maintenance";
				sender.sendMessage(ChatColor.AQUA + "Lockdown enabled.  Reason: " + ChatColor.RED + "Maintenance" + ChatColor.AQUA + ".");
				
				plugin.getConfig().set("lockdown", true);
				plugin.getConfig().set("lockdown-reason", "Maintenance");
			}
		}
		return true;
	}
}

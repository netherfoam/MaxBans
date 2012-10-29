package org.maxgamer.maxbans.commands;

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
		if(!sender.hasPermission("maxbans.lockdown.use")){
			sender.sendMessage(plugin.color_secondary + "You don't have permission to do that");
			return true;
		}
		if(args.length > 0){
			StringBuilder sb = new StringBuilder();
			for(String s : args){
				sb.append(s + " ");
			}
			sb.deleteCharAt(sb.length() - 1);
			String reason = sb.toString();
			
			plugin.getBanManager().lockdown = true;
			plugin.getBanManager().lockdownReason = reason;
			sender.sendMessage(plugin.color_primary + "Lockdown enabled.  Reason: " + plugin.color_secondary + reason + plugin.color_primary + ".");
			
			plugin.getConfig().set("lockdown", true);
			plugin.getConfig().set("lockdown-reason", reason);
		}
		else{
			if(plugin.getBanManager().lockdown){
				plugin.getBanManager().lockdown = false;
				plugin.getBanManager().lockdownReason = "";
				sender.sendMessage(plugin.color_primary + "Lockdown disabled.");
				
				plugin.getConfig().set("lockdown", false);
				plugin.getConfig().set("lockdown-reason", "");
			}
			else{
				plugin.getBanManager().lockdown = true;
				plugin.getBanManager().lockdownReason = "Maintenance";
				sender.sendMessage(plugin.color_primary + "Lockdown enabled.  Reason: " + plugin.color_secondary + "Maintenance" + plugin.color_primary + ".");
				
				plugin.getConfig().set("lockdown", true);
				plugin.getConfig().set("lockdown-reason", "Maintenance");
			}
		}
		plugin.saveConfig();
		return true;
	}
}

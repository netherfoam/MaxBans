package org.maxgamer.maxbans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.util.Formatter;

public class LockdownCommand implements CommandExecutor{
    private MaxBans plugin;
    public LockdownCommand(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("maxbans.lockdown.use")){
			sender.sendMessage(Formatter.secondary + "You don't have permission to do that");
			return true;
		}
		if(args.length > 0){
			if(args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("enable")){
				StringBuilder sb = new StringBuilder();
				args[0] = "";
				for(String s : args){
					if(s.isEmpty()) continue;
					sb.append(s + " ");
				}
				if(sb.length() > 0){
					sb.deleteCharAt(sb.length() - 1);
				}
				String reason = sb.toString();
				
				if(reason.isEmpty()){
					reason = "Maintenance";
				}
				
				plugin.getBanManager().setLockdown(true, reason);
				sender.sendMessage(Formatter.primary + "Lockdown enabled.  Reason: " + Formatter.secondary + reason + Formatter.primary + ".");
			}
			else if(args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("disable")){
				plugin.getBanManager().setLockdown(false);
				sender.sendMessage(Formatter.primary + "Lockdown disabled.");
			}
			else{
				StringBuilder sb = new StringBuilder();
				for(String s : args){
					if(s.isEmpty()) continue;
					sb.append(s + " ");
				}
				sb.deleteCharAt(sb.length() - 1);
				String reason = sb.toString();
				
				plugin.getBanManager().setLockdown(true, reason);
				sender.sendMessage(Formatter.primary + "Lockdown enabled.  Reason: " + Formatter.secondary + reason + Formatter.primary + ".");
			}
		}
		else{
			if(plugin.getBanManager().isLockdown()){
				plugin.getBanManager().setLockdown(false);
				sender.sendMessage(Formatter.primary + "Lockdown disabled.");
			}
			else{
				plugin.getBanManager().setLockdown(true);
				sender.sendMessage(Formatter.primary + "Lockdown enabled.  Reason: " + Formatter.secondary + "Maintenance" + Formatter.primary + ".");
			}
		}
		return true;
	}
}

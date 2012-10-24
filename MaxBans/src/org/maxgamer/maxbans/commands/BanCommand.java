package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.Ban;
import org.maxgamer.maxbans.banmanager.TempBan;
import org.maxgamer.maxbans.util.Util;

public class BanCommand implements CommandExecutor{
    private MaxBans plugin;
    public BanCommand(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("maxbans.ban")){
			sender.sendMessage(plugin.color_secondary + "You don't have permission to do that");
			return true;
		}
		
		String usage = plugin.color_secondary + "Usage: /ban <player> [-s] <reason>";
		
		if(args.length > 0){
			String name = args[0];
			
			name = plugin.getBanManager().match(name);
			if(name == null){
				name = args[0]; //Use exact name then.
			}
			
			Ban ban = plugin.getBanManager().getBan(name);
			if(ban != null && !(ban instanceof TempBan)){
				sender.sendMessage(plugin.color_secondary + "That player is already banned.");
				return true;
			}
			
			boolean silent = Util.isSilent(args);
			
			//Build reason
			String reason = Util.buildReason(args);
			
			//Build banner
			String banner;
			if(sender instanceof Player){
				banner = ((Player) sender).getName();
			}
			else{
				banner = "Console";
			}
			
			plugin.getBanManager().ban(name, reason, banner);
			
			//Kick them
			Player player = Bukkit.getPlayerExact(name);
			if(player != null && player.isOnline()){
				player.kickPlayer("You have been banned for: \n" + reason);
			}
			
			//Notify online players
			if(!silent){
				plugin.getBanManager().announce(plugin.color_secondary + name + plugin.color_primary + " has been banned by " + plugin.color_secondary + banner + plugin.color_primary + ". Reason: " + plugin.color_secondary + reason + ".");
			}
			else{
				sender.sendMessage(plugin.color_secondary + name + plugin.color_primary + " has been silently banned by " + plugin.color_secondary + banner + plugin.color_primary + ". Reason: " + plugin.color_secondary + reason + ".");
			}
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}

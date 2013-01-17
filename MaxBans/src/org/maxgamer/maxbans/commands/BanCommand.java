package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.Ban;
import org.maxgamer.maxbans.banmanager.IPBan;
import org.maxgamer.maxbans.banmanager.TempBan;
import org.maxgamer.maxbans.banmanager.TempIPBan;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.Util;

public class BanCommand implements CommandExecutor{
    private MaxBans plugin;
    public BanCommand(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("maxbans.ban")){
			sender.sendMessage(Formatter.secondary + "You don't have permission to do that");
			return true;
		}
		
		String usage = Formatter.secondary + "Usage: /ban <player> [-s] <reason>";
		
		if(args.length > 0){
			boolean silent = Util.isSilent(args);
			
			String name = args[0];
			if(name.isEmpty()){
				sender.sendMessage(Formatter.primary + " No name given.");
				return true;
			}
			
			//Build reason
			String reason = Util.buildReason(args);
			String banner = Util.getName(sender);
			
			if(!Util.isIP(name)){
				name = plugin.getBanManager().match(name);
				if(name == null){
					name = args[0]; //Use exact name then.
				}
				
				Ban ban = plugin.getBanManager().getBan(name);
				if(ban != null && !(ban instanceof TempBan)){
					sender.sendMessage(Formatter.secondary + "That player is already banned.");
					return true;
				}
				
				plugin.getBanManager().ban(name, reason, banner);
				
				//Kick them
				Player player = Bukkit.getPlayerExact(name);
				if(player != null){
					player.kickPlayer("You have been permanently banned for: \n" + reason + "\nBy " + banner);
				}
			}
			else{
				IPBan ipban = plugin.getBanManager().getIPBan(name);
				if(ipban != null && !(ipban instanceof TempIPBan)){
					sender.sendMessage(Formatter.secondary + "That IP is already banned.");
					return true;
				}
				
				plugin.getBanManager().ipban(name, reason, banner);
			}
			
			//Notify online players
			if(!silent){
				plugin.getBanManager().announce(Formatter.secondary + name + Formatter.primary + " has been banned by " + Formatter.secondary + banner + Formatter.primary + ". Reason: " + Formatter.secondary + reason);
			}
			else{
				sender.sendMessage(ChatColor.ITALIC + "" + Formatter.secondary + name + Formatter.primary + " has been silently banned by " + Formatter.secondary + banner + Formatter.primary + ". Reason: " + Formatter.secondary + reason);
			}
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}

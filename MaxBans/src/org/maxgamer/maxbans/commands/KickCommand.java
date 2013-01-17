package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.Util;

public class KickCommand implements CommandExecutor{
    private MaxBans plugin;
    public KickCommand(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("maxbans.kick")){
			sender.sendMessage(Formatter.secondary + "You don't have permission to do that");
			return true;
		}
		String usage = Formatter.secondary + "Usage: /kick <player> [-s] [reason]";
		
		if(args.length > 0){
			boolean silent = Util.isSilent(args);
			String name = args[0];
			if(name.isEmpty()){
				sender.sendMessage(Formatter.primary + " No name given.");
				return true;
			}
			String reason = Util.buildReason(args);
			String banner;
			
			if(sender instanceof Player){
				banner = ((Player) sender).getName();
			}
			else{
				banner = "Console";
			}
			
			if(name.equals("*") && sender.hasPermission("maxbans.kick.all")){
				for(Player p : Bukkit.getOnlinePlayers()){
					p.kickPlayer("Kicked by " + banner + " - Reason: \n" + reason);
				}
				
				plugin.getBanManager().announce(Formatter.secondary + banner + Formatter.primary + " has kicked everyone.");
				return true;
			}
			
			Player p = Bukkit.getPlayer(name);
			if(p != null){
				p.kickPlayer("Kicked by " + banner + " - Reason: \n" + reason);
				if(!silent){
					plugin.getBanManager().announce(Formatter.secondary + p.getName() + Formatter.primary + " was kicked by " + Formatter.secondary + banner + Formatter.primary + " for " + Formatter.secondary + reason + Formatter.primary + ".");
				}
				else{
					sender.sendMessage(ChatColor.ITALIC + "" + Formatter.primary + "Kicked " + Formatter.secondary + p.getName() + Formatter.primary + " for " + Formatter.secondary + reason + Formatter.primary + " silently.");
				}
			}
			else{
				sender.sendMessage(Formatter.primary + "No player found: " + Formatter.secondary + name);
			}
			
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}

package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.util.Util;

public class KickCommand implements CommandExecutor{
    private MaxBans plugin;
    public KickCommand(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("maxbans.kick")){
			sender.sendMessage(plugin.color_secondary + "You don't have permission to do that");
			return true;
		}
		String usage = plugin.color_secondary + "Usage: /kick <player> [-s] [reason]";
		
		if(args.length > 0){
			boolean silent = Util.isSilent(args);
			String name = args[0];
			if(name.isEmpty()){
				sender.sendMessage(plugin.color_primary + " No name given.");
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
				
				plugin.getBanManager().announce(plugin.color_secondary + banner + plugin.color_primary + " has kicked everyone.");
				return true;
			}
			
			Player p = Bukkit.getPlayer(name);
			if(p != null){
				p.kickPlayer("Kicked by " + banner + " - Reason: \n" + reason);
				if(!silent){
					plugin.getBanManager().announce(plugin.color_secondary + p.getName() + plugin.color_primary + " was kicked by " + plugin.color_secondary + banner + plugin.color_primary + " for " + plugin.color_secondary + reason + plugin.color_primary + ".");
				}
				else{
					sender.sendMessage(ChatColor.ITALIC + "" + plugin.color_primary + "Kicked " + plugin.color_secondary + p.getName() + plugin.color_primary + " for " + plugin.color_secondary + reason + plugin.color_primary + " silently.");
				}
			}
			else{
				sender.sendMessage(plugin.color_primary + "No player found: " + plugin.color_secondary + name);
			}
			
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}

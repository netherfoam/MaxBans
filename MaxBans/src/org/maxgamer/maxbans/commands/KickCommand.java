package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.Util;

public class KickCommand extends CmdSkeleton{
    public KickCommand(){
        super("maxbans.kick");
        usage = Formatter.secondary + "Usage: /kick <player> [-s] [reason]";
    }
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
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
					p.kickPlayer(Formatter.message + "Kicked by " + Formatter.banner + banner + Formatter.regular + " - Reason: \n'" + Formatter.reason + reason + Formatter.regular + "'");
				}
				
				plugin.getBanManager().announce(Formatter.secondary + banner + Formatter.primary + " has kicked everyone.");
				plugin.getBanManager().addHistory(Formatter.secondary + banner + Formatter.primary + " kicked " + Formatter.secondary + "everyone" + Formatter.primary + " for '" + Formatter.secondary + reason + Formatter.primary + "'");
				return true;
			}
			
			Player p = Bukkit.getPlayer(name);
			if(p != null){
				p.kickPlayer(Formatter.message + "Kicked by " + Formatter.banner + banner + Formatter.regular + " - Reason: \n'" + Formatter.reason + reason + Formatter.regular + "'");
				plugin.getBanManager().announce(Formatter.secondary + p.getName() + Formatter.primary + " was kicked by " + Formatter.secondary + banner + Formatter.primary + " for '" + Formatter.secondary + reason + Formatter.primary + "'.", silent, sender);
				plugin.getBanManager().addHistory(Formatter.secondary + banner + Formatter.primary + " kicked " + Formatter.secondary + p.getName() + Formatter.primary + " for '" + Formatter.secondary + reason + Formatter.primary + "'");
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

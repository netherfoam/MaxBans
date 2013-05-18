package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.Msg;
import org.maxgamer.maxbans.util.Util;

public class KickCommand extends CmdSkeleton{
    public KickCommand(){
        super("kick", "maxbans.kick");
    }
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length > 0){
			boolean silent = Util.isSilent(args);
			String name = args[0];
			if(name.isEmpty()){
				//sender.sendMessage(Formatter.primary + "No name given.");
				sender.sendMessage(Msg.get("error.no-player-given"));
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
					//p.kickPlayer(Formatter.message + "Kicked by " + Formatter.banner + banner + Formatter.regular + " - Reason: \n'" + Formatter.reason + reason + Formatter.regular + "'");
					String message = Msg.get("disconnection.you-were-kicked", new String[]{"banner", "reason"}, new String[]{banner, reason});
					p.kickPlayer(message);
				}
				//plugin.getBanManager().announce(Formatter.secondary + banner + Formatter.primary + " has kicked everyone.");
				//plugin.getBanManager().addHistory(name, banner, Formatter.secondary + banner + Formatter.primary + " kicked " + Formatter.secondary + "everyone" + Formatter.primary + " for '" + Formatter.secondary + reason + Formatter.primary + "'");
				String message = Msg.get("announcement.player-was-kicked", new String[]{"name", "banner", "reason"}, new String[]{"everyone", banner, reason});
				plugin.getBanManager().announce(message, silent, sender);
				plugin.getBanManager().addHistory(name, banner, message);
				return true;
			}
			
			if(Util.isIP(name)){
				for(Player p : Bukkit.getOnlinePlayers()){
					if(p.getAddress().getAddress().getHostAddress().equals(name)){
						//p.kickPlayer(Formatter.message + "Kicked by " + Formatter.banner + banner + Formatter.regular + " - Reason: \n'" + Formatter.reason + reason + Formatter.regular + "'");
						String message = Msg.get("disconnection.you-were-kicked", new String[]{"banner", "reason"}, new String[]{banner, reason});
						p.kickPlayer(message);
					}
				}
				/*
				plugin.getBanManager().announce(Formatter.secondary + banner + Formatter.primary + " has kicked everyone from " + Formatter.secondary + name + Formatter.primary + " for '" + Formatter.secondary + reason + Formatter.primary + "'.");
				plugin.getBanManager().addHistory(name, banner, Formatter.secondary + banner + Formatter.primary + " kicked everyone from " + Formatter.secondary + name + Formatter.primary + " for '" + Formatter.secondary + reason + Formatter.primary + "'");
				*/
				String message = Msg.get("announcement.player-was-kicked", new String[]{"name", "banner", "reason"}, new String[]{name, banner, reason});
				plugin.getBanManager().announce(message, silent, sender);
				plugin.getBanManager().addHistory(name, banner, message);
				return true;
			}
			
			Player p = Bukkit.getPlayer(name);
			if(p != null){
				//p.kickPlayer(Formatter.message + "Kicked by " + Formatter.banner + banner + Formatter.regular + " - Reason: \n'" + Formatter.reason + reason + Formatter.regular + "'");
				String message = Msg.get("disconnection.you-were-kicked", new String[]{"banner", "reason"}, new String[]{banner, reason});
				p.kickPlayer(message);
				message = Msg.get("announcement.player-was-kicked", new String[]{"name", "banner", "reason"}, new String[]{name, banner, reason});
				/*
				plugin.getBanManager().announce(Formatter.secondary + p.getName() + Formatter.primary + " was kicked by " + Formatter.secondary + banner + Formatter.primary + " for '" + Formatter.secondary + reason + Formatter.primary + "'.", silent, sender);
				plugin.getBanManager().addHistory(name, banner, Formatter.secondary + banner + Formatter.primary + " kicked " + Formatter.secondary + p.getName() + Formatter.primary + " for '" + Formatter.secondary + reason + Formatter.primary + "'");*/
				plugin.getBanManager().announce(message, silent, sender);
				plugin.getBanManager().addHistory(name, banner, message);
			}
			else{
				//sender.sendMessage(Formatter.primary + "No player found: " + Formatter.secondary + name);
				String message = Msg.get("error.unknown-player", new String[]{"name"}, new String[]{name});
				sender.sendMessage(message);
			}
			
			return true;
		}
		else{
			sender.sendMessage(getUsage());
			return true;
		}
	}
}

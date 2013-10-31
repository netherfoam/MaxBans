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
					String message = Msg.get("disconnection.you-were-kicked", new String[]{"banner", "reason"}, new String[]{banner, reason});
					plugin.getBanManager().kick(p.getName(), message);
				}
				String message = Msg.get("announcement.player-was-kicked", new String[]{"name", "banner", "reason"}, new String[]{"everyone", banner, reason});
				plugin.getBanManager().announce(message, silent, sender);
				plugin.getBanManager().addHistory(name, banner, message);
				return true;
			}
			
			if(Util.isIP(name)){
				String message = Msg.get("disconnection.you-were-kicked", new String[]{"banner", "reason"}, new String[]{banner, reason});
				plugin.getBanManager().kickIP(name, message);
				
				message = Msg.get("announcement.player-was-kicked", new String[]{"name", "banner", "reason"}, new String[]{name, banner, reason});
				plugin.getBanManager().announce(message, silent, sender);
				plugin.getBanManager().addHistory(name, banner, message);
				return true;
			}
			
			Player p = Bukkit.getPlayer(name);
			if(p != null){
				name = p.getName().toLowerCase();
				String message = Msg.get("disconnection.you-were-kicked", new String[]{"banner", "reason"}, new String[]{banner, reason});
				plugin.getBanManager().kick(name, message);
				message = Msg.get("announcement.player-was-kicked", new String[]{"name", "banner", "reason"}, new String[]{name, banner, reason});
				plugin.getBanManager().announce(message, silent, sender);
				plugin.getBanManager().addHistory(name, banner, message);
			}
			else{
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

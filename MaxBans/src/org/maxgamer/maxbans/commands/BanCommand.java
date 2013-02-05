package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.banmanager.Ban;
import org.maxgamer.maxbans.banmanager.IPBan;
import org.maxgamer.maxbans.banmanager.TempBan;
import org.maxgamer.maxbans.banmanager.TempIPBan;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.Util;

public class BanCommand extends CmdSkeleton{
    public BanCommand(){
        super("maxbans.ban");
        usage = Formatter.secondary + "Usage: /ban <player> [-s] <reason>";
    }
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
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
					player.kickPlayer(Formatter.message + "You have been permanently banned for: \n" + Formatter.reason + reason + Formatter.regular + "\nBy " + Formatter.banner + banner);
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
			
			plugin.getBanManager().announce(Formatter.secondary + name + Formatter.primary + " has been banned by " + Formatter.secondary + banner + Formatter.primary + ". Reason: " + Formatter.secondary + reason, silent, sender);
			plugin.getBanManager().addHistory(Formatter.secondary + banner + Formatter.primary + " banned " + Formatter.secondary + name + Formatter.primary + " for " + Formatter.secondary + reason);
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}

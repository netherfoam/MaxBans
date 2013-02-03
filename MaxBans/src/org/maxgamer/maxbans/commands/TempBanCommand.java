package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
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

public class TempBanCommand implements CommandExecutor{
    private MaxBans plugin;
    public TempBanCommand(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("maxbans.tempban")){
			sender.sendMessage(Formatter.secondary + "You don't have permission to do that");
			return true;
		}
		String usage = Formatter.secondary + "Usage: /tempban <player> <time> <time form> [-s] <reason>";
		
		if(args.length < 3){
			sender.sendMessage(usage);
			return true;
		}
		else{
			boolean silent = Util.isSilent(args);
			String name = args[0];
			if(name.isEmpty()){
				sender.sendMessage(Formatter.primary + " No name given.");
				return true;
			}
			
			long expires = Util.getTime(args);
			if(expires <= 0){
				sender.sendMessage(usage);
				return true;
			}
			expires += System.currentTimeMillis();
			
			String reason = Util.buildReason(args);
			String banner = Util.getName(sender);
			
			if(Util.isIP(name) == false){
				name = plugin.getBanManager().match(name);
				if(name == null){
					name = args[0]; //Use exact name then.
				}
				
				Ban ban = plugin.getBanManager().getBan(name);
				
				if(ban != null){
					if(ban instanceof TempBan){
						//They're already tempbanned!
						
						TempBan tBan = (TempBan) ban;
						if(tBan.getExpires() > expires){
							//Their old ban lasts longer than this one!
							sender.sendMessage(Formatter.secondary + "That player has a tempban which will last longer than the one you supplied!");
							return true;
						}
						else{
							//Increasing a previous ban, remove the old one first.
							plugin.getBanManager().unban(name);
						}
					}
					else{
						//Already perma banned
						sender.sendMessage(Formatter.secondary + "That player is already banned.");
						return true;
					}
				}
				
				plugin.getBanManager().tempban(name, reason, banner, expires);
				Player player = Bukkit.getPlayerExact(name);
				if(player != null && player.isOnline()){
					player.kickPlayer(Formatter.message + "You have been Temporarily Banned for: \n" + Formatter.reason + reason + Formatter.regular + "\nBy " + Formatter.banner + banner + Formatter.regular + ". Expires in " + Formatter.time + Util.getTimeUntil(expires));
				}
			}
			else{
				String ip = name; //Readability
				IPBan ipban = plugin.getBanManager().getIPBan(ip);
				if(ipban != null){
					if(ipban instanceof TempIPBan){
						TempIPBan tiBan = (TempIPBan) ipban;
						if(tiBan.getExpires() > expires){
							//Their old ban lasts longer than this one!
							sender.sendMessage(Formatter.secondary + "That IP has a tempban which will last longer than the one you supplied!");
							return true;
						}
						else{
							//Increasing a previous ban, remove the old one first.
							plugin.getBanManager().unbanip(ip);
						}
					}
				}
				plugin.getBanManager().tempipban(ip, reason, banner, expires);
			}
			
			
			plugin.getBanManager().announce(Formatter.secondary + name + Formatter.primary + " has been temp banned ("+Util.getTimeUntil(expires)+") by " + Formatter.secondary + banner + Formatter.primary + ". Reason: " + Formatter.secondary + reason + ".", silent, sender);
			plugin.getBanManager().addHistory(Formatter.secondary + banner + Formatter.primary + " tempbanned " + Formatter.secondary + name + Formatter.primary + " for " + Formatter.secondary + Util.getTimeUntil(expires) + Formatter.primary + " for " + Formatter.secondary + reason);
			return true;
		}
	}
}

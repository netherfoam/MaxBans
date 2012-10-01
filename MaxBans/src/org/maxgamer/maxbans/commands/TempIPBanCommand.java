package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.IPBan;
import org.maxgamer.maxbans.banmanager.TempIPBan;

public class TempIPBanCommand implements CommandExecutor{
    private MaxBans plugin;
    public TempIPBanCommand(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String usage = ChatColor.RED + "Usage: /ipban <player> <time> <timeform> [-s] <reason>";
		
		if(args.length > 0){
			String name = args[0];
			
			//Get expirey time
			long time = plugin.getBanManager().getTime(args);
			if(time <= 0){
				sender.sendMessage(usage);
				return true;
			}
			time += System.currentTimeMillis();
			
			//Fetch their IP address from history
			String ip = plugin.getBanManager().getIP(name);
			
			if(ip == null){
				sender.sendMessage(ChatColor.RED + "No IP recorded for " + name + " - Try ban them normally instead?");
				return true;
			}
			
			IPBan ban = plugin.getBanManager().getIPBan(ip);
			if(ban != null){
				if(ban instanceof TempIPBan){
					//They're already tempbanned!
					
					TempIPBan tBan = (TempIPBan) ban;
					if(tBan.getExpires() > time){
						//Their old ban lasts longer than this one!
						sender.sendMessage(ChatColor.RED + "That player has a tempban which will last longer than the one you supplied!");
						return true;
					}
					else{
						//Increasing a previous ban, remove the old one first.
						plugin.getBanManager().unbanip(ip);
					}
				}
				else{
					//Already perma banned
					sender.sendMessage(ChatColor.RED + "That player is already banned.");
					return true;
				}
			}
			
			boolean silent = plugin.getBanManager().isSilent(args);
			
			//Build the reason
			String reason = plugin.getBanManager().buildReason(args);
		
			String banner;			
			//Get the banners name
			if(sender instanceof Player){
				banner = ((Player) sender).getName();
			}
			else{
				banner = "Console";
			}
			
			
			//Ban them
			plugin.getBanManager().tempipban(ip, reason, banner, time);
			
			//Kick them
			Player player = Bukkit.getPlayer(name);
			if(player != null && player.isOnline()){
				player.kickPlayer("You have been Temporarily IP Banned for: \n"+reason);
			}
			
			//Notify online players
			if(!silent){
				plugin.getBanManager().announce(ChatColor.RED + name + ChatColor.AQUA + " has been temp IP banned ("+plugin.getBanManager().getTimeUntil(time)+") by " + ChatColor.RED + banner + ChatColor.AQUA + ". Reason: " + ChatColor.RED + reason + ".");
			}
			
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}

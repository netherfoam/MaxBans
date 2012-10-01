package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.IPBan;
import org.maxgamer.maxbans.banmanager.TempIPBan;

public class IPBanCommand implements CommandExecutor{
    private MaxBans plugin;
    public IPBanCommand(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("maxbans.ipban")){
			sender.sendMessage(plugin.color_secondary + "You don't have permission to do that");
			return true;
		}
		String usage = plugin.color_secondary + "Usage: /ipban <player> [-s] <reason>";
		
		if(args.length > 0){
			String name = args[0];
			
			name = plugin.getBanManager().match(name);
			if(name == null){
				name = args[0]; //Use exact name then.
			}
			
			//Fetch their IP address from history
			String ip = plugin.getBanManager().getIP(name);
			
			if(ip == null){
				sender.sendMessage(plugin.color_secondary + "No IP recorded for " + name + " - Try ban them normally instead?");
				return true;
			}
			
			IPBan ban = plugin.getBanManager().getIPBan(ip);
			if(ban != null && !(ban instanceof TempIPBan)){
				sender.sendMessage(plugin.color_secondary + "That player is already IP banned.");
				return true;
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
			plugin.getBanManager().ipban(ip, reason, banner);
			
			//Kick them
			Player player = Bukkit.getPlayerExact(name);
			if(player != null && player.isOnline()){
				player.kickPlayer("You have been IP Banned for: \n" + reason);
			}
			
			//Notify online players
			if(!silent){
				plugin.getBanManager().announce(plugin.color_secondary + name + plugin.color_primary + " has been banned by " + plugin.color_secondary + banner + plugin.color_primary + ". Reason: " + plugin.color_secondary + reason + ".");
			}
			
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}

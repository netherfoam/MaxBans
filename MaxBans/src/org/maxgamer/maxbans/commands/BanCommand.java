package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.Ban;
import org.maxgamer.maxbans.banmanager.TempBan;

public class BanCommand implements CommandExecutor{
    private MaxBans plugin;
    public BanCommand(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String usage = ChatColor.RED + "Usage: /ban <player> [-s] <reason>";
		
		if(args.length > 0){
			String name = args[0];
			
			Ban ban = plugin.getBanManager().getBan(name);
			if(ban != null && !(ban instanceof TempBan)){
				sender.sendMessage(ChatColor.RED + "That player is already banned.");
				return true;
			}
			
			boolean silent = plugin.getBanManager().isSilent(args);
			
			//Build reason
			String reason = plugin.getBanManager().buildReason(args);
			
			//Build banner
			String banner;
			if(sender instanceof Player){
				banner = ((Player) sender).getName();
			}
			else{
				banner = "Console";
			}
			
			plugin.getBanManager().ban(name, reason, banner);
			
			//Kick them
			Player player = Bukkit.getPlayer(name);
			if(player != null && player.isOnline()){
				player.kickPlayer("You have been banned for: \n" + reason);
			}
			
			//Notify online players
			if(!silent){
				for(Player p : Bukkit.getOnlinePlayers()){
					p.sendMessage(ChatColor.RED + name + " has been banned (forever) by " + banner + ". reason: " + reason);
				}
			}
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}

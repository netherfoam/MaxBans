package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.MaxBans;

public class KickCommand implements CommandExecutor{
    private MaxBans plugin;
    public KickCommand(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("maxbans.kick")){
			sender.sendMessage(ChatColor.RED + "You don't have permission to do that");
			return true;
		}
		String usage = ChatColor.RED + "Usage: /kick <player> [reason]";
		
		if(args.length > 0){
			String name = args[0];
			String reason = plugin.getBanManager().buildReason(args);
			boolean silent = plugin.getBanManager().isSilent(args);
			
			String banner;
			if(sender instanceof Player){
				banner = ((Player) sender).getName();
			}
			else{
				banner = "Console";
			}
			
			Player p = Bukkit.getPlayer(name);
			if(p != null){
				p.kickPlayer("Kicked by " + banner + " - Reason: \n" + reason);
				if(!silent){
					plugin.getBanManager().announce(ChatColor.RED + p.getName() + ChatColor.AQUA + " was kicked by " + ChatColor.RED + banner + ChatColor.AQUA + " for " + ChatColor.RED + reason + ChatColor.AQUA + ".");
				}
				else{
					sender.sendMessage(ChatColor.AQUA + "Kicked " + ChatColor.RED + p.getName() + ChatColor.AQUA + " for " + ChatColor.RED + reason + ChatColor.AQUA + "silently.");
				}
			}
			else{
				sender.sendMessage(ChatColor.AQUA + "No player found: " + ChatColor.RED + name);
			}
			
			
			
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}

package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
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
			sender.sendMessage(plugin.color_secondary + "You don't have permission to do that");
			return true;
		}
		String usage = plugin.color_secondary + "Usage: /kick <player> [reason]";
		
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
			
			if(name.equals("*") && sender.hasPermission("maxbans.kick.all")){
				for(Player p : Bukkit.getOnlinePlayers()){
					p.kickPlayer("Kicked by " + banner + " - Reason: \n" + reason);
				}
				return true;
			}
			
			Player p = Bukkit.getPlayer(name);
			if(p != null){
				p.kickPlayer("Kicked by " + banner + " - Reason: \n" + reason);
				if(!silent){
					plugin.getBanManager().announce(plugin.color_secondary + p.getName() + plugin.color_primary + " was kicked by " + plugin.color_secondary + banner + plugin.color_primary + " for " + plugin.color_secondary + reason + plugin.color_primary + ".");
				}
				else{
					sender.sendMessage(plugin.color_primary + "Kicked " + plugin.color_secondary + p.getName() + plugin.color_primary + " for " + plugin.color_secondary + reason + plugin.color_primary + "silently.");
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

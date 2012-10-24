package org.maxgamer.maxbans.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.Ban;
import org.maxgamer.maxbans.banmanager.IPBan;
import org.maxgamer.maxbans.util.Util;

public class UnbanCommand implements CommandExecutor{
    private MaxBans plugin;
    public UnbanCommand(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("maxbans.unban")){
			sender.sendMessage(plugin.color_secondary + "You don't have permission to do that");
			return true;
		}
		String usage = plugin.color_secondary + "Usage: /unban <player>";
		
		if(args.length > 0){
			String name = args[0];
			
			Ban ban = null;
			IPBan ipBan = null;
			
			if(!Util.isIP(name)){
				name = plugin.getBanManager().match(name, true);
				ban = plugin.getBanManager().getBan(name);
				ipBan = plugin.getBanManager().getIPBan(plugin.getBanManager().getIP(name));
			}
			else{
				ipBan = plugin.getBanManager().getIPBan(name);
			}
			
			String banner;

			if(sender instanceof Player){
				banner = ((Player) sender).getName();
			}
			else{
				banner = "Console";
			}
			
			if(ban != null || ipBan != null){
				plugin.getBanManager().unban(name);
				plugin.getBanManager().unbanip(name);
				sender.sendMessage(ChatColor.GREEN + "Unbanned " + name);
			}
			else{
				sender.sendMessage(plugin.color_primary + "Could not find a ban for " + plugin.color_secondary + name + plugin.color_primary + ".");
				return true;
			}
			
			plugin.getBanManager().announce(plugin.color_secondary + name + plugin.color_primary + " has been unbanned by " + plugin.color_secondary + banner + plugin.color_primary + ".");
			
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}

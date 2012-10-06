package org.maxgamer.maxbans.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.Ban;
import org.maxgamer.maxbans.banmanager.IPBan;

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
			
			name = plugin.getBanManager().match(name, true);
			
			String banner;

			if(sender instanceof Player){
				banner = ((Player) sender).getName();
			}
			else{
				banner = "Console";
			}
			
			Ban ban = plugin.getBanManager().getBan(name);
			IPBan ipBan = plugin.getBanManager().getIPBan(plugin.getBanManager().getIP(name));
			if(ban != null || ipBan != null){
				plugin.getBanManager().unban(name);
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

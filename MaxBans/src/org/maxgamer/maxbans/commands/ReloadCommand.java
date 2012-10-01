package org.maxgamer.maxbans.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.MaxBans;

public class ReloadCommand implements CommandExecutor{
    private MaxBans plugin;
    public ReloadCommand(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("maxbans.reload")){
			sender.sendMessage(plugin.color_secondary + "You don't have permission to do that");
			return true;
		}
		
		sender.sendMessage(plugin.color_secondary + "Reloading MaxBans");
		plugin.getBanManager().reload();
		sender.sendMessage(ChatColor.GREEN + "Reload Complete");
		return true;
	}
}

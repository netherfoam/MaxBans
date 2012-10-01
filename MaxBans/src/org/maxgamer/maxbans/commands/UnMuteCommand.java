package org.maxgamer.maxbans.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.Mute;

public class UnMuteCommand implements CommandExecutor{
    private MaxBans plugin;
    public UnMuteCommand(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String usage = ChatColor.RED + "Usage: /mute <player> <reason>";
		
		if(args.length > 0){
			String name = args[0];
			
			Mute mute = plugin.getBanManager().getMute(name);
			if(mute != null){
				plugin.getBanManager().unmute(name);
				sender.sendMessage(ChatColor.GREEN + "Unmuted " + name);
				return true;
			}
			else{
				sender.sendMessage(ChatColor.GREEN + name + " is not muted.");
				return true;
			}
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}

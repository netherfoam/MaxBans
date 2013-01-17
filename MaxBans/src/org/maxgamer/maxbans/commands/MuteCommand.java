package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.Mute;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.Util;

public class MuteCommand implements CommandExecutor{
    private MaxBans plugin;
    public MuteCommand(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("maxbans.mute")){
			sender.sendMessage(Formatter.secondary + "You don't have permission to do that");
			return true;
		}
		String usage = Formatter.secondary + "Usage: /mute <player>";
		
		if(args.length > 0){
			String name = args[0];
			
			name = plugin.getBanManager().match(name);
			if(name == null){
				name = args[0]; //Use exact name then.
			}
			
			Mute mute = plugin.getBanManager().getMute(name);
			if(mute != null){
				plugin.getBanManager().unmute(name);
				sender.sendMessage(ChatColor.GREEN + "Unmuted " + name);
				return true;
			}
			
			String banner = Util.getName(sender);
			
			plugin.getBanManager().mute(name, banner);
			
			Player p = Bukkit.getPlayerExact(name);
			if(p != null){
				p.sendMessage(Formatter.secondary + " You have been muted.");
			}
			sender.sendMessage(Formatter.primary + "Muted " + Formatter.secondary + name);
			
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}

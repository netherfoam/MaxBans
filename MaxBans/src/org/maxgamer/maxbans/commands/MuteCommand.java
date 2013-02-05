package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.banmanager.Mute;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.Util;

public class MuteCommand extends CmdSkeleton{
    public MuteCommand(){
        super("maxbans.mute");
        usage = Formatter.secondary + "Usage: /mute <player>";
    }
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
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
			plugin.getBanManager().addHistory(Formatter.secondary + banner + Formatter.primary + " muted " + Formatter.secondary + name);
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}

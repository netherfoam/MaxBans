package org.maxgamer.maxbans.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.banmanager.Mute;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.Util;

public class UnMuteCommand extends CmdSkeleton{
    public UnMuteCommand(){
        super("maxbans.unmute");
        usage = Formatter.secondary + "Usage: /unmute <player>";
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
				plugin.getBanManager().addHistory(Formatter.secondary + Util.getName(sender) + Formatter.primary + " unmuted " + Formatter.secondary + name);
			}
			else{
				sender.sendMessage(ChatColor.GREEN + name + " is not muted.");
			}
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}

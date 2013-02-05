package org.maxgamer.maxbans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.Util;

public class WarnCommand extends CmdSkeleton{
    public WarnCommand(){
        super("maxbans.warn");
        usage = Formatter.secondary + "Usage: /warn <player> <reason>";
    }
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length > 1){
			String name = args[0];
			
			name = plugin.getBanManager().match(name);
			if(name == null){
				name = args[0]; //Use exact name then.
			}
			
			String reason = Util.buildReason(args);
			String banner = Util.getName(sender);
			
			plugin.getBanManager().announce(Formatter.secondary + name + Formatter.primary + " has been warned for " + Formatter.secondary + reason + Formatter.primary + " by " + Formatter.secondary + banner + Formatter.primary + ".");
			plugin.getBanManager().warn(name, reason, banner);
			plugin.getBanManager().addHistory(Formatter.secondary + banner + Formatter.primary + " warned " + Formatter.secondary + name + Formatter.primary + " for " + Formatter.secondary + reason);
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}

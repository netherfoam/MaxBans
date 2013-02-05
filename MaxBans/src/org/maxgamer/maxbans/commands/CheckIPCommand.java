package org.maxgamer.maxbans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.util.Formatter;

public class CheckIPCommand extends CmdSkeleton{
    public CheckIPCommand(){
        super("maxbans.checkip");
        usage = Formatter.secondary + "Usage: /checkip <player>";
    }
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length > 0){
			String name = args[0];
			
			name = plugin.getBanManager().match(name);
			if(name == null){
				name = args[0]; //Use exact name then.
			}
			
			String ip = plugin.getBanManager().getIP(name);
			
			if(ip == null){
				sender.sendMessage(Formatter.primary + "Player " + Formatter.secondary + name + Formatter.primary + " has no recorded IP history.");
			}
			else{
				sender.sendMessage(Formatter.primary + "Player " + Formatter.secondary + name + Formatter.primary + " last used the IP " + Formatter.secondary + ip);
			}
			
			
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}

package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.sync.Packet;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.Util;

public class WarnCommand extends CmdSkeleton{
    public WarnCommand(){
        super("warn", "maxbans.warn");
    }
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length > 1){
			boolean silent = Util.isSilent(args);
			
			String name = args[0];
			
			name = plugin.getBanManager().match(name);
			if(name == null){
				name = args[0]; //Use exact name then.
			}
			
			String reason = Util.buildReason(args);
			String banner = Util.getName(sender);
			
			String msg = Formatter.secondary + name + Formatter.primary + " has been warned for '" + Formatter.secondary + reason + Formatter.primary + "' by " + Formatter.secondary + banner + Formatter.primary + ".";
			plugin.getBanManager().announce(msg, silent, sender);
			if(silent){
				Player targ = Bukkit.getPlayerExact(name);
				if(targ != null) targ.sendMessage(msg);
			}
			
			plugin.getBanManager().warn(name, reason, banner);
			
			String message = Formatter.secondary + banner + Formatter.primary + " warned " + Formatter.secondary + name + Formatter.primary + " for '" + Formatter.secondary + reason + Formatter.primary + "'";
			plugin.getBanManager().addHistory(name, banner, message);
			
	    	if(plugin.getSyncer() != null){
	    		Packet prop = new Packet();
	    		prop.setCommand("warn");
	    		prop.put("name", name);
	    		prop.put("banner", banner);
	    		prop.put("reason", reason);
	    		plugin.getSyncer().broadcast(prop);
	    		
	    		//Send the addhistory request.
	    		Packet history = new Packet().setCommand("addhistory").put("string", message).put("banner", banner).put("name", name);
	    		plugin.getSyncer().broadcast(history);
	    	}
	    	
			return true;
		}
		else{
			sender.sendMessage(getUsage());
			return true;
		}
	}
}

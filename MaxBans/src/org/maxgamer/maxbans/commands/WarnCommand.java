package org.maxgamer.maxbans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.Msg;
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
			
			if(name.isEmpty()){
				sender.sendMessage(Msg.get("error.no-player-given"));
				return true;
			}
			
			String reason = Util.buildReason(args);
			String banner = Util.getName(sender);
			
			plugin.getBanManager().warn(name, reason, banner);
			String msg = Msg.get("announcement.player-was-warned", new String[]{"banner", "name", "reason"}, new String[]{banner, name, reason});
			plugin.getBanManager().announce(msg, silent, sender);
			plugin.getBanManager().addHistory(name, banner, msg);
	    	
			return true;
		}
		else{
			sender.sendMessage(getUsage());
			return true;
		}
	}
}

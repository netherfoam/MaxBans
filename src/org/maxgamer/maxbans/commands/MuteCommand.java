package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.Msg;
import org.maxgamer.maxbans.banmanager.Mute;
import org.maxgamer.maxbans.sync.Packet;
import org.maxgamer.maxbans.util.Util;

public class MuteCommand extends CmdSkeleton{
    public MuteCommand(){
        super("mute", "maxbans.mute");
    }
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length > 0){
			boolean silent = Util.isSilent(args);
			String name = args[0];
			if(name.isEmpty()){
				sender.sendMessage(Msg.get("error.no-player-given"));
				return true;
			}
			
			name = plugin.getBanManager().match(name);
			if(name == null){
				name = args[0]; //Use exact name then.
			}
			
			Mute mute = plugin.getBanManager().getMute(name);
			if(mute != null){
				Bukkit.dispatchCommand(sender, "unmute");
				return true;
			}
			
			String reason = Util.buildReason(args);
			
			String banner = Util.getName(sender);
			
			plugin.getBanManager().mute(name, banner, reason);
			
			
			String message = Msg.get("announcement.player-was-muted", new String[]{"banner", "name", "reason"}, new String[]{banner, name, reason});
			
			plugin.getBanManager().announce(message, silent, sender);
			plugin.getBanManager().addHistory(name, banner, message);
			
	    	if(plugin.getSyncer() != null){
	    		Packet prop = new Packet();
	    		prop.setCommand("mute");
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

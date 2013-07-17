package org.maxgamer.maxbans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.Msg;
import org.maxgamer.maxbans.banmanager.Ban;
import org.maxgamer.maxbans.banmanager.IPBan;
import org.maxgamer.maxbans.banmanager.TempBan;
import org.maxgamer.maxbans.banmanager.TempIPBan;
import org.maxgamer.maxbans.sync.Packet;
import org.maxgamer.maxbans.util.Util;

public class BanCommand extends CmdSkeleton{
    public BanCommand(){
        super("ban", "maxbans.ban");
    }
    
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length > 0){
			boolean silent = Util.isSilent(args);
			
			String name = args[0];
			if(name.isEmpty()){
				sender.sendMessage(Msg.get("error.no-player-given"));
				return true;
			}
			
			//Build reason
			String reason = Util.buildReason(args);
			String banner = Util.getName(sender);
			//The message we will log in history, send to online players, and send across the sync server (if applicable)
			String message = Msg.get("announcement.player-was-banned", new String[]{"banner", "name", "reason"}, new String[]{banner, name, reason});
			Packet request = new Packet();
			
			if(!Util.isIP(name)){
				//They supplied us with a username
				name = plugin.getBanManager().match(name);
				if(name == null){
					name = args[0]; //Use exact name then.
				}
				
				Ban ban = plugin.getBanManager().getBan(name);
				if(ban != null && !(ban instanceof TempBan)){
					sender.sendMessage(Msg.get("error.player-already-banned"));
					return true;
				}
				
				plugin.getBanManager().ban(name, reason, banner);
				//Request to ban the username if required.
				if(plugin.getSyncer() != null){
					request.setCommand("ban").put("name", name).put("reason", reason).put("banner", banner);
		    	}
			}
			else{
				IPBan ipban = plugin.getBanManager().getIPBan(name);
				if(ipban != null && !(ipban instanceof TempIPBan)){
					sender.sendMessage(Msg.get("error.ip-already-banned"));
					return true;
				}
				
				plugin.getBanManager().ipban(name, reason, banner);
				//Request to ban the IP if required
				if(plugin.getSyncer() != null){
					request.setCommand("ipban").put("ip", name).put("reason", reason).put("banner", banner);
		    	}
			}
			plugin.getBanManager().announce(message, silent, sender);
			plugin.getBanManager().addHistory(name, banner, message);
			
			if(plugin.getSyncer() != null){
				plugin.getSyncer().broadcast(request);
	    		Packet history = new Packet().setCommand("addhistory").put("string", message).put("banner", banner).put("name", name);
	    		plugin.getSyncer().broadcast(history);
	    		Packet msg = new Packet().setCommand("announce").put("string", message).put("silent", silent);
	    		plugin.getSyncer().broadcast(msg);
			}
	    	
			return true;
		}
		else{
			sender.sendMessage(getUsage());
			return true;
		}
	}
}

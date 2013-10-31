package org.maxgamer.maxbans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.Msg;
import org.maxgamer.maxbans.banmanager.IPBan;
import org.maxgamer.maxbans.banmanager.TempIPBan;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.Util;

public class IPBanCommand extends CmdSkeleton{
    public IPBanCommand(){
        super("ipban", "maxbans.ipban");
    }
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		boolean silent = Util.isSilent(args);
		
		//Build the reason
		String reason = Util.buildReason(args);
		String banner = Util.getName(sender);
		
		if(args.length > 0){
			String ip;
			String name = args[0];
			if(name.isEmpty()){
				sender.sendMessage(Msg.get("error.no-player-given"));
				return true;
			}
			if(!Util.isIP(name)){
				name = plugin.getBanManager().match(name);
				if(name == null){
					name = args[0]; //Use exact name then.
				}
				
				//Fetch their IP address from history
				ip = plugin.getBanManager().getIP(name);
				
				if(ip == null){
					sender.sendMessage(Formatter.secondary + "No IP recorded for " + name + " - Try ban them normally instead?");
					return true;
				}
				
				plugin.getBanManager().ban(name, reason, banner); //User
			}
			else{
				ip = name;
			}
			
			IPBan ban = plugin.getBanManager().getIPBan(ip);
			if(ban != null && !(ban instanceof TempIPBan)){
				sender.sendMessage(Msg.get("error.ip-already-banned"));
				return true;
			}
			
			//Ban them
			plugin.getBanManager().ipban(ip, reason, banner); //IP
			
			String message = Msg.get("announcement.player-was-ip-banned", new String[]{"banner", "name", "reason", "ip"}, new String[]{banner, name, reason, ip});
			plugin.getBanManager().announce(message, silent, sender);
			plugin.getBanManager().addHistory(name, banner, message);
			
			return true;
		}
		else{
			sender.sendMessage(getUsage());
			return true;
		}
	}
}

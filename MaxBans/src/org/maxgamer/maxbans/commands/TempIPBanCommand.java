package org.maxgamer.maxbans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.Msg;
import org.maxgamer.maxbans.banmanager.IPBan;
import org.maxgamer.maxbans.banmanager.TempIPBan;
import org.maxgamer.maxbans.util.Util;

public class TempIPBanCommand extends CmdSkeleton{
    public TempIPBanCommand(){
        super("tempipban", "maxbans.tempipban");
    }
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length > 2){
			boolean silent = Util.isSilent(args);
			String name = args[0];
			if(name.isEmpty()){
				//sender.sendMessage(Formatter.primary + " No name given.");
				sender.sendMessage(Msg.get("error.no-player-given"));
				return true;
			}
			
			//Get expirey time
			long time = Util.getTime(args);
			if(time <= 0){
				sender.sendMessage(getUsage());
				return true;
			}
			time += System.currentTimeMillis();
			
			//Build the reason
			String reason = Util.buildReason(args);
			String banner = Util.getName(sender);
			
			String ip;
			if(!Util.isIP(name)){
				name = plugin.getBanManager().match(name);
				if(name == null){
					name = args[0]; //Use exact name then.
				}
				//Fetch their IP address from history
				ip = plugin.getBanManager().getIP(name);
				
				if(ip == null){
					String msg = Msg.get("error.no-ip-known");
					sender.sendMessage(msg);
					return true;
				}
				
				plugin.getBanManager().tempban(name, reason, banner, time); //User
			}
			else{
				ip = name;
			}
			
			//Make sure the ban isnt redundant
			IPBan ban = plugin.getBanManager().getIPBan(ip);
			if(ban != null){
				if(ban instanceof TempIPBan){
					//They're already tempbanned!
					
					TempIPBan tBan = (TempIPBan) ban;
					if(tBan.getExpires() > time){
						//Their old ban lasts longer than this one!
						String msg = Msg.get("error.tempipban-shorter-than-last");
						sender.sendMessage(msg);
						return true;
					}
					else{
						//Increasing a previous ban, remove the old one first.
						plugin.getBanManager().unbanip(ip);
					}
				}
				else{
					//Already perma banned
					//sender.sendMessage(Formatter.secondary + "That player is already banned.");
					String msg = Msg.get("error.tempipban-shorter-than-last");
					sender.sendMessage(msg);
					return true;
				}
			}
			
			//Ban them
			plugin.getBanManager().tempipban(ip, reason, banner, time); //IP
			String message = Msg.get("announcement.player-was-tempipbanned", new String[]{"banner", "name", "reason", "ip", "time"}, new String[]{banner, name, reason, ip, Util.getTimeUntil(time)});
			plugin.getBanManager().announce(message, silent, sender);
			
			return true;
		}
		else{
			sender.sendMessage(getUsage());
			return true;
		}
	}
}

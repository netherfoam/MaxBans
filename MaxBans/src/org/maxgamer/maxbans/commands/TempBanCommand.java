package org.maxgamer.maxbans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.Msg;
import org.maxgamer.maxbans.banmanager.Ban;
import org.maxgamer.maxbans.banmanager.IPBan;
import org.maxgamer.maxbans.banmanager.TempBan;
import org.maxgamer.maxbans.banmanager.TempIPBan;
import org.maxgamer.maxbans.util.Util;

public class TempBanCommand extends CmdSkeleton{
    public TempBanCommand(){
        super("tempban", "maxbans.tempban");
    }
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length < 3){
			sender.sendMessage(getUsage());
			return true;
		}
		else{
			boolean silent = Util.isSilent(args);
			String name = args[0];
			if(name.isEmpty()){
				//sender.sendMessage(Formatter.primary + " No name given.");
				sender.sendMessage(Msg.get("error.no-player-given"));
				return true;
			}
			
			long expires = Util.getTime(args);
			if(expires <= 0){
				sender.sendMessage(getUsage());
				return true;
			}
			expires += System.currentTimeMillis();
			
			String reason = Util.buildReason(args);
			String banner = Util.getName(sender);
			
			if(Util.isIP(name) == false){
				name = plugin.getBanManager().match(name);
				if(name == null){
					name = args[0]; //Use exact name then.
				}
				
				Ban ban = plugin.getBanManager().getBan(name);
				
				if(ban != null){
					if(ban instanceof TempBan){
						//They're already tempbanned!
						
						TempBan tBan = (TempBan) ban;
						if(tBan.getExpires() > expires){
							//Their old ban lasts longer than this one!
							String msg = Msg.get("error.tempban-shorter-than-last");
							sender.sendMessage(msg);
							return true;
						}
						else{
							//Increasing a previous ban, remove the old one first.
							plugin.getBanManager().unban(name);
						}
					}
					else{
						//Already perma banned
						String msg = Msg.get("error.tempban-shorter-than-last");
						sender.sendMessage(msg);
						return true;
					}
				}
				
				plugin.getBanManager().tempban(name, reason, banner, expires);
			}
			else{
				String ip = name; //Readability
				IPBan ipban = plugin.getBanManager().getIPBan(ip);
				if(ipban != null){
					if(ipban instanceof TempIPBan){
						TempIPBan tiBan = (TempIPBan) ipban;
						if(tiBan.getExpires() > expires){
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
				}
				plugin.getBanManager().tempipban(ip, reason, banner, expires);
			}
			
			String message = Msg.get("announcement.player-was-tempbanned", new String[]{"banner", "name", "reason", "time"}, new String[]{banner, name, reason, Util.getTimeUntil(expires)});
			plugin.getBanManager().announce(message, silent, sender);
			plugin.getBanManager().addHistory(name, banner, message);
			
			return true;
		}
	}
}

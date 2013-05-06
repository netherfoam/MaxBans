package org.maxgamer.maxbans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.banmanager.IPBan;
import org.maxgamer.maxbans.banmanager.TempIPBan;
import org.maxgamer.maxbans.sync.Packet;
import org.maxgamer.maxbans.util.Formatter;
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
				sender.sendMessage(Formatter.primary + " No name given.");
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
					sender.sendMessage(Formatter.secondary + "No IP recorded for " + name + " - Try ban them normally instead?");
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
						sender.sendMessage(Formatter.secondary + "That player has a tempban which will last longer than the one you supplied!");
						return true;
					}
					else{
						//Increasing a previous ban, remove the old one first.
						plugin.getBanManager().unbanip(ip);
					}
				}
				else{
					//Already perma banned
					sender.sendMessage(Formatter.secondary + "That player is already banned.");
					return true;
				}
			}
			
			//Ban them
			plugin.getBanManager().tempipban(ip, reason, banner, time); //IP
			plugin.getBanManager().announce(Formatter.secondary + name + Formatter.primary + " has been temp IP banned ("+Util.getTimeUntil(time)+") by " + Formatter.secondary + banner + Formatter.primary + ". Reason: '" + Formatter.secondary + reason + Formatter.primary + "'.", silent, sender);
			String message = Formatter.secondary + banner + Formatter.primary + " temp IP banned " + Formatter.secondary + name + Formatter.primary + " ("+ Formatter.secondary + ip + Formatter.primary + ") for " + Formatter.secondary + Util.getTimeUntil(time) + Formatter.primary + " for '" + Formatter.secondary + reason + Formatter.primary + "'";
			plugin.getBanManager().addHistory(name, banner, message);
			
	    	if(plugin.getSyncer() != null){
	    		Packet prop = new Packet();
	    		prop.setCommand("ipban");
	    		prop.put("ip", ip);
	    		prop.put("reason", reason);
	    		prop.put("banner", banner);
	    		prop.put("expires", time);
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

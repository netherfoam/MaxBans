package org.maxgamer.maxbans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.banmanager.IPBan;
import org.maxgamer.maxbans.banmanager.TempIPBan;
import org.maxgamer.maxbans.sync.Packet;
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
				sender.sendMessage(Formatter.primary + " No name/IP given.");
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
				sender.sendMessage(Formatter.secondary + "That player is already IP banned.");
				return true;
			}
			
			//Ban them
			plugin.getBanManager().ipban(ip, reason, banner); //IP
			plugin.getBanManager().announce(Formatter.secondary + name + Formatter.primary + " has been banned by " + Formatter.secondary + banner + Formatter.primary + ". Reason: '" + Formatter.secondary + reason + Formatter.primary + "'.", silent, sender);
			String message = Formatter.secondary + banner + Formatter.primary + " IP banned " + Formatter.secondary + name + Formatter.primary + " (" + Formatter.secondary + ip + Formatter.primary + ") for '" + Formatter.secondary + reason + Formatter.primary + "'";
			plugin.getBanManager().addHistory(name, banner, message);
			
	    	if(plugin.getSyncer() != null){
	    		Packet prop = new Packet();
	    		prop.setCommand("ipban");
	    		prop.put("ip", ip);
	    		prop.put("reason", reason);
	    		prop.put("banner", banner);
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

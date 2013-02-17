package org.maxgamer.maxbans.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.maxgamer.maxbans.banmanager.Ban;
import org.maxgamer.maxbans.banmanager.IPBan;
import org.maxgamer.maxbans.banmanager.Mute;
import org.maxgamer.maxbans.banmanager.TempBan;
import org.maxgamer.maxbans.banmanager.TempIPBan;
import org.maxgamer.maxbans.banmanager.TempMute;
import org.maxgamer.maxbans.banmanager.Warn;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.IPAddress;
import org.maxgamer.maxbans.util.RangeBan;
import org.maxgamer.maxbans.util.TempRangeBan;
import org.maxgamer.maxbans.util.Util;

public class CheckBanCommand extends CmdSkeleton{
    public CheckBanCommand(){
    	super("");
    	namePos = 1;
    	usage = Formatter.secondary + "Usage: /checkban <player>"; 
    }
    
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("maxbans.checkban")){ //We know they're a player because console has all perms
			if(sender.hasPermission("maxbans.checkban.self")){
				args = new String[]{((Player) sender).getName()}; //Let them checkban themself, that's all.
			}
			else{
				sender.sendMessage(Formatter.secondary + "You don't have permission to do that");
				return true;
			}
		}
		
		if(args.length > 0){
			String name = args[0];
			String ip;
			
			Ban ban = null;
			IPBan ipBan = null;
			
			if(!Util.isIP(name)){
				name = plugin.getBanManager().match(name);
				if(name == null){
					name = args[0]; //Use exact name then.
				}
				ban = plugin.getBanManager().getBan(name);
				ip = plugin.getBanManager().getIP(name);
				ipBan = plugin.getBanManager().getIPBan(ip);
				
				List<Warn> warnings = plugin.getBanManager().getWarnings(name);
				
				if(warnings != null){
					int ttl = plugin.getConfig().getInt("warn-expirey-in-minutes") * 60000;
					for(Warn warn : plugin.getBanManager().getWarnings(name)){
						long amt = 2*System.currentTimeMillis() - warn.getExpires() + ttl; //2x system.now because util.getTimeUntil() subtracts it once.
						sender.sendMessage(Formatter.secondary + name + Formatter.primary + " was warned for '" + Formatter.secondary + warn.getReason() + Formatter.primary + "' by " + Formatter.secondary + warn.getBanner() + " " + Util.getTimeUntil(amt) + Formatter.primary + " ago.");
					}
				}
				sender.sendMessage(Formatter.primary + "Whitelisted: " + (plugin.getBanManager().isWhitelisted(name)));
			}
			else{
				ip = name;
				ipBan = plugin.getBanManager().getIPBan(name);
			}
			
			RangeBan rb = plugin.getBanManager().getRanger().getBan(new IPAddress(ip));
			if(rb != null){
				if(rb instanceof TempRangeBan){
					TempRangeBan trb = (TempRangeBan) rb;
					sender.sendMessage(Formatter.primary + "RangeBan: " + Formatter.secondary + trb.toString() + Formatter.primary + ", Reason: " + Formatter.secondary + trb.getReason() + Formatter.primary + " expires: " + Formatter.secondary + Util.getTimeUntil(trb.getExpires()));
				}
				else{
					sender.sendMessage(Formatter.primary + "RangeBan: " + rb.toString());
				}
			}
			
			if(ban != null){
				if(ban instanceof TempBan){
					TempBan tBan = (TempBan) ban;
					sender.sendMessage(Formatter.secondary + name + Formatter.primary + " is temp banned for " + Formatter.secondary + tBan.getReason() + Formatter.primary + " by " + Formatter.secondary +  tBan.getBanner() + Formatter.primary + ". Remaining: " + Formatter.secondary + Util.getTimeUntil(tBan.getExpires())+ Formatter.primary + ".");
				}
				else{
					sender.sendMessage(Formatter.secondary + name + Formatter.primary +  " is banned for " + Formatter.secondary + ban.getReason() + Formatter.primary + " by " + Formatter.secondary + ban.getBanner() + Formatter.primary + ".");
				}
			}
			
			if(ipBan != null){
				if(ipBan instanceof TempIPBan){
					TempIPBan tipBan = (TempIPBan) ipBan;
					sender.sendMessage(Formatter.secondary + name + Formatter.primary + " is temp IP banned for " + Formatter.secondary + tipBan.getReason() + Formatter.primary + " by " + Formatter.secondary + tipBan.getBanner() + Formatter.primary + ". Remaining: " + Formatter.secondary + Util.getTimeUntil(tipBan.getExpires())+ Formatter.primary + ".");
				}
				else{
					sender.sendMessage(Formatter.secondary + name + Formatter.primary + " is IP banned for " + Formatter.secondary + ipBan.getReason() + Formatter.primary + " by " + Formatter.secondary +  ipBan.getBanner() + Formatter.primary + ".");
				}
			}
			
			Mute mute = plugin.getBanManager().getMute(name);
			if(mute != null){
				if(mute instanceof TempMute){
					TempMute tMute = (TempMute) mute;
					sender.sendMessage(Formatter.secondary + name + Formatter.primary + " is temp muted by " + Formatter.secondary + tMute.getBanner() + Formatter.primary + ". Remaining: " + Formatter.secondary +Util.getTimeUntil(tMute.getExpires()) + Formatter.primary + ".");
				}
				else{
					sender.sendMessage(Formatter.secondary + name + Formatter.primary + " is muted by " + mute.getBanner() + Formatter.primary + ".");
				}
			}
			
			if(mute == null && ipBan == null && ban == null){
				sender.sendMessage(Formatter.primary + "No IP ban, ban or mute records for " + Formatter.secondary + name);
			}
			
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}

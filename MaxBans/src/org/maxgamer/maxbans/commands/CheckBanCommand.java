package org.maxgamer.maxbans.commands;

import java.util.HashSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.Msg;
import org.maxgamer.maxbans.banmanager.Ban;
import org.maxgamer.maxbans.banmanager.IPBan;
import org.maxgamer.maxbans.banmanager.Mute;
import org.maxgamer.maxbans.banmanager.RangeBan;
import org.maxgamer.maxbans.banmanager.Temporary;
import org.maxgamer.maxbans.banmanager.Warn;
import org.maxgamer.maxbans.util.DNSBL.CacheRecord;
import org.maxgamer.maxbans.util.DNSBL.DNSStatus;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.IPAddress;
import org.maxgamer.maxbans.util.Util;

public class CheckBanCommand extends CmdSkeleton{
    public CheckBanCommand(){
    	super("checkban", "");
    	namePos = 1; 
    }
    
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("maxbans.checkban")){ //We know they're a player because console has all perms
			if(sender.hasPermission("maxbans.checkban.self")){
				args = new String[]{((Player) sender).getName()}; //Let them checkban themself, that's all.
			}
			else{
				sender.sendMessage(Msg.get("error.no-permission"));
				return true;
			}
		}
		else if(args.length <= 0){
			sender.sendMessage(ChatColor.RED + getUsage());
			return true;
		}
		
		String name = args[0];
		String ip;
		
		if(!Util.isIP(name)){
			name = plugin.getBanManager().match(name);
			ip = plugin.getBanManager().getIP(name);
			Ban ban = plugin.getBanManager().getBan(name);
			Mute mute = plugin.getBanManager().getMute(name);
			boolean white = plugin.getBanManager().isWhitelisted(name);
			
			sender.sendMessage(Formatter.secondary + "+---------------------------------------------------+");
			sender.sendMessage(Formatter.primary + "User: " + Formatter.secondary + name);
			sender.sendMessage(Formatter.primary + "Banned: " + Formatter.secondary + (ban == null ? "False" : "'" + ban.getReason() + Formatter.secondary + "' (" + ban.getBanner() + ")" + (ban instanceof Temporary ? " Ends: " + Util.getShortTime(((Temporary) ban).getExpires() - System.currentTimeMillis()) : "")));
			sender.sendMessage(Formatter.primary + "Muted: " + Formatter.secondary + (mute == null ? "False" : "'" + mute.getReason() + Formatter.secondary + "' (" + mute.getBanner() + ")" + (mute instanceof Temporary ? " Ends: " + Util.getShortTime(((Temporary) mute).getExpires() - System.currentTimeMillis()) : "")));
			
			List<Warn> warnings = plugin.getBanManager().getWarnings(name);
			if(warnings == null || warnings.isEmpty()) sender.sendMessage(Formatter.primary + "Warnings: " + Formatter.secondary + "(0)");
			else{
				sender.sendMessage(Formatter.primary + "Warnings: " + Formatter.secondary + "(" + warnings.size() + ")");
				for(Warn w : warnings){
					sender.sendMessage(Formatter.secondary + "'" + w.getReason() + "' (" + w.getBanner() + ") Expires: " + Util.getShortTime(w.getExpires() - System.currentTimeMillis()));
				}
			}
			
			sender.sendMessage(Formatter.primary + "Whitelisted: " + Formatter.secondary + (white ? "True" : "False"));
		}
		else{
			ip = name;
			IPBan ban = plugin.getBanManager().getIPBan(ip);
			RangeBan rb = plugin.getBanManager().getBan(new IPAddress(ip));
			
			sender.sendMessage(Formatter.secondary + "+---------------------------------------------------+");
			sender.sendMessage(Formatter.primary + "IP: " + Formatter.secondary + ip);
			sender.sendMessage(Formatter.primary + "IP Banned: " + Formatter.secondary + (ban == null ? "False" : "'" + ban.getReason() + Formatter.secondary + "' (" + ban.getBanner() + ")" + (ban instanceof Temporary ? " Ends: " + Util.getShortTime(((Temporary) ban).getExpires() - System.currentTimeMillis()) : "")));
			sender.sendMessage(Formatter.primary + "RangeBan: " + Formatter.secondary + (rb == null ? "False" : rb.toString() + " '" + rb.getReason() + Formatter.secondary + "' (" + rb.getBanner() + ")" + (ban instanceof Temporary ? " Ends: " + Util.getShortTime(((Temporary) rb).getExpires() - System.currentTimeMillis()) : "")));
			
			if(plugin.getBanManager().getDNSBL() != null){
				CacheRecord r = plugin.getBanManager().getDNSBL().getRecord(ip);
				if(r != null){
					sender.sendMessage(Formatter.primary + "Proxy: " + Formatter.secondary + (r.getStatus() == DNSStatus.ALLOWED ? "False" : "True"));
				}
			}
			
			HashSet<String> dupeip = plugin.getBanManager().getUsers(ip);
			sender.sendMessage(Formatter.primary + "Users: " + Formatter.secondary + (dupeip == null ? "0" : dupeip.size()));
		}
		sender.sendMessage(Formatter.secondary + "+---------------------------------------------------+");
		
		
		return true;
	}
}

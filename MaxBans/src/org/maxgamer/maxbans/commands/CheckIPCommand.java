package org.maxgamer.maxbans.commands;

import java.util.HashSet;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.banmanager.IPBan;
import org.maxgamer.maxbans.banmanager.RangeBan;
import org.maxgamer.maxbans.banmanager.Temporary;
import org.maxgamer.maxbans.util.DNSBL.CacheRecord;
import org.maxgamer.maxbans.util.DNSBL.DNSStatus;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.IPAddress;
import org.maxgamer.maxbans.util.Util;

public class CheckIPCommand extends CmdSkeleton{
    public CheckIPCommand(){
        super("checkip", "maxbans.checkip");
    }
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length > 0){
			String name = args[0];
			
			String ip;
			
			if(Util.isIP(args[0])){
				ip = args[0];
			}
			else{
				name = plugin.getBanManager().match(name);
				if(name == null){
					name = args[0]; //Use exact name then.
				}
				ip = plugin.getBanManager().getIP(name);
				
				if(ip == null){
					sender.sendMessage(Formatter.primary + "That player has no IP history!");
					return true;
				}
			}
			
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
			sender.sendMessage(Formatter.primary + "GeoIP: " + Formatter.secondary + "http://www.geoiptool.com/en/?IP=" + ip);
			if(plugin.getGeoDB() != null) sender.sendMessage(Formatter.primary + "Country: " + Formatter.secondary + plugin.getGeoDB().getCountry(ip));
			sender.sendMessage(Formatter.secondary + "+---------------------------------------------------+");
			
			return true;
		}
		else{
			sender.sendMessage(getUsage());
			return true;
		}
	}
}

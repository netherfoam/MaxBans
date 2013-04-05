package org.maxgamer.maxbans.commands;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.Util;

public class DupeIPCommand extends CmdSkeleton{
	private static ChatColor banned = ChatColor.RED;
	private static ChatColor online = ChatColor.GREEN;
	private static ChatColor offline = ChatColor.GRAY;
    
    public DupeIPCommand(){
        super("dupeip", "maxbans.dupeip");
    }
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length > 0){
			String name = args[0];
			
			String ip;
			if(!Util.isIP(name)){
				name = plugin.getBanManager().match(name);
				if(name == null){
					name = args[0]; //Use exact name then.
				}
				ip = plugin.getBanManager().getIP(name);
				
				if(ip == null){
					sender.sendMessage(Formatter.primary + "Player " + Formatter.secondary + name + Formatter.primary + " has no IP history.");
					return true;
				}
			}
			else{
				ip = name;
			}
			
			sender.sendMessage(getScanningString(name, ip));
			
			StringBuilder sb = new StringBuilder();
			
			HashSet<String> dupes = plugin.getBanManager().getUsers(ip);
			if(dupes != null){ //Could be null if we're looking up an invalid IP
				for(String dupe : dupes){
					if(dupe.equalsIgnoreCase(name)) continue; //Same guy is not a duplicate of himself.
					sb.append(getChatColor(dupe).toString()+ dupe + ", ");
				}
			}
			
			if(sb.length() > 0){
				sb.replace(sb.length() - 2, sb.length(), "");
				sender.sendMessage(sb.toString());
			}
			else{
				sender.sendMessage(Formatter.primary + "No duplicates!");
			}
			return true;
		}
		else{
			sender.sendMessage(getUsage());
			return true;
		}
	}
	
	public static String getScanningString(String name, String ip){
		return Formatter.primary + "Scanning " + getChatColor(name) + name + Formatter.primary + " on " + Formatter.secondary + ip + ChatColor.WHITE + ".  [" + banned + "Banned" + ChatColor.WHITE + "] [" + online + "Online" + ChatColor.WHITE + "] [" + offline + "Offline" + ChatColor.WHITE + "]";
	}
	
	public static ChatColor getChatColor(String name){
		if(Util.isIP(name)){
			if(MaxBans.instance.getBanManager().getIPBan(name) != null) return banned;
			else return offline; //Well...
		}
		else{
			if(MaxBans.instance.getBanManager().getBan(name) != null) return banned;
			if(Bukkit.getPlayerExact(name) != null) return online;
			return offline;
		}
	}
}

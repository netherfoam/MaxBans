package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.banmanager.RangeBan;
import org.maxgamer.maxbans.banmanager.TempRangeBan;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.IPAddress;
import org.maxgamer.maxbans.util.Util;

public class TempRangeBanCommand extends CmdSkeleton{
	public TempRangeBanCommand(){
		super("temprangeban", "maxbans.temprangeban");
	}

	@Override
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length < 3){
			sender.sendMessage(getUsage());
			return true;
		}
		
		String banner = Util.getName(sender);
		boolean silent = Util.isSilent(args);
		String[] ips = args[0].split("-");
		
		if(ips.length == 1 && ips[0].contains("*")){
			//If they supplied us with /rangeban 127.0.0.*, then we know its actually 127.0.0.0-127.0.0.255.
			ips = new String[]{ips[0].replace('*', '0'), ips[0].replace("*", "255")};
		}
		else if(ips.length != 2){
			sender.sendMessage(ChatColor.RED + "Not enough IP addresses supplied! Usage: " + getUsage());
			return true;
		}
		
		for(int i = 0; i < ips.length; i++){
			if(!Util.isIP(ips[i])){
				sender.sendMessage(ChatColor.RED + ips[i] + " is not a valid IP address.");
				return true;
			}
		}
		
		IPAddress start = new IPAddress(ips[0]);
		IPAddress end = new IPAddress(ips[1]);
		
		long expires = Util.getTime(args) + System.currentTimeMillis();
		if(expires <= 0){
			sender.sendMessage(getUsage());
			return true;
		}
		String reason = Util.buildReason(args);
		
		TempRangeBan rb = new TempRangeBan(banner, reason, System.currentTimeMillis(), expires, start, end);
		RangeBan overlap = plugin.getBanManager().ban(rb);
		if(overlap == null){
			plugin.getBanManager().announce(Formatter.secondary + banner + Formatter.primary + " banned " + Formatter.secondary + rb.toString() + Formatter.primary + ". Reason: '" + Formatter.secondary + reason + Formatter.primary + "' Remaining: "+ Formatter.secondary + Util.getTimeUntil(rb.getExpires()), silent, sender);
			for(Player p : Bukkit.getOnlinePlayers()){
				if(rb.contains(new IPAddress(p.getAddress().getAddress().getHostAddress()))){
					p.kickPlayer(rb.getKickMessage());
				}
			}
		}
		else{
			sender.sendMessage(ChatColor.RED + "That RangeBan overlaps an existing one! (" + overlap.toString() + ")");
		}
		
		return true;
	}
	
}
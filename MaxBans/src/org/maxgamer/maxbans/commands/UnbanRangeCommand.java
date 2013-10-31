package org.maxgamer.maxbans.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.banmanager.RangeBan;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.IPAddress;
import org.maxgamer.maxbans.util.Util;

public class UnbanRangeCommand extends CmdSkeleton{
	public UnbanRangeCommand(){
		super("unrangeban", "maxbans.unbanrange");
		this.minArgs = 1;
	}

	@Override
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		boolean silent = Util.isSilent(args);
		String banner = Util.getName(sender);
		
		if(!Util.isIP(args[0])){
			sender.sendMessage(ChatColor.RED + args[0] + " is not a valid IP!");
			return true;
		}
		
		IPAddress ip = new IPAddress(args[0]);
		
		RangeBan rb = plugin.getBanManager().getBan(ip);
		if(rb == null){
			sender.sendMessage(ChatColor.RED + ip.toString() + " is not banned!");
			return true;
		}
		
		plugin.getBanManager().unban(rb);
		plugin.getBanManager().announce(Formatter.secondary + banner + Formatter.primary + " unbanned the IP Range " + Formatter.secondary + rb.toString() + Formatter.primary + "!", silent, sender);
		
		String msg = Formatter.secondary + banner + Formatter.primary + " unbanned the IP Range " + Formatter.secondary + rb.toString() + Formatter.primary + "!";
		plugin.getBanManager().addHistory(rb.toString(), banner, msg);
		
		return true;
	}
	
	
}
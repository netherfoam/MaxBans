package org.maxgamer.maxbans.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.Ban;
import org.maxgamer.maxbans.banmanager.IPBan;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.Util;

public class UnbanCommand implements CommandExecutor{
    private MaxBans plugin;
    public UnbanCommand(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("maxbans.unban")){
			sender.sendMessage(Formatter.secondary + "You don't have permission to do that");
			return true;
		}
		String usage = Formatter.secondary + "Usage: /unban <player>";
		
		if(args.length > 0){
			boolean silent = Util.isSilent(args);
			String name = args[0];
			if(name.isEmpty()){
				sender.sendMessage(Formatter.primary + " No name given.");
				return true;
			}
			
			Ban ban = null;
			IPBan ipBan = null;
						
			if(!Util.isIP(name)){
				name = plugin.getBanManager().match(name, true);
				ban = plugin.getBanManager().getBan(name);
				ipBan = plugin.getBanManager().getIPBan(plugin.getBanManager().getIP(name));
			}
			else{
				ipBan = plugin.getBanManager().getIPBan(name);
			}
			
			String banner = Util.getName(sender);
			
			if(ban != null || ipBan != null){
				plugin.getBanManager().unban(name);
				plugin.getBanManager().unbanip(name);
			}
			else{
				sender.sendMessage(Formatter.primary + "Could not find a ban for " + Formatter.secondary + name + Formatter.primary + ".");
				return true;
			}
			
			if(silent){
				sender.sendMessage(ChatColor.ITALIC + "" + Formatter.secondary + name + Formatter.primary + " has been silenty unbanned.");
			}
			else{
				plugin.getBanManager().announce(Formatter.secondary + name + Formatter.primary + " has been unbanned by " + Formatter.secondary + banner + Formatter.primary + ".");
			}
			plugin.getBanManager().addHistory(banner + " unbanned " + name);
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}

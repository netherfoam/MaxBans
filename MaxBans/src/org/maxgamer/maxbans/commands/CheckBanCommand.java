package org.maxgamer.maxbans.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.Ban;
import org.maxgamer.maxbans.banmanager.IPBan;
import org.maxgamer.maxbans.banmanager.Mute;
import org.maxgamer.maxbans.banmanager.TempBan;
import org.maxgamer.maxbans.banmanager.TempIPBan;
import org.maxgamer.maxbans.banmanager.TempMute;

public class CheckBanCommand implements CommandExecutor{
    private MaxBans plugin;
    public CheckBanCommand(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String usage = ChatColor.RED + "Usage: /checkban <player>";
		
		if(args.length > 0){
			String name = args[0];
			
			Ban ban = plugin.getBanManager().getBan(name);
			
			if(ban != null){
				if(ban instanceof TempBan){
					TempBan tBan = (TempBan) ban;
					sender.sendMessage(ChatColor.AQUA + name + " is temp banned for " + tBan.getReason() + " by " + tBan.getBanner() + " until " + plugin.getBanManager().getTimeUntil(tBan.getExpires())+".");
				}
				else{
					sender.sendMessage(ChatColor.AQUA + name + " is banned for " + ban.getReason() + " by " + ban.getBanner() + ".");
				}
			}
			
			String ip = plugin.getBanManager().getIP(name);
			IPBan ipBan = plugin.getBanManager().getIPBan(ip);
			
			if(ipBan != null){
				if(ipBan instanceof TempIPBan){
					TempIPBan tipBan = (TempIPBan) ipBan;
					sender.sendMessage(ChatColor.AQUA + name + " is temp IP banned for " + tipBan.getReason() + " by " + tipBan.getBanner() + " until " + plugin.getBanManager().getTimeUntil(tipBan.getExpires())+".");
				}
				else{
					sender.sendMessage(ChatColor.AQUA + name + " is IP banned for " + ipBan.getReason() + " by " + ipBan.getBanner() + ".");
				}
			}
			
			Mute mute = plugin.getBanManager().getMute(name);
			if(mute != null){
				if(mute instanceof TempMute){
					TempMute tMute = (TempMute) mute;
					sender.sendMessage(ChatColor.AQUA + name + " is temp muted by " + tMute.getMuter() + " until " + tMute.getExpires() + ".");
				}
				else{
					sender.sendMessage(ChatColor.AQUA + name + " is muted by " + mute.getMuter() + ".");
				}
			}
			
			if(mute == null && ipBan == null && ban == null){
				sender.sendMessage(ChatColor.AQUA + "No records for " + name);
			}
			
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}

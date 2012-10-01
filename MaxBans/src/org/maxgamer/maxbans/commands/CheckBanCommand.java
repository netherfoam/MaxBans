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
					sender.sendMessage(ChatColor.RED + name + ChatColor.AQUA + " is temp banned for " + ChatColor.RED + tBan.getReason() + ChatColor.AQUA + " by " + ChatColor.RED +  tBan.getBanner() + ChatColor.AQUA + " until " + ChatColor.RED + plugin.getBanManager().getTimeUntil(tBan.getExpires())+ ChatColor.AQUA + ".");
				}
				else{
					sender.sendMessage(ChatColor.RED + name + ChatColor.AQUA +  " is banned for " + ChatColor.RED + ban.getReason() + ChatColor.AQUA + " by " + ChatColor.RED + ban.getBanner() + ChatColor.AQUA + ".");
				}
			}
			
			String ip = plugin.getBanManager().getIP(name);
			IPBan ipBan = plugin.getBanManager().getIPBan(ip);
			
			if(ipBan != null){
				if(ipBan instanceof TempIPBan){
					TempIPBan tipBan = (TempIPBan) ipBan;
					sender.sendMessage(ChatColor.RED + name + ChatColor.AQUA + " is temp IP banned for " + ChatColor.RED + tipBan.getReason() + ChatColor.AQUA + " by " + ChatColor.RED + tipBan.getBanner() + ChatColor.AQUA + " until " + ChatColor.RED + plugin.getBanManager().getTimeUntil(tipBan.getExpires())+ ChatColor.AQUA + ".");
				}
				else{
					sender.sendMessage(ChatColor.RED + name + ChatColor.AQUA + " is IP banned for " + ChatColor.RED + ipBan.getReason() + ChatColor.AQUA + " by " + ChatColor.RED +  ipBan.getBanner() + ChatColor.AQUA + ".");
				}
			}
			
			Mute mute = plugin.getBanManager().getMute(name);
			if(mute != null){
				if(mute instanceof TempMute){
					TempMute tMute = (TempMute) mute;
					sender.sendMessage(ChatColor.RED + name + ChatColor.AQUA + " is temp muted by " + ChatColor.RED + tMute.getMuter() + ChatColor.AQUA + " until " + ChatColor.RED + tMute.getExpires() + ChatColor.AQUA + ".");
				}
				else{
					sender.sendMessage(ChatColor.RED + name + ChatColor.AQUA + " is muted by " + mute.getMuter() + ChatColor.AQUA + ".");
				}
			}
			
			if(mute == null && ipBan == null && ban == null){
				sender.sendMessage(ChatColor.AQUA + "No records for " + ChatColor.RED + name);
			}
			
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}

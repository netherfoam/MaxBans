package org.maxgamer.maxbans.commands;

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
		if(!sender.hasPermission("maxbans.checkban")){
			sender.sendMessage(plugin.color_secondary + "You don't have permission to do that");
			return true;
		}
		
		String usage = plugin.color_secondary + "Usage: /checkban <player>";
		
		if(args.length > 0){
			String name = args[0];
			
			name = plugin.getBanManager().match(name);
			if(name == null){
				name = args[0]; //Use exact name then.
			}
			
			Ban ban = plugin.getBanManager().getBan(name);
			
			if(ban != null){
				if(ban instanceof TempBan){
					TempBan tBan = (TempBan) ban;
					sender.sendMessage(plugin.color_secondary + name + plugin.color_primary + " is temp banned for " + plugin.color_secondary + tBan.getReason() + plugin.color_primary + " by " + plugin.color_secondary +  tBan.getBanner() + plugin.color_primary + ". Remaining: " + plugin.color_secondary + plugin.getBanManager().getTimeUntil(tBan.getExpires())+ plugin.color_primary + ".");
				}
				else{
					sender.sendMessage(plugin.color_secondary + name + plugin.color_primary +  " is banned for " + plugin.color_secondary + ban.getReason() + plugin.color_primary + " by " + plugin.color_secondary + ban.getBanner() + plugin.color_primary + ".");
				}
			}
			
			String ip = plugin.getBanManager().getIP(name);
			IPBan ipBan = plugin.getBanManager().getIPBan(ip);
			
			if(ipBan != null){
				if(ipBan instanceof TempIPBan){
					TempIPBan tipBan = (TempIPBan) ipBan;
					sender.sendMessage(plugin.color_secondary + name + plugin.color_primary + " is temp IP banned for " + plugin.color_secondary + tipBan.getReason() + plugin.color_primary + " by " + plugin.color_secondary + tipBan.getBanner() + plugin.color_primary + ". Remaining: " + plugin.color_secondary + plugin.getBanManager().getTimeUntil(tipBan.getExpires())+ plugin.color_primary + ".");
				}
				else{
					sender.sendMessage(plugin.color_secondary + name + plugin.color_primary + " is IP banned for " + plugin.color_secondary + ipBan.getReason() + plugin.color_primary + " by " + plugin.color_secondary +  ipBan.getBanner() + plugin.color_primary + ".");
				}
			}
			
			Mute mute = plugin.getBanManager().getMute(name);
			if(mute != null){
				if(mute instanceof TempMute){
					TempMute tMute = (TempMute) mute;
					sender.sendMessage(plugin.color_secondary + name + plugin.color_primary + " is temp muted by " + plugin.color_secondary + tMute.getMuter() + plugin.color_primary + ". Remaining: " + plugin.color_secondary + tMute.getExpires() + plugin.color_primary + ".");
				}
				else{
					sender.sendMessage(plugin.color_secondary + name + plugin.color_primary + " is muted by " + mute.getMuter() + plugin.color_primary + ".");
				}
			}
			
			if(mute == null && ipBan == null && ban == null){
				sender.sendMessage(plugin.color_primary + "No records for " + plugin.color_secondary + name);
			}
			
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}

package org.maxgamer.maxbans.commands;

import java.util.List;

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
import org.maxgamer.maxbans.banmanager.Warn;
import org.maxgamer.maxbans.util.Util;

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
			
			Ban ban = null;
			IPBan ipBan = null;
			
			if(!Util.isIP(name)){
				name = plugin.getBanManager().match(name);
				if(name == null){
					name = args[0]; //Use exact name then.
				}
				ban = plugin.getBanManager().getBan(name);
				String ip = plugin.getBanManager().getIP(name);
				ipBan = plugin.getBanManager().getIPBan(ip);
				
				List<Warn> warnings = plugin.getBanManager().getWarnings(name);
				
				if(warnings != null){
					int ttl = plugin.getConfig().getInt("warn-expirey-in-minutes") * 60000;
					for(Warn warn : plugin.getBanManager().getWarnings(name)){
						long amt = 2*System.currentTimeMillis() - warn.getExpires() + ttl; //2x system.now because util.getTimeUntil() subtracts it once.
						sender.sendMessage(plugin.color_secondary + name + plugin.color_primary + " was warned for " + plugin.color_secondary + warn.getReason() + plugin.color_primary + " by " + plugin.color_secondary + warn.getBanner() + " " + Util.getTimeUntil(amt) + plugin.color_primary + " ago.");
					}
				}
			}
			else{
				ipBan = plugin.getBanManager().getIPBan(name);
			}
			if(ban != null){
				if(ban instanceof TempBan){
					TempBan tBan = (TempBan) ban;
					sender.sendMessage(plugin.color_secondary + name + plugin.color_primary + " is temp banned for " + plugin.color_secondary + tBan.getReason() + plugin.color_primary + " by " + plugin.color_secondary +  tBan.getBanner() + plugin.color_primary + ". Remaining: " + plugin.color_secondary + Util.getTimeUntil(tBan.getExpires())+ plugin.color_primary + ".");
				}
				else{
					sender.sendMessage(plugin.color_secondary + name + plugin.color_primary +  " is banned for " + plugin.color_secondary + ban.getReason() + plugin.color_primary + " by " + plugin.color_secondary + ban.getBanner() + plugin.color_primary + ".");
				}
			}
			
			if(ipBan != null){
				if(ipBan instanceof TempIPBan){
					TempIPBan tipBan = (TempIPBan) ipBan;
					sender.sendMessage(plugin.color_secondary + name + plugin.color_primary + " is temp IP banned for " + plugin.color_secondary + tipBan.getReason() + plugin.color_primary + " by " + plugin.color_secondary + tipBan.getBanner() + plugin.color_primary + ". Remaining: " + plugin.color_secondary + Util.getTimeUntil(tipBan.getExpires())+ plugin.color_primary + ".");
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

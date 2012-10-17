package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.Mute;
import org.maxgamer.maxbans.banmanager.TempMute;

public class TempMuteCommand implements CommandExecutor{
    private MaxBans plugin;
    public TempMuteCommand(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("maxbans.tempmute")){
			sender.sendMessage(plugin.color_secondary + "You don't have permission to do that");
			return true;
		}
		String usage = plugin.color_secondary + "Usage: /tempmute <player> <time> <timeform>";
		
		if(args.length > 2){
			String name = args[0];
			name = plugin.getBanManager().match(name);
			if(name == null){
				name = args[0]; //Use exact name then.
			}
			
			String banner;
			
			if(sender instanceof Player){
				banner = ((Player) sender).getName();
			}
			else{
				banner = "Console";
			}
			
			long time = plugin.getBanManager().getTime(args);
			
			if(time <= 0){
				sender.sendMessage(usage);
				return true;
			}
			time += System.currentTimeMillis();
			
			//Make sure giving this mute will change something.
			Mute mute = plugin.getBanManager().getMute(name);
			if(mute != null){
				if(mute instanceof TempMute){
					TempMute tMute = (TempMute) mute;
					if(tMute.getExpires() > time){
						sender.sendMessage(plugin.color_primary + "That player already has a mute which lasts longer than the one you tried to give.");
						return true;
					}
					//else: Continue normally.
				}
				else{
					sender.sendMessage(plugin.color_primary + "That player is already permantly muted.");
					return true;
				}
			}
			
			plugin.getBanManager().tempmute(name, banner, time);
			
			String until = plugin.getBanManager().getTimeUntil(time/1000*1000);
			Player p = Bukkit.getPlayerExact(name);
			if(p != null){
				p.sendMessage(plugin.color_secondary + "You have been muted for " + until);
			}
			sender.sendMessage(plugin.color_primary + "Muted " + plugin.color_secondary + name + plugin.color_primary + " for " + plugin.color_secondary + until);
			
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}

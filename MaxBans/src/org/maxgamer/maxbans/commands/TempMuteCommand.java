package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.banmanager.Mute;
import org.maxgamer.maxbans.banmanager.TempMute;
import org.maxgamer.maxbans.sync.Packet;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.Util;

public class TempMuteCommand extends CmdSkeleton{
    public TempMuteCommand(){
        super("tempmute", "maxbans.tempmute");
        //usage = Formatter.secondary + "Usage: /tempmute <player> <time> <timeform>";
    }
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length > 2){
			String name = args[0];
			name = plugin.getBanManager().match(name);
			if(name == null){
				name = args[0]; //Use exact name then.
			}
			
			String banner = Util.getName(sender);
			
			long time = Util.getTime(args);
			
			if(time <= 0){
				sender.sendMessage(getUsage());
				return true;
			}
			time += System.currentTimeMillis();
			
			//Make sure giving this mute will change something.
			Mute mute = plugin.getBanManager().getMute(name);
			if(mute != null){
				if(mute instanceof TempMute){
					TempMute tMute = (TempMute) mute;
					if(tMute.getExpires() > time){
						sender.sendMessage(Formatter.primary + "That player already has a mute which lasts longer than the one you tried to give.");
						return true;
					}
					//else: Continue normally.
				}
				else{
					sender.sendMessage(Formatter.primary + "That player is already permantly muted.");
					return true;
				}
			}
			
			plugin.getBanManager().tempmute(name, banner, time);
			
			String until = Util.getTimeUntil(time/1000*1000);
			Player p = Bukkit.getPlayerExact(name);
			if(p != null){
				p.sendMessage(Formatter.secondary + "You have been muted for " + until);
			}
			sender.sendMessage(Formatter.primary + "Muted " + Formatter.secondary + name + Formatter.primary + " for " + Formatter.secondary + until);
			String message = Formatter.secondary + banner + Formatter.primary + " temp muted " + Formatter.secondary + name + Formatter.primary + " for " + Formatter.secondary + until;
			plugin.getBanManager().addHistory(name, banner, message);
			
	    	if(plugin.getSyncer() != null){
	    		Packet prop = new Packet();
	    		prop.setCommand("tempmute");
	    		prop.put("name", name);
	    		prop.put("banner", banner);
	    		prop.put("expires", time);
	    		plugin.getSyncer().broadcast(prop);
	    		
	    		//Send the addhistory request.
	    		Packet history = new Packet().setCommand("addhistory").put("string", message).put("banner", banner).put("name", name);
	    		plugin.getSyncer().broadcast(history);
	    	}
			
			return true;
		}
		else{
			sender.sendMessage(getUsage());
			return true;
		}
	}
}

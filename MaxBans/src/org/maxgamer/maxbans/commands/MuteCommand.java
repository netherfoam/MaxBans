package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.banmanager.Mute;
import org.maxgamer.maxbans.sync.Packet;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.Util;

public class MuteCommand extends CmdSkeleton{
    public MuteCommand(){
        super("maxbans.mute");
        usage = Formatter.secondary + "Usage: /mute <player>";
    }
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length > 0){
			String name = args[0];
			
			name = plugin.getBanManager().match(name);
			if(name == null){
				name = args[0]; //Use exact name then.
			}
			
			Mute mute = plugin.getBanManager().getMute(name);
			if(mute != null){
				plugin.getBanManager().unmute(name);
				sender.sendMessage(ChatColor.GREEN + "Unmuted " + name);
				
		    	if(plugin.getSyncer() != null){
		    		Packet prop = new Packet();
		    		prop.setCommand("unmute");
		    		prop.put("name", name);
		    		plugin.getSyncer().send(prop);
		    	}
				
				return true;
			}
			
			String banner = Util.getName(sender);
			
			plugin.getBanManager().mute(name, banner);
			
			Player p = Bukkit.getPlayerExact(name);
			if(p != null){
				p.sendMessage(Formatter.secondary + " You have been muted.");
			}
			sender.sendMessage(Formatter.primary + "Muted " + Formatter.secondary + name);
			String message = Formatter.secondary + banner + Formatter.primary + " muted " + Formatter.secondary + name;
			plugin.getBanManager().addHistory(message);
			
	    	if(plugin.getSyncer() != null){
	    		Packet prop = new Packet();
	    		prop.setCommand("mute");
	    		prop.put("name", name);
	    		prop.put("banner", banner);
	    		plugin.getSyncer().broadcast(prop);
	    		
	    		//Send the addhistory request.
	    		Packet history = new Packet().setCommand("addhistory").put("string", message);
	    		plugin.getSyncer().broadcast(history);
	    	}
			
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}

package org.maxgamer.maxbans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.sync.Packet;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.Util;

public class WhitelistCommand extends CmdSkeleton{
	public WhitelistCommand(){
		super("mbwhitelist", "maxbans.whitelist");
		minArgs = 1;
		namePos = 1;
	}

	@Override
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		String name = plugin.getBanManager().match(args[0]);
		String banner = Util.getName(sender);
		
		boolean on = !plugin.getBanManager().isWhitelisted(name);
		plugin.getBanManager().setWhitelisted(name, on);
		
		String msg = Formatter.secondary + name + Formatter.primary + " is " + Formatter.secondary + (on ? "whitelisted" + Formatter.primary + " and may " : "no longer whitelisted" + Formatter.primary + " and may not ") + "bypass any IP/Range/DNSBL bans.";
		sender.sendMessage(msg);
		msg = Formatter.secondary + name + Formatter.primary + " was " + (on ? "whitelisted" : "unwhitelisted") + " by " + Formatter.secondary + banner;
		plugin.getBanManager().addHistory(name, banner, msg);
		
    	if(plugin.getSyncer() != null){
    		Packet prop = new Packet();
    		prop.setCommand("whitelist");
    		prop.put("name", name);
    		prop.put("banner", banner);
    		prop.put("state", on);
    		plugin.getSyncer().broadcast(prop);
    		
    		//Send the addhistory request.
    		Packet history = new Packet().setCommand("addhistory").put("string", msg).put("banner", banner).put("name", name);
    		plugin.getSyncer().broadcast(history);
    	}
		
		return true;
	}
}
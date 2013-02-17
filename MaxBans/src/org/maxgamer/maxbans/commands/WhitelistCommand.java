package org.maxgamer.maxbans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.util.Formatter;

public class WhitelistCommand extends CmdSkeleton{
	public WhitelistCommand(){
		super("maxbans.whitelist");
		usage = "/mbwhitelist <user>";
		minArgs = 1;
		namePos = 1;
	}

	@Override
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		String name = plugin.getBanManager().match(args[0]);
		
		boolean on = !plugin.getBanManager().isWhitelisted(name);
		plugin.getBanManager().setWhitelisted(name, on);
		
		String msg = Formatter.secondary + name + Formatter.primary + " is " + Formatter.secondary + (on ? "whitelisted" + Formatter.primary + " and may " : "no longer whitelisted" + Formatter.primary + " and may not ") + "bypass any IP/Range/DNSBL bans.";
		sender.sendMessage(msg);
		
		return true;
	}
}
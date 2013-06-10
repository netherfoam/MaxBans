package org.maxgamer.maxbans.commands;

import java.text.ParseException;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.util.Util;

public class ImmuneCommand extends CmdSkeleton{
	public ImmuneCommand(){
		super("immune", "maxbans.immune");
	}

	@Override
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length < 2){
			sender.sendMessage(ChatColor.RED + "Usage: /immune <name> <true|false>");
			return true;
		}
		try{
			boolean immune = Util.parseBoolean(args[1]);
			String user = MaxBans.instance.getBanManager().match(args[0]);
			if(plugin.getBanManager().setImmunity(user, immune)){
				sender.sendMessage(ChatColor.GREEN + "Success!");
			}
			else{
				sender.sendMessage(ChatColor.RED + "Failure - Nothing has changed.");
			}
		}
		catch (ParseException e) {
			sender.sendMessage(ChatColor.RED + "Unrecognised state " + args[1]);
		}
		return true;
	}
	
}
package org.maxgamer.maxbans.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.MaxBans;

public abstract class CmdSkeleton implements CommandExecutor{
	protected String perm;
	protected MaxBans plugin = MaxBans.instance;
	protected String usage;
	protected int minArgs = 0;
	
	public CmdSkeleton(String perm){
		this.perm = perm;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(perm != null && !perm.isEmpty() && !sender.hasPermission(perm)){
			sender.sendMessage(ChatColor.RED + "You don't have the permission: " + perm + " to do that.");
			return true;
		}
		
		if(minArgs > 0 && args.length < minArgs && usage != null){
			sender.sendMessage(usage);
			return true;
		}
		
		return run(sender, cmd, label, args);
	}

	public abstract boolean run(CommandSender sender, Command cmd, String label, String[] args);
}
package org.maxgamer.maxbans.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.maxgamer.maxbans.MaxBans;

public abstract class CmdSkeleton implements CommandExecutor, TabCompleter{
	/** A quick reference to the MaxBans plugin */
	protected MaxBans plugin = MaxBans.instance;
	/** The permission required to execute this command. This is done automatically. */
	protected String perm;
	/** The String usage for this command. E.g. /ban [name] [reason] */
	protected String usage;
	/** The minimum number of arguments this command must have. If this isn't met, the usage is sent instead of being run. */
	protected int minArgs = 0;
	
	/** The position in the args[] array which is a name. Use less than 1 for doesn't take a name, or no tab autocomplete intended */
	protected int namePos = 1;
	
	public CmdSkeleton(String perm){
		this.perm = perm;
	}
	
	/**
	 * Checks the player has permission, if this command has a permission node,
	 * then checks there are enough arguments.  If both succeed, it runs this
	 * command.
	 */
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
	
	/**
	 * Helps autocomplete names in some commands.
	 * If this.namePos is less than 1, this method will not autocomplete.
	 * Additionally, you can override this if you want.
	 */
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
    	ArrayList<String> results = new ArrayList<String>();
    	
    	if(args.length == namePos){
        	String bestMatch = plugin.getBanManager().match(args[0]);
        	if(bestMatch == null){
        		return results;
        	}
        	else{
        		results.add(bestMatch); //Best one first.
        		
        		HashSet<String> all = plugin.getBanManager().matchAll(args[0]);
        		all.remove(bestMatch); //Don't want this name here twice.
        		results.addAll(all);
        	}
    	}
    	return results;
	}

	public abstract boolean run(CommandSender sender, Command cmd, String label, String[] args);
}
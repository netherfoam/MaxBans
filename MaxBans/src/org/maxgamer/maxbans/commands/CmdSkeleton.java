package org.maxgamer.maxbans.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.util.Formatter;

public abstract class CmdSkeleton implements CommandExecutor, TabCompleter, Comparable<CmdSkeleton>{
	private static ArrayList<CmdSkeleton> commands = new ArrayList<CmdSkeleton>();
	public static CmdSkeleton[] getCommands(){ return commands.toArray(new CmdSkeleton[commands.size()]); }
	
	public int compareTo(CmdSkeleton skelly){
		return this.cmd.getUsage().compareToIgnoreCase(skelly.cmd.getUsage());
	}
	
	/** A quick reference to the MaxBans plugin */
	protected MaxBans plugin = MaxBans.instance;
	/** The permission required to execute this command. This is done automatically. */
	protected String perm;
	/** The minimum number of arguments this command must have. If this isn't met, the usage is sent instead of being run. */
	protected int minArgs = 0;
	
	/** The position in the args[] array which is a name. Use less than 1 for doesn't take a name, or no tab autocomplete intended */
	protected int namePos = 1;
	
	/** The plugin command - This is null until the constructor is called. If it is null still, then MaxBans hasn't registered the command (Failed) */
	protected PluginCommand cmd;
	
	public CmdSkeleton(String command, String perm){
		this.perm = perm;
		cmd = plugin.getCommand(command);
		//cmd = Bukkit.getPluginCommand(command);
		//plugin.getLogger().info("Cmd registered to: " + cmd.getPlugin());
		if(cmd == null){
			//TODO: Handle this? Somehow it reports that the owner of the command was already me... though when executing it, other plugins are using it!
			/*
			if(plugin.getConfig().getBoolean("command-override", true)){
				plugin.getLogger().info("Stealing command: " + command);
				try{
					Field f = plugin.getServer().getPluginManager().getClass().getDeclaredField("commandMap");
					f.setAccessible(true);
					SimpleCommandMap map = (SimpleCommandMap) f.get(plugin.getServer().getPluginManager());
					Command steal = map.getCommand(command);
					steal.unregister(map);
					
					//cmd = Bukkit.getPluginCommand(command);
					cmd = plugin.getCommand(command);
					cmd.setExecutor(this);
					plugin.getLogger().info("Successfully stole command: " + command);
				}
				catch(Exception e){
					e.printStackTrace();
					plugin.getLogger().warning("Could not override command: " + command + "...");
				}
				
			}
			else{
				plugin.getLogger().warning("MaxBans does not have control of the command: " + command + "! This may cause issues!");
			}*/
		}
		else{
			//plugin.getLogger().info("Command " + command + " was already mine!");
			cmd.setExecutor(this);
		}
		commands.add(this);
	}
	
	public String getUsage(){
		return Formatter.secondary + cmd.getUsage();
	}
	public String getDescription(){
		return cmd.getDescription();
	}
	
	/** Shorthand method to check if the sender has permission */
	public boolean hasPermission(CommandSender sender){
		if(this.perm == null || this.perm.isEmpty()) return true;
		return sender.hasPermission(this.perm);
	}
	
	/**
	 * Checks the player has permission, if this command has a permission node,
	 * then checks there are enough arguments.  If both succeed, it runs this
	 * command.
	 */
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(hasPermission(sender) == false){
			sender.sendMessage(ChatColor.RED + "You don't have the permission: " + perm + " to do that.");
			return true;
		}
		
		if(minArgs > 0 && args.length < minArgs && getUsage() != null){
			sender.sendMessage(getUsage());
			return true;
		}
		
		try{
			return run(sender, cmd, label, args);
		}
		catch(Exception e){
			e.printStackTrace();
			sender.sendMessage(ChatColor.RED + "Something went wrong when executing the command: ");
			StringBuilder sb = new StringBuilder(args[0]); for(int i = 1; i < args.length; i++){ sb.append(" " + args[i]); }
			sender.sendMessage(ChatColor.RED + "/" + label + " " + sb.toString());
			sender.sendMessage(ChatColor.RED + "Exception: " + e.getClass().getSimpleName() + ": " + e.getMessage());
			sender.sendMessage(ChatColor.RED + "The remainder of the exception is in the console.");
			sender.sendMessage(ChatColor.RED + "Please report this to netherfoam: http://dev.bukkit.org/server-mods/maxbans and include the error in the console in your post.");
			return true;
		}
	}
	
	/**
	 * Helps autocomplete names in some commands.
	 * If this.namePos is less than 1, this method will not autocomplete.
	 * Additionally, you can override this if you want.
	 */
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
    	ArrayList<String> results = new ArrayList<String>();
    	
    	if(args.length == namePos){
    		String partial = args[namePos - 1];
    				
        	String bestMatch = plugin.getBanManager().match(partial);
        	if(bestMatch == null){
        		return results;
        	}
        	else{
        		results.add(bestMatch); //Best one first. Regardless of if they're offline.
        		
        		//Rest of the matches
        		for(Player p : Bukkit.getOnlinePlayers()){
        			String name = p.getName().toLowerCase();
        			if(name.equals(bestMatch)) continue;
        			if(name.startsWith(partial)){
        				results.add(name);
        			}
        		}
        	}
    	}
    	return results;
	}
	
	/** 
	 * Called when this command is run. You are guaranteed that<br/>
	 * If a permission was set, then this will only be run if the user has that permission.<br/>
	 * If minArgs was set, then there are atleast that many arguments in the command.<br/> 
	 * @param sender The CommandSender
	 * @param cmd The command
	 * @param label The label
	 * @param args The arguments for the command
	 * @return True if handled (Should always return true)
	 */
	public abstract boolean run(CommandSender sender, Command cmd, String label, String[] args);
}
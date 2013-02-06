package org.maxgamer.maxbans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.util.Formatter;

public class MBCommand extends CmdSkeleton{
    public MBCommand(){
    	super(null);
    	namePos = -1;
    }
    
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		sender.sendMessage(Formatter.primary + "MaxBans Commands:");
		if(sender.hasPermission("maxbans.ban")) sender.sendMessage(Formatter.secondary + "/ban "+Formatter.primary+"<player> [-s] <reason>");
		if(sender.hasPermission("maxbans.checkban")) sender.sendMessage(Formatter.secondary + "/checkban "+Formatter.primary+"<player>");
		if(sender.hasPermission("maxbans.checkip")) sender.sendMessage(Formatter.secondary + "/checkip "+Formatter.primary+"<player>");
		if(sender.hasPermission("maxbans.clearwarnings")) sender.sendMessage(Formatter.secondary + "/clearwarnings "+Formatter.primary+"<player>");
		if(sender.hasPermission("maxbans.dupeip")) sender.sendMessage(Formatter.secondary + "/dupeip "+Formatter.primary+"<player>");
		if(sender.hasPermission("maxbans.forcespawn")) sender.sendMessage(Formatter.secondary + "/forcespawn "+Formatter.primary+"<player>");
		if(sender.hasPermission("maxbans.ipban")) sender.sendMessage(Formatter.secondary + "/ipban "+Formatter.primary+"<player> [-s] <reason>");
		if(sender.hasPermission("maxbans.kick")) sender.sendMessage(Formatter.secondary + "/kick "+Formatter.primary+"<player> [-s] [reason]");
		if(sender.hasPermission("maxbans.lockdown.use")) sender.sendMessage(Formatter.secondary + "/lockdown "+Formatter.primary+"[reason]");
		if(sender.hasPermission("maxbans.mute")) sender.sendMessage(Formatter.secondary + "/mute "+Formatter.primary+"<player>");
		if(sender.hasPermission("maxbans.reload")) sender.sendMessage(Formatter.secondary + "/mbreload");
		if(sender.hasPermission("maxbans.tempban")) sender.sendMessage(Formatter.secondary + "/tempban "+Formatter.primary+"<player> <time> <time form> [-s] <reason>");
		if(sender.hasPermission("maxbans.tempipban")) sender.sendMessage(Formatter.secondary + "/tempipban "+Formatter.primary+"<player> <time> <timeform> [-s] <reason>");
		if(sender.hasPermission("maxbans.tempmute")) sender.sendMessage(Formatter.secondary + "/tempmute "+Formatter.primary+"<player> <time> <timeform>");
		if(sender.hasPermission("maxbans.unban")) sender.sendMessage(Formatter.secondary + "/unban "+Formatter.primary+"<player>");
		if(sender.hasPermission("maxbans.unmute")) sender.sendMessage(Formatter.secondary + "/unmute "+Formatter.primary+"<player>");
		if(sender.hasPermission("maxbans.warn")) sender.sendMessage(Formatter.secondary + "/warn "+Formatter.primary+"<player> <reason>");
		return true;
	}
}

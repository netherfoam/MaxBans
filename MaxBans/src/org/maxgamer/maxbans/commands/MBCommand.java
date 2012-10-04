package org.maxgamer.maxbans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.MaxBans;

public class MBCommand implements CommandExecutor{
    private MaxBans plugin;
    public MBCommand(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		sender.sendMessage(plugin.color_primary + "MaxBans Commands:");
		if(sender.hasPermission("maxbans.ban")) sender.sendMessage(plugin.color_secondary + "/ban "+plugin.color_primary+"<player> [-s] <reason>");
		if(sender.hasPermission("maxbans.checkban")) sender.sendMessage(plugin.color_secondary + "/checkban "+plugin.color_primary+"<player>");
		if(sender.hasPermission("maxbans.checkip")) sender.sendMessage(plugin.color_secondary + "/checkip "+plugin.color_primary+"<player>");
		if(sender.hasPermission("maxbans.clearwarnings")) sender.sendMessage(plugin.color_secondary + "/clearwarnings "+plugin.color_primary+"<player>");
		if(sender.hasPermission("maxbans.dupeip")) sender.sendMessage(plugin.color_secondary + "/dupeip "+plugin.color_primary+"<player>");
		if(sender.hasPermission("maxbans.forcespawn")) sender.sendMessage(plugin.color_secondary + "/forcespawn "+plugin.color_primary+"<player>");
		if(sender.hasPermission("maxbans.ipban")) sender.sendMessage(plugin.color_secondary + "/ipban "+plugin.color_primary+"<player> [-s] <reason>");
		if(sender.hasPermission("maxbans.kick")) sender.sendMessage(plugin.color_secondary + "/kick "+plugin.color_primary+"<player> [-s] [reason]");
		if(sender.hasPermission("maxbans.lockdown.use")) sender.sendMessage(plugin.color_secondary + "/lockdown "+plugin.color_primary+"[reason]");
		if(sender.hasPermission("maxbans.mute")) sender.sendMessage(plugin.color_secondary + "/mute "+plugin.color_primary+"<player>");
		if(sender.hasPermission("maxbans.reload")) sender.sendMessage(plugin.color_secondary + "/mbreload");
		if(sender.hasPermission("maxbans.tempban")) sender.sendMessage(plugin.color_secondary + "/tempban "+plugin.color_primary+"<player> <time> <time form> [-s] <reason>");
		if(sender.hasPermission("maxbans.tempipban")) sender.sendMessage(plugin.color_secondary + "/tempipban "+plugin.color_primary+"<player> <time> <timeform> [-s] <reason>");
		if(sender.hasPermission("maxbans.tempmute")) sender.sendMessage(plugin.color_secondary + "/tempmute "+plugin.color_primary+"<player> <time> <timeform>");
		if(sender.hasPermission("maxbans.unban")) sender.sendMessage(plugin.color_secondary + "/unban "+plugin.color_primary+"<player>");
		if(sender.hasPermission("maxbans.unmute")) sender.sendMessage(plugin.color_secondary + "/unmute "+plugin.color_primary+"<player>");
		if(sender.hasPermission("maxbans.warn")) sender.sendMessage(plugin.color_secondary + "/warn "+plugin.color_primary+"<player> <reason>");
		return true;
	}
}

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
		sender.sendMessage(plugin.color_primary + "MaxBans Commands");
		if(sender.hasPermission("maxbans.ban")) sender.sendMessage(plugin.color_secondary + "Usage: /ban <player> [-s] <reason>");
		if(sender.hasPermission("maxbans.checkban")) sender.sendMessage(plugin.color_secondary + "Usage: /checkban <player>");
		if(sender.hasPermission("maxbans.checkip")) sender.sendMessage(plugin.color_secondary + "Usage: /checkip <player>");
		if(sender.hasPermission("maxbans.clearwarnings")) sender.sendMessage(plugin.color_secondary + "Usage: /clearwarnings <player>");
		if(sender.hasPermission("maxbans.dupeip")) sender.sendMessage(plugin.color_secondary + "Usage: /dupeip <player>");
		if(sender.hasPermission("maxbans.forcespawn")) sender.sendMessage(plugin.color_secondary + "Usage: /forcespawn <player>");
		if(sender.hasPermission("maxbans.ipban")) sender.sendMessage(plugin.color_secondary + "Usage: /ipban <player> [-s] <reason>");
		if(sender.hasPermission("maxbans.kick")) sender.sendMessage(plugin.color_secondary + "Usage: /kick <player> [reason]");
		if(sender.hasPermission("maxbans.lockdown.use")) sender.sendMessage(plugin.color_secondary + "Usage: /lockdown [reason]");
		if(sender.hasPermission("maxbans.mute")) sender.sendMessage(plugin.color_secondary + "Usage: /mute <player>");
		if(sender.hasPermission("maxbans.reload")) sender.sendMessage(plugin.color_secondary + "Usage: /mbreload");
		if(sender.hasPermission("maxbans.tempban")) sender.sendMessage(plugin.color_secondary + "Usage: /tempban <player> <time> <time form> [-s] <reason>");
		if(sender.hasPermission("maxbans.tempipban")) sender.sendMessage(plugin.color_secondary + "Usage: /tempipban <player> <time> <timeform> [-s] <reason>");
		if(sender.hasPermission("maxbans.tempmute")) sender.sendMessage(plugin.color_secondary + "Usage: /tempmute <player> <time> <timeform>");
		if(sender.hasPermission("maxbans.unban")) sender.sendMessage(plugin.color_secondary + "Usage: /unban <player>");
		if(sender.hasPermission("maxbans.unmute")) sender.sendMessage(plugin.color_secondary + "Usage: /unmute <player>");
		if(sender.hasPermission("maxbans.warn")) sender.sendMessage(plugin.color_secondary + "Usage: /warn <player> <reason>");
		return true;
	}
}

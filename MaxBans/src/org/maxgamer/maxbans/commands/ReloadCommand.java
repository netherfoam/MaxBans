package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.util.Formatter;

public class ReloadCommand extends CmdSkeleton{
    public ReloadCommand(){
        super("mbreload", "maxbans.reload");
        namePos = -1;
    }
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		sender.sendMessage(Formatter.secondary + "Reloading MaxBans");
		Bukkit.getPluginManager().disablePlugin(plugin);
		Bukkit.getPluginManager().enablePlugin(plugin);
		sender.sendMessage(ChatColor.GREEN + "Reload Complete");
		return true;
	}
}

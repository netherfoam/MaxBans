package org.maxgamer.maxbans.commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.Bukkit;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class UUID extends CmdSkeleton
{
    public UUID() {
        super("uuid", "MaxBans.uuid");
        this.namePos = -1;
    }
    
    public boolean run(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        try {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "/UUID <Name>");
                return true;
            }
            String name = args[0];
            name = this.plugin.getBanManager().match(name);
            try {
                @SuppressWarnings("deprecation")
				final OfflinePlayer p = Bukkit.getOfflinePlayer(name);
                sender.sendMessage(ChatColor.GREEN + p.getName() + "'s UUID is " + p.getUniqueId().toString());
            }
            catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "No player by that name!");
            }
            return true;
        }
        catch (Exception e2) {
            sender.sendMessage(ChatColor.RED + "/UUID <Name>");
            return true;
        }
    }
}

package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.banmanager.RangeBan;
import org.maxgamer.maxbans.banmanager.TempRangeBan;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.IPAddress;
import org.maxgamer.maxbans.util.Util;

public class TempRangeBanCommand extends CmdSkeleton
{
    public TempRangeBanCommand() {
        super("temprangeban", "maxbans.temprangeban");
    }
    
    public boolean run(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (args.length < 3) {
            sender.sendMessage(this.getUsage());
            return true;
        }
        final String banner = Util.getName(sender);
        final boolean silent = Util.isSilent(args);
        String[] ips = args[0].split("-");
        if (ips.length == 1 && ips[0].contains("*")) {
            ips = new String[] { ips[0].replace('*', '0'), ips[0].replace("*", "255") };
        }
        else if (ips.length != 2) {
            sender.sendMessage(ChatColor.RED + "Not enough IP addresses supplied! Usage: " + this.getUsage());
            return true;
        }
        for (int i = 0; i < ips.length; ++i) {
            if (!Util.isIP(ips[i])) {
                sender.sendMessage(ChatColor.RED + ips[i] + " is not a valid IP address.");
                return true;
            }
        }
        final IPAddress start = new IPAddress(ips[0]);
        final IPAddress end = new IPAddress(ips[1]);
        final long expires = Util.getTime(args) + System.currentTimeMillis();
        if (expires <= 0L) {
            sender.sendMessage(this.getUsage());
            return true;
        }
        final String reason = Util.buildReason(args);
        final TempRangeBan rb = new TempRangeBan(banner, reason, System.currentTimeMillis(), expires, start, end);
        final RangeBan overlap = this.plugin.getBanManager().ban(rb);
        if (overlap == null) {
            this.plugin.getBanManager().announce(Formatter.secondary + banner + Formatter.primary + " banned " + Formatter.secondary + rb.toString() + Formatter.primary + ". Reason: '" + Formatter.secondary + reason + Formatter.primary + "' Remaining: " + Formatter.secondary + Util.getTimeUntil(rb.getExpires()), silent, sender);
            for (final Player p : Bukkit.getOnlinePlayers()) {
                if (rb.contains(new IPAddress(p.getAddress().getAddress().getHostAddress()))) {
                    p.kickPlayer(rb.getKickMessage());
                }
            }
        }
        else {
            sender.sendMessage(ChatColor.RED + "That RangeBan overlaps an existing one! (" + overlap.toString() + ")");
        }
        return true;
    }
}

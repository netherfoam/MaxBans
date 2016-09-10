package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.banmanager.RangeBan;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.IPAddress;
import org.maxgamer.maxbans.util.Util;

public class RangeBanCommand extends CmdSkeleton
{
    public RangeBanCommand() {
        super("rangeban", "maxbans.rangeban");
        this.minArgs = 1;
    }
    
    public boolean run(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        final String banner = Util.getName(sender);
        final boolean silent = Util.isSilent(args);
        final String reason = Util.buildReason(args);
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
        final RangeBan rb = new RangeBan(banner, reason, System.currentTimeMillis(), start, end);
        final RangeBan result = this.plugin.getBanManager().ban(rb);
        if (result != null) {
            sender.sendMessage(ChatColor.RED + "That RangeBan overlaps another RangeBan! (" + result.toString() + ")");
            return true;
        }
        this.plugin.getBanManager().announce(Formatter.secondary + banner + Formatter.primary + " RangeBanned " + Formatter.secondary + rb.toString() + Formatter.primary + ". Reason: " + Formatter.secondary + rb.getReason(), silent, sender);
        final String msg = Formatter.secondary + banner + Formatter.primary + " RangeBanned " + Formatter.secondary + rb.toString() + Formatter.primary + ". Reason: " + Formatter.secondary + rb.getReason();
        this.plugin.getBanManager().addHistory(rb.toString(), banner, msg);
        for (final Player p : Bukkit.getOnlinePlayers()) {
            if (rb.contains(new IPAddress(p.getAddress().getAddress().getHostAddress()))) {
                p.kickPlayer(rb.getKickMessage());
            }
        }
        return true;
    }
}

package org.maxgamer.maxbans.commands;

import java.util.HashSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.Msg;
import org.maxgamer.maxbans.banmanager.Ban;
import org.maxgamer.maxbans.banmanager.IPBan;
import org.maxgamer.maxbans.banmanager.Mute;
import org.maxgamer.maxbans.banmanager.RangeBan;
import org.maxgamer.maxbans.banmanager.Temporary;
import org.maxgamer.maxbans.banmanager.Warn;
import org.maxgamer.maxbans.util.DNSBL;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.IPAddress;
import org.maxgamer.maxbans.util.Util;

public class CheckBanCommand extends CmdSkeleton
{
    public CheckBanCommand() {
        super("checkban", "");
        this.namePos = 1;
    }
    
    public boolean run(final CommandSender sender, final Command cmd, final String label, String[] args) {
        if (!sender.hasPermission("maxbans.checkban")) {
            if (!sender.hasPermission("maxbans.checkban.self")) {
                sender.sendMessage(Msg.get("error.no-permission"));
                return true;
            }
            args = new String[] { ((Player)sender).getName() };
        }
        else if (args.length <= 0) {
            sender.sendMessage(ChatColor.RED + this.getUsage());
            return true;
        }
        String name = args[0];
        if (!Util.isIP(name)) {
            name = this.plugin.getBanManager().match(name);
            //final String ip = this.plugin.getBanManager().getIP(name);
            final Ban ban = this.plugin.getBanManager().getBan(name);
            final Mute mute = this.plugin.getBanManager().getMute(name);
            final boolean white = this.plugin.getBanManager().isWhitelisted(name);
            sender.sendMessage(Formatter.secondary + "+---------------------------------------------------+");
            sender.sendMessage(Formatter.primary + "User: " + Formatter.secondary + name);
            sender.sendMessage(Formatter.primary + "Banned: " + Formatter.secondary + ((ban == null) ? "False" : ("'" + ban.getReason() + Formatter.secondary + "' (" + ban.getBanner() + ")" + ((ban instanceof Temporary) ? (" Ends: " + Util.getShortTime(((Temporary)ban).getExpires() - System.currentTimeMillis())) : ""))));
            sender.sendMessage(Formatter.primary + "Muted: " + Formatter.secondary + ((mute == null) ? "False" : ("'" + mute.getReason() + Formatter.secondary + "' (" + mute.getBanner() + ")" + ((mute instanceof Temporary) ? (" Ends: " + Util.getShortTime(((Temporary)mute).getExpires() - System.currentTimeMillis())) : ""))));
            final List<Warn> warnings = this.plugin.getBanManager().getWarnings(name);
            if (warnings == null || warnings.isEmpty()) {
                sender.sendMessage(Formatter.primary + "Warnings: " + Formatter.secondary + "(0)");
            }
            else {
                sender.sendMessage(Formatter.primary + "Warnings: " + Formatter.secondary + "(" + warnings.size() + ")");
                for (final Warn w : warnings) {
                    sender.sendMessage(Formatter.secondary + "'" + w.getReason() + "' (" + w.getBanner() + ") Expires: " + Util.getShortTime(w.getExpires() - System.currentTimeMillis()));
                }
            }
            sender.sendMessage(Formatter.primary + "Whitelisted: " + Formatter.secondary + (white ? "True" : "False"));
        }
        else {
            final String ip = name;
            final IPBan ban2 = this.plugin.getBanManager().getIPBan(ip);
            final RangeBan rb = this.plugin.getBanManager().getBan(new IPAddress(ip));
            sender.sendMessage(Formatter.secondary + "+---------------------------------------------------+");
            sender.sendMessage(Formatter.primary + "IP: " + Formatter.secondary + ip);
            sender.sendMessage(Formatter.primary + "IP Banned: " + Formatter.secondary + ((ban2 == null) ? "False" : ("'" + ban2.getReason() + Formatter.secondary + "' (" + ban2.getBanner() + ")" + ((ban2 instanceof Temporary) ? (" Ends: " + Util.getShortTime(((Temporary)ban2).getExpires() - System.currentTimeMillis())) : ""))));
            sender.sendMessage(Formatter.primary + "RangeBan: " + Formatter.secondary + ((rb == null) ? "False" : (String.valueOf(rb.toString()) + " '" + rb.getReason() + Formatter.secondary + "' (" + rb.getBanner() + ")" + ((ban2 instanceof Temporary) ? (" Ends: " + Util.getShortTime(((Temporary)rb).getExpires() - System.currentTimeMillis())) : ""))));
            if (this.plugin.getBanManager().getDNSBL() != null) {
                final DNSBL.CacheRecord r = this.plugin.getBanManager().getDNSBL().getRecord(ip);
                if (r != null) {
                    sender.sendMessage(Formatter.primary + "Proxy: " + Formatter.secondary + ((r.getStatus() == DNSBL.DNSStatus.ALLOWED) ? "False" : "True"));
                }
            }
            final HashSet<String> dupeip = this.plugin.getBanManager().getUsers(ip);
            sender.sendMessage(Formatter.primary + "Users: " + Formatter.secondary + ((dupeip == null) ? "0" : dupeip.size()));
        }
        sender.sendMessage(Formatter.secondary + "+---------------------------------------------------+");
        return true;
    }
}

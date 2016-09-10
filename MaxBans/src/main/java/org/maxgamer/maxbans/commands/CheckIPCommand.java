package org.maxgamer.maxbans.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.banmanager.IPBan;
import org.maxgamer.maxbans.banmanager.RangeBan;
import org.maxgamer.maxbans.banmanager.Temporary;
import org.maxgamer.maxbans.util.DNSBL;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.IPAddress;
import org.maxgamer.maxbans.util.Util;

public class CheckIPCommand extends CmdSkeleton
{
    public CheckIPCommand() {
        super("checkip", "maxbans.checkip");
    }
    
    public boolean run(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (args.length > 0) {
            String name = args[0];
            String ip;
            if (Util.isIP(args[0])) {
                ip = args[0];
            }
            else {
                name = this.plugin.getBanManager().match(name);
                if (name == null) {
                    name = args[0];
                }
                ip = this.plugin.getBanManager().getIP(name);
                if (ip == null) {
                    sender.sendMessage(Formatter.primary + "That player has no IP history!");
                    return true;
                }
            }
            final IPBan ban = this.plugin.getBanManager().getIPBan(ip);
            final RangeBan rb = this.plugin.getBanManager().getBan(new IPAddress(ip));
            sender.sendMessage(Formatter.secondary + "+---------------------------------------------------+");
            sender.sendMessage(Formatter.primary + "IP: " + Formatter.secondary + ip);
            sender.sendMessage(Formatter.primary + "IP Banned: " + Formatter.secondary + ((ban == null) ? "False" : ("'" + ban.getReason() + Formatter.secondary + "' (" + ban.getBanner() + ")" + ((ban instanceof Temporary) ? (" Ends: " + Util.getShortTime(((Temporary)ban).getExpires() - System.currentTimeMillis())) : ""))));
            sender.sendMessage(Formatter.primary + "RangeBan: " + Formatter.secondary + ((rb == null) ? "False" : (String.valueOf(rb.toString()) + " '" + rb.getReason() + Formatter.secondary + "' (" + rb.getBanner() + ")" + ((ban instanceof Temporary) ? (" Ends: " + Util.getShortTime(((Temporary)rb).getExpires() - System.currentTimeMillis())) : ""))));
            if (this.plugin.getBanManager().getDNSBL() != null) {
                final DNSBL.CacheRecord r = this.plugin.getBanManager().getDNSBL().getRecord(ip);
                if (r != null) {
                    sender.sendMessage(Formatter.primary + "Proxy: " + Formatter.secondary + ((r.getStatus() == DNSBL.DNSStatus.ALLOWED) ? "False" : "True"));
                }
            }
            final HashSet<String> dupeip = this.plugin.getBanManager().getUsers(ip);
            final ArrayList<UUID> ids = new ArrayList<UUID>();
            for (final String s : dupeip) {
                try {
                    @SuppressWarnings("deprecation")
					final OfflinePlayer p = Bukkit.getOfflinePlayer(s);
                    if (ids.contains(p.getUniqueId())) {
                        continue;
                    }
                    ids.add(p.getUniqueId());
                }
                catch (Exception ex) {}
            }
            sender.sendMessage(Formatter.primary + "Users: " + Formatter.secondary + ((dupeip == null) ? "0" : dupeip.size()));
            sender.sendMessage(Formatter.primary + "Unique IDs: " + Formatter.secondary + ((dupeip == null) ? "0" : dupeip.size()));
            sender.sendMessage(Formatter.primary + "GeoIP: " + Formatter.secondary + "http://www.geoiptool.com/en/?IP=" + ip);
            if (this.plugin.getGeoDB() != null) {
                sender.sendMessage(Formatter.primary + "Country: " + Formatter.secondary + this.plugin.getGeoDB().getCountry(ip));
            }
            sender.sendMessage(Formatter.secondary + "+---------------------------------------------------+");
            return true;
        }
        sender.sendMessage(this.getUsage());
        return true;
    }
}

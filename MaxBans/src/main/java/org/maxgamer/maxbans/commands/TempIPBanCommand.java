package org.maxgamer.maxbans.commands;

import org.maxgamer.maxbans.banmanager.IPBan;
import org.maxgamer.maxbans.banmanager.TempIPBan;
import org.maxgamer.maxbans.Msg;
import org.maxgamer.maxbans.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class TempIPBanCommand extends CmdSkeleton
{
    public TempIPBanCommand() {
        super("tempipban", "maxbans.tempipban");
    }
    
    public boolean run(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (args.length <= 2) {
            sender.sendMessage(this.getUsage());
            return true;
        }
        final boolean silent = Util.isSilent(args);
        String name = args[0];
        if (name.isEmpty()) {
            sender.sendMessage(Msg.get("error.no-player-given"));
            return true;
        }
        long time = Util.getTime(args);
        if (time <= 0L) {
            sender.sendMessage(this.getUsage());
            return true;
        }
        time += System.currentTimeMillis();
        final String reason = Util.buildReason(args);
        final String banner = Util.getName(sender);
        String ip;
        if (!Util.isIP(name)) {
            name = this.plugin.getBanManager().match(name);
            if (name == null) {
                name = args[0];
            }
            ip = this.plugin.getBanManager().getIP(name);
            if (ip == null) {
                final String msg = Msg.get("error.no-ip-known");
                sender.sendMessage(msg);
                return true;
            }
            this.plugin.getBanManager().tempban(name, reason, banner, time);
        }
        else {
            ip = name;
        }
        final IPBan ban = this.plugin.getBanManager().getIPBan(ip);
        if (ban != null) {
            if (!(ban instanceof TempIPBan)) {
                final String msg2 = Msg.get("error.tempipban-shorter-than-last");
                sender.sendMessage(msg2);
                return true;
            }
            final TempIPBan tBan = (TempIPBan)ban;
            if (tBan.getExpires() > time) {
                final String msg3 = Msg.get("error.tempipban-shorter-than-last");
                sender.sendMessage(msg3);
                return true;
            }
            this.plugin.getBanManager().unbanip(ip);
        }
        this.plugin.getBanManager().tempipban(ip, reason, banner, time);
        final String message = Msg.get("announcement.player-was-tempipbanned", new String[] { "banner", "name", "reason", "ip", "time" }, new String[] { banner, name, reason, ip, Util.getTimeUntil(time) });
        this.plugin.getBanManager().announce(message, silent, sender);
        return true;
    }
}

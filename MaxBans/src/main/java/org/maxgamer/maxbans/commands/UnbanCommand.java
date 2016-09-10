package org.maxgamer.maxbans.commands;

import org.maxgamer.maxbans.banmanager.Ban;
import org.maxgamer.maxbans.banmanager.IPBan;
import org.maxgamer.maxbans.Msg;
import org.maxgamer.maxbans.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class UnbanCommand extends CmdSkeleton
{
    public UnbanCommand() {
        super("unban", "maxbans.unban");
    }
    
    public boolean run(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (args.length <= 0) {
            sender.sendMessage(this.getUsage());
            return true;
        }
        final boolean silent = Util.isSilent(args);
        final String banner = Util.getName(sender);
        String name = args[0];
        if (name.isEmpty()) {
            sender.sendMessage(Msg.get("error.no-player-given"));
            return true;
        }
        if (Util.isIP(name)) {
            final String ip = name;
            final IPBan ipban = this.plugin.getBanManager().getIPBan(ip);
            if (ipban != null) {
                this.plugin.getBanManager().unbanip(ip);
                final String msg = Msg.get("announcement.player-was-unbanned", new String[] { "banner", "name" }, new String[] { banner, name });
                this.plugin.getBanManager().announce(msg, silent, sender);
                this.plugin.getBanManager().addHistory(name, banner, msg);
            }
            else {
                final String msg = Msg.get("error.no-ban-found", "name", ip);
                sender.sendMessage(msg);
            }
            return true;
        }
        name = this.plugin.getBanManager().match(name, true);
        final String ip = this.plugin.getBanManager().getIP(name);
        final Ban ban = this.plugin.getBanManager().getBan(name);
        final IPBan ipban2 = this.plugin.getBanManager().getIPBan(ip);
        if (ipban2 == null && ban == null) {
            final String msg2 = Msg.get("error.no-ban-found", "name", name);
            sender.sendMessage(msg2);
            return true;
        }
        if (ban != null) {
            this.plugin.getBanManager().unban(name);
        }
        if (ipban2 != null) {
            this.plugin.getBanManager().unbanip(ip);
        }
        final String message = Msg.get("announcement.player-was-unbanned", new String[] { "banner", "name" }, new String[] { banner, name });
        this.plugin.getBanManager().announce(message, silent, sender);
        this.plugin.getBanManager().addHistory(name, banner, message);
        return true;
    }
}

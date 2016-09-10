package org.maxgamer.maxbans.commands;

import org.maxgamer.maxbans.banmanager.IPBan;
import org.maxgamer.maxbans.banmanager.Ban;
import org.maxgamer.maxbans.banmanager.TempIPBan;
import org.maxgamer.maxbans.banmanager.TempBan;
import org.maxgamer.maxbans.Msg;
import org.maxgamer.maxbans.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class BanCommand extends CmdSkeleton
{
    public BanCommand() {
        super("ban", "maxbans.ban");
    }
    
    public boolean run(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (args.length <= 0) {
            sender.sendMessage(this.getUsage());
            return true;
        }
        final boolean silent = Util.isSilent(args);
        String name = args[0];
        if (name.isEmpty()) {
            sender.sendMessage(Msg.get("error.no-player-given"));
            return true;
        }
        final String reason = Util.buildReason(args);
        final String banner = Util.getName(sender);
        final String message = Msg.get("announcement.player-was-banned", new String[] { "banner", "name", "reason" }, new String[] { banner, name, reason });
        if (!Util.isIP(name)) {
            name = this.plugin.getBanManager().match(name);
            if (name == null) {
                name = args[0];
            }
            final Ban ban = this.plugin.getBanManager().getBan(name);
            if (ban != null && !(ban instanceof TempBan)) {
                sender.sendMessage(Msg.get("error.player-already-banned"));
                return true;
            }
            this.plugin.getBanManager().ban(name, reason, banner);
        }
        else {
            final IPBan ipban = this.plugin.getBanManager().getIPBan(name);
            if (ipban != null && !(ipban instanceof TempIPBan)) {
                sender.sendMessage(Msg.get("error.ip-already-banned"));
                return true;
            }
            this.plugin.getBanManager().ipban(name, reason, banner);
        }
        this.plugin.getBanManager().announce(message, silent, sender);
        this.plugin.getBanManager().addHistory(name, banner, message);
        return true;
    }
}

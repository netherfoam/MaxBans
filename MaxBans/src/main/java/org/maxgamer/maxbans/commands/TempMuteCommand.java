package org.maxgamer.maxbans.commands;

import org.maxgamer.maxbans.banmanager.Mute;
import org.maxgamer.maxbans.banmanager.TempMute;
import org.maxgamer.maxbans.Msg;
import org.maxgamer.maxbans.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class TempMuteCommand extends CmdSkeleton
{
    public TempMuteCommand() {
        super("tempmute", "maxbans.tempmute");
    }
    
    public boolean run(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (args.length <= 2) {
            sender.sendMessage(this.getUsage());
            return true;
        }
        String name = args[0];
        name = this.plugin.getBanManager().match(name);
        if (name == null) {
            name = args[0];
        }
        final boolean silent = Util.isSilent(args);
        if (name.isEmpty()) {
            sender.sendMessage(Msg.get("error.no-player-given"));
            return true;
        }
        final String banner = Util.getName(sender);
        long time = Util.getTime(args);
        if (time <= 0L) {
            sender.sendMessage(this.getUsage());
            return true;
        }
        time += System.currentTimeMillis();
        final Mute mute = this.plugin.getBanManager().getMute(name);
        if (mute != null) {
            if (!(mute instanceof TempMute)) {
                final String msg = Msg.get("tempmute-shorter-than-last");
                sender.sendMessage(msg);
                return true;
            }
            final TempMute tMute = (TempMute)mute;
            if (tMute.getExpires() > time) {
                final String msg2 = Msg.get("tempmute-shorter-than-last");
                sender.sendMessage(msg2);
                return true;
            }
        }
        final String reason = Util.buildReason(args);
        this.plugin.getBanManager().tempmute(name, banner, reason, time);
        final String until = Util.getTimeUntil(time);
        final String message = Msg.get("announcement.player-was-temp-muted", new String[] { "banner", "name", "time", "reason" }, new String[] { banner, name, until, reason });
        this.plugin.getBanManager().addHistory(name, banner, message);
        this.plugin.getBanManager().announce(message, silent, sender);
        return true;
    }
}

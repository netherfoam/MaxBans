package org.maxgamer.maxbans.commands;

import org.maxgamer.maxbans.banmanager.Mute;
import org.bukkit.Bukkit;
import org.maxgamer.maxbans.Msg;
import org.maxgamer.maxbans.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class MuteCommand extends CmdSkeleton
{
    public MuteCommand() {
        super("mute", "maxbans.mute");
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
        name = this.plugin.getBanManager().match(name);
        if (name == null) {
            name = args[0];
        }
        final Mute mute = this.plugin.getBanManager().getMute(name);
        if (mute != null) {
            Bukkit.dispatchCommand(sender, "unmute");
            return true;
        }
        final String reason = Util.buildReason(args);
        final String banner = Util.getName(sender);
        this.plugin.getBanManager().mute(name, banner, reason);
        final String message = Msg.get("announcement.player-was-muted", new String[] { "banner", "name", "reason" }, new String[] { banner, name, reason });
        this.plugin.getBanManager().announce(message, silent, sender);
        this.plugin.getBanManager().addHistory(name, banner, message);
        return true;
    }
}

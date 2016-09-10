package org.maxgamer.maxbans.commands;

import org.maxgamer.maxbans.Msg;
import org.maxgamer.maxbans.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class WarnCommand extends CmdSkeleton
{
    public WarnCommand() {
        super("warn", "maxbans.warn");
    }
    
    public boolean run(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (args.length <= 1) {
            sender.sendMessage(this.getUsage());
            return true;
        }
        final boolean silent = Util.isSilent(args);
        String name = args[0];
        name = this.plugin.getBanManager().match(name);
        if (name == null) {
            name = args[0];
        }
        if (name.isEmpty()) {
            sender.sendMessage(Msg.get("error.no-player-given"));
            return true;
        }
        final String reason = Util.buildReason(args);
        final String banner = Util.getName(sender);
        this.plugin.getBanManager().warn(name, reason, banner);
        final String msg = Msg.get("announcement.player-was-warned", new String[] { "banner", "name", "reason" }, new String[] { banner, name, reason });
        this.plugin.getBanManager().announce(msg, silent, sender);
        this.plugin.getBanManager().addHistory(name, banner, msg);
        return true;
    }
}

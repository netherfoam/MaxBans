package org.maxgamer.maxbans.commands;

import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class WhitelistCommand extends CmdSkeleton
{
    public WhitelistCommand() {
        super("mbwhitelist", "maxbans.whitelist");
        this.minArgs = 1;
        this.namePos = 1;
    }
    
    public boolean run(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        final String name = this.plugin.getBanManager().match(args[0]);
        final String banner = Util.getName(sender);
        final boolean on = !this.plugin.getBanManager().isWhitelisted(name);
        this.plugin.getBanManager().setWhitelisted(name, on);
        String msg = Formatter.secondary + name + Formatter.primary + " is " + Formatter.secondary + (on ? ("whitelisted" + Formatter.primary + " and may ") : ("no longer whitelisted" + Formatter.primary + " and may not ")) + "bypass any IP/Range/DNSBL bans.";
        sender.sendMessage(msg);
        msg = Formatter.secondary + name + Formatter.primary + " was " + (on ? "whitelisted" : "unwhitelisted") + " by " + Formatter.secondary + banner;
        this.plugin.getBanManager().addHistory(name, banner, msg);
        return true;
    }
}

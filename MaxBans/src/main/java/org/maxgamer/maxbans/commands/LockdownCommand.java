package org.maxgamer.maxbans.commands;

import org.maxgamer.maxbans.util.Formatter;
import java.text.ParseException;
import org.maxgamer.maxbans.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class LockdownCommand extends CmdSkeleton
{
    private static String defaultReason;
    
    static {
        LockdownCommand.defaultReason = "Maintenance";
    }
    
    public LockdownCommand() {
        super("lockdown", "maxbans.lockdown.use");
        this.namePos = -1;
    }
    
    public boolean run(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        boolean on;
        String reason;
        if (args.length > 0) {
            try {
                on = Util.parseBoolean(args[0]);
                args[0] = "";
            }
            catch (ParseException e) {
                on = !this.plugin.getBanManager().isLockdown();
            }
            final StringBuilder sb = new StringBuilder();
            for (final String s : args) {
                if (!s.isEmpty()) {
                    sb.append(String.valueOf(s) + " ");
                }
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            reason = sb.toString();
            if (reason.isEmpty()) {
                reason = LockdownCommand.defaultReason;
            }
        }
        else {
            on = !this.plugin.getBanManager().isLockdown();
            reason = LockdownCommand.defaultReason;
        }
        this.plugin.getBanManager().setLockdown(on, reason);
        sender.sendMessage(Formatter.secondary + "Lockdown is now " + (on ? ("enabled. Reason: " + Formatter.primary + this.plugin.getBanManager().getLockdownReason() + Formatter.secondary) : "disabled") + ".");
        final String banner = Util.getName(sender);
        this.plugin.getBanManager().addHistory(banner, banner, String.valueOf(banner) + " set lockdown: " + this.plugin.getBanManager().isLockdown());
        return true;
    }
}

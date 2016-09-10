package org.maxgamer.maxbans.commands;

import org.maxgamer.maxbans.banmanager.HistoryRecord;
import org.maxgamer.maxbans.util.Util;
import org.maxgamer.maxbans.util.Formatter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class HistoryCommand extends CmdSkeleton
{
    public HistoryCommand() {
        super("history", "maxbans.history");
        this.namePos = -1;
    }
    
    public boolean run(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        int count = 20;
        String name = null;
        if (args.length > 0) {
            try {
                count = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException e) {
                name = this.plugin.getBanManager().match(args[0]);
                if (args.length > 1) {
                    try {
                        count = Integer.parseInt(args[1]);
                    }
                    catch (NumberFormatException ex) {
                        sender.sendMessage(Formatter.secondary + this.getUsage());
                        return true;
                    }
                }
            }
        }
        HistoryRecord[] history;
        if (name == null) {
            history = this.plugin.getBanManager().getHistory();
        }
        else {
            history = this.plugin.getBanManager().getHistory(name);
        }
        if (history.length <= 0) {
            sender.sendMessage(Formatter.primary + "No history.");
        }
        else {
            for (int i = Math.min(history.length, count) - 1; i >= 0; --i) {
                sender.sendMessage(Formatter.primary + "[" + Util.getShortTime(System.currentTimeMillis() - history[i].getCreated()) + "] " + Formatter.secondary + history[i].getMessage());
            }
        }
        return true;
    }
}

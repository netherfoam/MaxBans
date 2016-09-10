package org.maxgamer.maxbans.commands;

import org.maxgamer.maxbans.banmanager.Warn;
import java.util.List;
import org.maxgamer.maxbans.Msg;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ClearWarningsCommand extends CmdSkeleton
{
    public ClearWarningsCommand() {
        super("clearwarnings", "maxbans.clearwarnings");
    }
    
    public boolean run(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (args.length <= 0) {
            sender.sendMessage(this.getUsage());
            return true;
        }
        String name = args[0];
        name = this.plugin.getBanManager().match(name);
        if (name == null) {
            name = args[0];
        }
        String banner;
        if (sender instanceof Player) {
            banner = ((Player)sender).getName();
        }
        else {
            banner = "Console";
        }
        final List<Warn> warnings = this.plugin.getBanManager().getWarnings(name);
        if (warnings == null || warnings.size() == 0) {
            sender.sendMessage(Msg.get("error.no-warnings", new String[] { "name" }, new String[] { name }));
            return true;
        }
        this.plugin.getBanManager().clearWarnings(name);
        final String message = Msg.get("announcement.player-warnings-cleared", new String[] { "banner", "name" }, new String[] { banner, name });
        this.plugin.getBanManager().announce(message);
        this.plugin.getBanManager().addHistory(name, banner, message);
        return true;
    }
}

package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.Msg;
import org.maxgamer.maxbans.util.Util;

public class KickCommand extends CmdSkeleton
{
    public KickCommand() {
        super("kick", "maxbans.kick");
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
        String banner;
        if (sender instanceof Player) {
            banner = ((Player)sender).getName();
        }
        else {
            banner = "Console";
        }
        if (name.equals("*") && sender.hasPermission("maxbans.kick.all")) {
            for (final Player p : Bukkit.getOnlinePlayers()) {
                final String message = Msg.get("disconnection.you-were-kicked", new String[] { "banner", "reason" }, new String[] { banner, reason });
                this.plugin.getBanManager().kick(p.getName(), message);
            }
            final String message2 = Msg.get("announcement.player-was-kicked", new String[] { "name", "banner", "reason" }, new String[] { "everyone", banner, reason });
            this.plugin.getBanManager().announce(message2, silent, sender);
            this.plugin.getBanManager().addHistory(name, banner, message2);
            return true;
        }
        if (Util.isIP(name)) {
            String message2 = Msg.get("disconnection.you-were-kicked", new String[] { "banner", "reason" }, new String[] { banner, reason });
            this.plugin.getBanManager().kickIP(name, message2);
            message2 = Msg.get("announcement.player-was-kicked", new String[] { "name", "banner", "reason" }, new String[] { name, banner, reason });
            this.plugin.getBanManager().announce(message2, silent, sender);
            this.plugin.getBanManager().addHistory(name, banner, message2);
            return true;
        }
        Player p = Bukkit.getPlayer(name);
        if (p != null) {
            name = p.getName().toLowerCase();
            String message3 = Msg.get("disconnection.you-were-kicked", new String[] { "banner", "reason" }, new String[] { banner, reason });
            this.plugin.getBanManager().kick(name, message3);
            message3 = Msg.get("announcement.player-was-kicked", new String[] { "name", "banner", "reason" }, new String[] { name, banner, reason });
            this.plugin.getBanManager().announce(message3, silent, sender);
            this.plugin.getBanManager().addHistory(name, banner, message3);
        }
        else {
            final String message3 = Msg.get("error.unknown-player", new String[] { "name" }, new String[] { name });
            sender.sendMessage(message3);
        }
        return true;
    }
}

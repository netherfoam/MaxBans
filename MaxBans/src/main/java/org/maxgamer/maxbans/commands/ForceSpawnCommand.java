package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.maxgamer.maxbans.util.Formatter;

public class ForceSpawnCommand extends CmdSkeleton
{
    public ForceSpawnCommand() {
        super("forcespawn", "maxbans.forcespawn");
    }
    
    public boolean run(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (args.length > 0) {
            final String name = args[0];
            String banner;
            if (sender instanceof Player) {
                banner = ((Player)sender).getName();
            }
            else {
                banner = "Console";
            }
            final Player p = Bukkit.getPlayer(name);
            if (p != null) {
                final PlayerRespawnEvent e = new PlayerRespawnEvent(p, Bukkit.getWorlds().get(0).getSpawnLocation(), false);
                Bukkit.getPluginManager().callEvent((Event)e);
                final Location spawn = e.getRespawnLocation();
                p.teleport(spawn, PlayerTeleportEvent.TeleportCause.PLUGIN);
                p.teleport(spawn, PlayerTeleportEvent.TeleportCause.PLUGIN);
                p.sendMessage(Formatter.secondary + "Forced to the spawn by " + banner);
                sender.sendMessage(Formatter.primary + "Forced " + Formatter.secondary + p.getName() + Formatter.primary + " to the spawn.");
            }
            else {
                sender.sendMessage(Formatter.primary + "No player found: " + Formatter.secondary + name);
            }
            return true;
        }
        sender.sendMessage(this.getUsage());
        return true;
    }
}

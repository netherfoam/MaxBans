package org.maxgamer.maxbans.commands;

import java.util.HashSet;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.Util;

public class DupeIPCommand extends CmdSkeleton
{
    private static ChatColor banned;
    private static ChatColor online;
    private static ChatColor offline;
    
    static {
        DupeIPCommand.banned = ChatColor.RED;
        DupeIPCommand.online = ChatColor.GREEN;
        DupeIPCommand.offline = ChatColor.GRAY;
    }
    
    public DupeIPCommand() {
        super("dupeip", "maxbans.dupeip");
    }
    
    public boolean run(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (args.length > 0) {
            String name = args[0];
            String ip;
            if (!Util.isIP(name)) {
                name = this.plugin.getBanManager().match(name);
                if (name == null) {
                    name = args[0];
                }
                ip = this.plugin.getBanManager().getIP(name);
                if (ip == null) {
                    sender.sendMessage(Formatter.primary + "Player " + Formatter.secondary + name + Formatter.primary + " has no IP history.");
                    return true;
                }
            }
            else {
                ip = name;
            }
            try {
                @SuppressWarnings("deprecation")
				final OfflinePlayer pl = Bukkit.getOfflinePlayer(name);
                if (sender instanceof Player) {
                    final Player p = (Player)sender;
                    final String[] str = getScanningString(name, ip).split("\\|");
                    final HoverEvent ev = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(new StringBuilder().append(pl.getUniqueId()).toString()).create());
                    //final HoverEvent Null = new HoverEvent((HoverEvent.Action)null, (BaseComponent[])null);
                    final ComponentBuilder message = new ComponentBuilder(str[0].replace("\\|", "")).append(" (UUID)").color(net.md_5.bungee.api.ChatColor.GRAY).event(ev).append(str[1].replace("\\|", "")).color(net.md_5.bungee.api.ChatColor.RESET);
                    p.spigot().sendMessage(message.create());
                }
                else {
                    sender.sendMessage("His UUID is " + pl.getUniqueId());
                }
            }
            catch (NullPointerException e1) {
                sender.sendMessage("Player has never played!");
            }
            final StringBuilder sb = new StringBuilder();
            final HashSet<String> dupes = this.plugin.getBanManager().getUsers(ip);
            if (dupes != null) {
                for (final String dupe : dupes) {
                    if (dupe.equalsIgnoreCase(name)) {
                        continue;
                    }
                    sb.append(String.valueOf(getChatColor(dupe).toString()) + dupe + ", ");
                }
            }
            if (sb.length() > 0) {
                sb.replace(sb.length() - 2, sb.length(), "");
                sender.sendMessage(sb.toString());
            }
            else {
                sender.sendMessage(Formatter.primary + "No duplicates!");
            }
            return true;
        }
        sender.sendMessage(this.getUsage());
        return true;
    }
    
    public static String getScanningString(final String name, final String ip) {
        return Formatter.primary + "Scanning " + getChatColor(name) + name + Formatter.primary + "| on " + Formatter.secondary + ip + ChatColor.WHITE + ".  [" + DupeIPCommand.banned + "Banned" + ChatColor.WHITE + "] [" + DupeIPCommand.online + "Online" + ChatColor.WHITE + "] [" + DupeIPCommand.offline + "Offline" + ChatColor.WHITE + "]";
    }
    
    public static ChatColor getChatColor(final String name) {
        if (Util.isIP(name)) {
            if (MaxBans.instance.getBanManager().getIPBan(name) != null) {
                return DupeIPCommand.banned;
            }
            return DupeIPCommand.offline;
        }
        else {
            if (MaxBans.instance.getBanManager().getBan(name) != null) {
                return DupeIPCommand.banned;
            }
            if (Bukkit.getPlayerExact(name) != null) {
                return DupeIPCommand.online;
            }
            return DupeIPCommand.offline;
        }
    }
}

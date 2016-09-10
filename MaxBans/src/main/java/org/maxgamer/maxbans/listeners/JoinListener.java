package org.maxgamer.maxbans.listeners;

import org.maxgamer.maxbans.banmanager.Ban;
import org.maxgamer.maxbans.banmanager.RangeBan;
import org.maxgamer.maxbans.banmanager.IPBan;
import org.maxgamer.maxbans.sync.Packet;
import org.maxgamer.maxbans.banmanager.Temporary;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.IPAddress;
import org.maxgamer.maxbans.util.Util;
import java.io.IOException;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import org.maxgamer.maxbans.MaxBans;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.ChatColor;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.Plugin;
import java.util.HashSet;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.maxgamer.maxbans.commands.DupeIPCommand;
import org.bukkit.event.player.PlayerLoginEvent;

public class JoinListener extends ListenerSkeleton
{
    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoinDupeip(final PlayerLoginEvent e) {
        if (!this.getPlugin().getConfig().getBoolean("auto-dupeip") || e.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            return;
        }
        final Runnable r = new Runnable() {
            public void run() {
                final HashSet<String> dupes = JoinListener.this.getPlugin().getBanManager().getUsers(e.getAddress().getHostAddress());
                if (dupes == null) {
                    return;
                }
                dupes.remove(e.getPlayer().getName().toLowerCase());
                if (dupes.isEmpty()) {
                    return;
                }
                final StringBuilder sb = new StringBuilder();
                for (final String dupe : dupes) {
                    sb.append(String.valueOf(DupeIPCommand.getChatColor(dupe).toString()) + dupe + ", ");
                }
                sb.replace(sb.length() - 2, sb.length(), "");
                for (final Player p : Bukkit.getOnlinePlayers()) {
                    if (p.hasPermission("maxbans.notify")) {
                        p.sendMessage(DupeIPCommand.getScanningString(e.getPlayer().getName().toLowerCase(), e.getAddress().getHostAddress()));
                        p.sendMessage(sb.toString());
                    }
                }
            }
        };
        if (this.getPlugin().isBungee()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)this.getPlugin(), r);
        }
        else {
            r.run();
        }
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onJoinLockdown(final PlayerLoginEvent event) {
        final Player player = event.getPlayer();
        if (this.getPlugin().getBanManager().isLockdown()) {
            if (!player.hasPermission("maxbans.lockdown.bypass")) {
                event.setKickMessage("Server is in lockdown mode. Try again shortly. Reason: \n" + this.getPlugin().getBanManager().getLockdownReason());
                event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                return;
            }
            final String name = player.getName();
            Bukkit.getScheduler().runTaskLaterAsynchronously((Plugin)this.getPlugin(), (Runnable)new Runnable() {
                public void run() {
                    final Player p = Bukkit.getPlayerExact(name);
                    if (p != null) {
                        p.sendMessage(ChatColor.RED + "Bypassing lockdown (" + JoinListener.this.getPlugin().getBanManager().getLockdownReason() + ")!");
                    }
                }
            }, 40L);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEnter(final PlayerJoinEvent e) {
        if (MaxBans.instance.isBungee()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)MaxBans.instance, (Runnable)new Runnable() {
                public void run() {
                    final ByteArrayOutputStream b = new ByteArrayOutputStream();
                    final DataOutputStream out = new DataOutputStream(b);
                    try {
                        out.writeUTF("IP");
                    }
                    catch (IOException ex) {}
                    e.getPlayer().sendPluginMessage((Plugin)MaxBans.instance, "BungeeCord", b.toByteArray());
                }
            });
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoinHandler(final PlayerLoginEvent e) {
        final Player player = e.getPlayer();
        if (this.getPlugin().getBanManager().hasImmunity(player.getName())) {
            return;
        }
        if (this.getPlugin().filter_names) {
            final String invalidChars = Util.getInvalidChars(player.getName());
            if (!invalidChars.isEmpty()) {
                e.setKickMessage("Kicked by MaxBans.\nYour name contains invalid characters:\n'" + invalidChars + "'");
                e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                return;
            }
            if (player.getName().isEmpty()) {
                e.setKickMessage("Kicked by MaxBans.\nYour name is invalid!");
                e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                return;
            }
        }
        if (!MaxBans.instance.isBungee()) {
            this.getPlugin().getBanManager().logIP(player.getName(), e.getAddress().getHostAddress());
        }
        final String address = this.getPlugin().getBanManager().getIP(player.getName());
        final boolean whitelisted = this.getPlugin().getBanManager().isWhitelisted(player.getName());
        if (!whitelisted && address != null && !this.getPlugin().isBungee()) {
            final IPBan ipban = this.getPlugin().getBanManager().getIPBan(address);
            if (ipban != null) {
                e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                e.setKickMessage(ipban.getKickMessage());
                return;
            }
            final IPAddress ip = new IPAddress(address);
            final RangeBan rb = this.getPlugin().getBanManager().getBan(ip);
            if (rb != null) {
                e.disallow(PlayerLoginEvent.Result.KICK_OTHER, rb.getKickMessage());
                if (this.getPlugin().getConfig().getBoolean("notify", true)) {
                    final String msg = Formatter.secondary + player.getName() + Formatter.primary + " (" + ChatColor.RED + address + Formatter.primary + ")" + " tried to join, but is " + ((rb instanceof Temporary) ? "temp " : "") + "RangeBanned.";
                    for (final Player p : Bukkit.getOnlinePlayers()) {
                        if (p.hasPermission("maxbans.notify")) {
                            p.sendMessage(msg);
                        }
                    }
                }
                return;
            }
            if (this.getPlugin().getBanManager().getDNSBL() != null) {
                this.getPlugin().getBanManager().getDNSBL().handle(e);
            }
        }
        if (this.getPlugin().getBanManager().logActual(player.getName(), player.getName()) && this.getPlugin().getSyncer() != null) {
            final Packet nameUpdate = new Packet().setCommand("setname").put("name", player.getName());
            this.getPlugin().getSyncer().broadcast(nameUpdate);
        }
        final Ban ban = this.getPlugin().getBanManager().getBan(player.getName());
        if (ban == null) {
            return;
        }
        e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
        e.setKickMessage(ban.getKickMessage());
        if (this.getPlugin().getConfig().getBoolean("notify", true)) {
            final String msg2 = ((ban == null) ? Formatter.secondary : ChatColor.RED) + player.getName() + Formatter.primary + " tried to join, but is " + ((ban instanceof Temporary) ? "temp " : "") + "banned!";
            for (final Player p2 : Bukkit.getOnlinePlayers()) {
                if (p2.hasPermission("maxbans.notify")) {
                    p2.sendMessage(msg2);
                }
            }
        }
    }
}
